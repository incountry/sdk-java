package com.incountry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incountry.FindOptions.FindOptionsException;
import com.incountry.crypto.Crypto;
import com.incountry.exceptions.StorageClientException;
import com.incountry.http.IHttpAgent;
import com.incountry.key_accessor.ISecretKeyAccessor;
import org.json.JSONObject;
import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.http.HttpAgent;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class Storage {
    private static final String PORTALBACKEND_URI = "https://portal-backend.incountry.com";
    private static final String DEFAULT_ENDPOINT = "https://us.api.incountry.io";

    class POP {
        String host;
        String name;
        POP(String h, String n){
            host = h;
            name = n;
        }
    }

    private String mEnvID = null;
    private String mAPIKey = null;
    private String mEndpoint = null;
    private Boolean mIsDefaultEndpoint = false;
    private Crypto mCrypto = null;
    private HashMap<String, POP> mPoplist;
    private IHttpAgent httpAgent = null;

    public Storage() throws StorageServerException, IOException {
        this(null);
    }

    public Storage(ISecretKeyAccessor secretKeyAccessor) throws StorageServerException, IOException {
        this(System.getenv("INC_ENVIRONMENT_ID"),
                System.getenv("INC_API_KEY"),
                System.getenv("INC_ENDPOINT"),
                secretKeyAccessor != null,
                secretKeyAccessor);
    }

    public Storage(String environmentID, String apiKey, ISecretKeyAccessor secretKeyAccessor) throws StorageServerException, IOException {
        this(environmentID, apiKey, null, secretKeyAccessor != null, secretKeyAccessor);
    }

    public Storage(String environmentID, String apiKey, String endpoint, boolean encrypt, ISecretKeyAccessor secretKeyAccessor) throws StorageServerException, IOException {
        mEnvID = environmentID;
        if (mEnvID == null) throw new IllegalArgumentException("Please pass environment_id param or set INC_ENVIRONMENT_ID env var");

        mAPIKey = apiKey;
        if (mAPIKey == null) throw new IllegalArgumentException("Please pass api_key param or set INC_API_KEY env var");

        mEndpoint = endpoint;
        if (mEndpoint == null) {
        	mEndpoint = DEFAULT_ENDPOINT;
        	mIsDefaultEndpoint = true;
        }

        mPoplist = new HashMap<String, POP>();
        httpAgent = new HttpAgent(apiKey, environmentID);

        load_country_endpoints();

        if (encrypt) {
            mCrypto = new Crypto(secretKeyAccessor.getKey(), environmentID);
        }

    }

    private void load_country_endpoints() throws StorageServerException, IOException {
        String content = httpAgent.request(PORTALBACKEND_URI+"/countries", "GET", null, false);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(content);
        JsonNode countries = actualObj.get("countries");
        int i = countries.size();
        while (i-->0) {
            JsonNode country = countries.get(i);
            if (country.get("direct").asBoolean()){
                String cc = country.get("id").asText().toLowerCase();
                POP pop = new POP("https://"+cc+".api.incountry.io", country.get("name").asText());
                mPoplist.put(cc, pop);
            }
        }
    }

    private String getEndpoint(String country, String path){
    	if (Boolean.FALSE.equals(mIsDefaultEndpoint))
            return mEndpoint+path;
        if (path.charAt(0) != '/') path = "/"+path;
        if (mPoplist.containsKey(country))
            return mPoplist.get(country).host+path;
        return mEndpoint+path;
    }

    private void checkParameters(String country, String key) throws StorageException {
        if (country == null) throw new StorageClientException("Missing country");
        if (key == null) throw new StorageClientException("Missing key");
    }

    public void write(String country, String key, String body, String profileKey, Integer rangeKey, String key2, String key3) throws StorageException, GeneralSecurityException, IOException{
        country = country.toLowerCase();
        checkParameters(country, key);
        Data data = new Data(country, key, body, profileKey, rangeKey, key2, key3);
        String url = getEndpoint(country, "/v2/storage/records/"+country);
        httpAgent.request(url, "POST", data.toString(mCrypto), false);
    }

    public Data read(String country, String key) throws StorageException, IOException, GeneralSecurityException {
        country = country.toLowerCase();
        checkParameters(country, key);
        if (mCrypto != null) key = mCrypto.createKeyHash(key);
        String url = getEndpoint(country, "/v2/storage/records/" + country + "/" + key);
        String content = httpAgent.request(url, "GET", null, true);
        if (content == null) return null;
        Data d = Data.fromString(content,  mCrypto);
        d.setCountry(country);
        return d;
    }
    
    public Data updateOne(String country, FindFilter filter, String key, String body, String profileKey, Integer rangeKey, String key2, String key3) throws StorageException, GeneralSecurityException, IOException, FindOptionsException{
    	FindOptions options = new FindOptions(1, 0);
    	BatchData existingRecords = find(country, filter, options);
    	
    	if (existingRecords.getTotal() > 1) {
    		throw new StorageException("Multiple records found");
    	}
    	if (existingRecords.getTotal() <= 0) {
    		throw new StorageException("Record not found");
    	}
    	
    	Data foundRecord = existingRecords.getRecords()[0];
    	
    	Data updatedRecord = new Data(country, key != null ? key : foundRecord.key, body != null ? body : foundRecord.body, profileKey != null ? profileKey : foundRecord.profileKey, rangeKey != null ? rangeKey : foundRecord.rangeKey, key2 != null ? key2 : foundRecord.key2, key3 != null ? key3 : foundRecord.key3);
    	write(country, updatedRecord.key, updatedRecord.body, updatedRecord.profileKey, updatedRecord.rangeKey, updatedRecord.key2, updatedRecord.key3);
    	
    	return updatedRecord;
    }

    public String delete(String country, String key) throws StorageException, IOException {
        country = country.toLowerCase();
        checkParameters(country, key);
        if (mCrypto != null) key = mCrypto.createKeyHash(key);
        String url = getEndpoint(country, "/v2/storage/records/" + country + "/" + key);
        return httpAgent.request(url, "DELETE", null, false);
    }

    public void setHttpAgent(IHttpAgent agent) {
        httpAgent = agent;
    }
    public BatchData find(String country, FindFilter filter, FindOptions options) throws StorageException, IOException, GeneralSecurityException {
        if (country == null) throw new StorageClientException("Missing country");
        country = country.toLowerCase();
        String url = getEndpoint(country, "/v2/storage/records/"+country+"/find");
        String postData = new JSONObject()
            .put("filter", filter.toJSONObject(mCrypto))
            .put("options", options.toJSONObject())
            .toString();

        String content = httpAgent.request(url, "POST", postData, false);

        if (content == null) return null;
        return BatchData.fromString(content, mCrypto);
    }
    
    public Data findOne(String country, FindFilter filter, FindOptions options) throws StorageException, IOException, GeneralSecurityException {
    	BatchData findResults = find(country, filter, options);
    	
    	Data[] records = findResults.getRecords();
    	
    	if (records.length == 0) {
    		return null;
    	}
    	
    	return records[0];
    }

}
