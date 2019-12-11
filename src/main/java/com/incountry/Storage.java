package com.incountry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.crypto.impl.Crypto;
import com.incountry.exceptions.FindOptionsException;
import com.incountry.exceptions.StorageClientException;
import com.incountry.http.IHttpAgent;
import com.incountry.keyaccessor.SecretKeyAccessor;
import org.json.JSONObject;
import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.http.impl.HttpAgent;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public Storage(SecretKeyAccessor secretKeyAccessor) throws StorageServerException, IOException {
        this(System.getenv("INC_ENVIRONMENT_ID"),
                System.getenv("INC_API_KEY"),
                System.getenv("INC_ENDPOINT"),
                secretKeyAccessor != null,
                secretKeyAccessor);
    }

    public Storage(String environmentID, String apiKey, SecretKeyAccessor secretKeyAccessor) throws StorageServerException, IOException {
        this(environmentID, apiKey, null, secretKeyAccessor != null, secretKeyAccessor);
    }

    public Storage(String environmentID, String apiKey, String endpoint, boolean encrypt, SecretKeyAccessor secretKeyAccessor) throws StorageServerException, IOException {
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

        loadCountryEndpoints();

        if (encrypt) {
            mCrypto = new Crypto(secretKeyAccessor.getKey(), environmentID);
        }

    }

    /**
     * Load endpoint from server
     * @throws StorageServerException if server return one of client error responses
     * @throws IOException if server connection failed
     */
    private void loadCountryEndpoints() throws StorageServerException, IOException {
        String content = httpAgent.request(PORTALBACKEND_URI + "/countries", "GET", null, false);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        JsonObject contentJson = gson.fromJson(content, JsonObject.class);
        contentJson.getAsJsonArray("countries").forEach(item -> {
            if(((JsonObject) item).get("direct").getAsBoolean()) {
                String countryCode  = ((JsonObject) item).get("id").getAsString().toLowerCase();
                POP pop = new POP("https://" + countryCode + ".api.incountry.io", ((JsonObject) item).get("name").getAsString());
                mPoplist.put(countryCode, pop);
            }
        });
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

    public void write(Record record) throws StorageException, GeneralSecurityException, IOException{
        String country = record.getCountry().toLowerCase();
        checkParameters(country, record.getKey());
        String url = getEndpoint(country, "/v2/storage/records/"+country);
        httpAgent.request(url, "POST", record.toString(mCrypto), false);
    }

    private String createUrl(String country, String recordKey) throws StorageException {
        country = country.toLowerCase();
        checkParameters(country, recordKey);
        if (mCrypto != null) recordKey = mCrypto.createKeyHash(recordKey);
        return getEndpoint(country, "/v2/storage/records/" + country + "/" + recordKey);
    }

    /**
     * Read data from remote storage
     * @param country country identifier
     * @param recordKey record unique identifier
     * @return record object
     * @throws StorageException if country or recordKey is null
     * @throws IOException if server connection failed
     * @throws GeneralSecurityException if record decryption failed
     */
    public Record read(String country, String recordKey) throws StorageException, IOException, GeneralSecurityException{
        String url = createUrl(country, recordKey);
        String content = httpAgent.request(url, "GET", null, true);
        if (content == null) return null;
        Record record = Record.fromString(content,  mCrypto);
        record.setCountry(country);

        return record;
    }

    /**
     * Make batched key-rotation-migration of records
     * @param country country identifier
     * @param limit batch-limit parameter
     * @return MigrateResult object which contain total records left to migrate and total amount of migrated records
     * @throws StorageException if country or recordKey is null
     * @throws FindOptionsException if limit more than 100
     * @throws GeneralSecurityException if record encryption failed
     * @throws IOException if server connection failed
     */
    public MigrateResult migrate(String country, int limit) throws StorageException, FindOptionsException, GeneralSecurityException, IOException {
        if (mCrypto == null) {
            throw new StorageClientException("Migration not supported when encryption is off");
        }
        Integer secretKeyCurrentVersion = mCrypto.getCurrentSecretVersion();
        FindFilter findFilter = new FindFilter();
        findFilter.setVersionParam(new FilterStringParam(secretKeyCurrentVersion.toString(), true));
        BatchRecord batchRecord = find(country, findFilter,  new FindOptions(limit, 0));
        batchWrite(country, batchRecord.getRecords());

        return new MigrateResult(batchRecord.getCount(), batchRecord.getTotal() - batchRecord.getCount());
    }

    /**
     * Write multiple records at once in remote storage
     * @param country country identifier
     * @param records record list
     * @return true if writing was successful
     * @throws StorageException if country or recordKey is null
     * @throws GeneralSecurityException if record encryption failed
     * @throws IOException if server connection failed
     */
    public boolean batchWrite(String country, List<Record> records) throws StorageException, GeneralSecurityException, IOException {
        country = country.toLowerCase();
        List<String> recordsStrings = new ArrayList<>();
        for (Record record : records) {
            checkParameters(country, record.getKey());
            recordsStrings.add(record.toString(mCrypto));
        }
        String url = getEndpoint(country, "/v2/storage/records/"  + country + "/batchWrite");
        httpAgent.request(url, "POST", new Gson().toJson(recordsStrings), false);

        return true;
    }

    public Record updateOne(String country, FindFilter filter, Record record) throws StorageException, GeneralSecurityException, IOException, FindOptionsException{
    	FindOptions options = new FindOptions(1, 0);
    	BatchRecord existingRecords = find(country, filter, options);

    	if (existingRecords.getTotal() > 1) {
    		throw new StorageServerException("Multiple records found");
    	}
    	if (existingRecords.getTotal() <= 0) {
    		throw new StorageServerException("Record not found");
    	}
    	
    	Record foundRecord = existingRecords.getRecords().get(0);

    	Record updatedRecord = Record.merge(foundRecord, record);

    	write(updatedRecord);
    	
    	return updatedRecord;
    }

    /**
     * Delete record from remote storage
     * @param country country identifier
     * @param recordKey record unique identifier
     * @throws StorageException if country or recordKey is null
     * @throws IOException if server connection failed
     */
    public void delete(String country, String recordKey) throws StorageException, IOException {
        String url = createUrl(country, recordKey);
        httpAgent.request(url, "DELETE", null, false);
    }

    public void setHttpAgent(IHttpAgent agent) {
        httpAgent = agent;
    }

    public BatchRecord find(String country, FindFilter filter, FindOptions options) throws StorageException, IOException, GeneralSecurityException {
        if (country == null) throw new StorageClientException("Missing country");
        country = country.toLowerCase();
        String url = getEndpoint(country, "/v2/storage/records/"+country+"/find");

        String postData = new JSONObject()
            .put("filter", filter.toJSONObject(mCrypto))
            .put("options", options.toJSONObject())
            .toString();

        String content = httpAgent.request(url, "POST", postData, false);

        if (content == null) return null;
        return BatchRecord.fromString(content, mCrypto);
    }
    
    public Record findOne(String country, FindFilter filter, FindOptions options) throws StorageException, IOException, GeneralSecurityException {
    	BatchRecord findResults = find(country, filter, options);

    	List<Record> records = findResults.getRecords();

    	if (records.isEmpty()) {
    		return null;
    	}
    	
    	return records.get(0);
    }

}
