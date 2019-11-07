package com.incountry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incountry.crypto.Crypto;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class Storage {
    private static final String PORTALBACKEND_URI = "https://portal-backend.incountry.com";
    private static final String DEFAULT_ENDPOINT = "https://us.api.incountry.io";

    class StorageException extends Exception {
        StorageException(String s){
            super(s);
        }
    }

    class StorageClientException extends StorageException {
        StorageClientException(String s) {
            super(s);
        }
    }

    class StorageServerException extends StorageException {
        StorageServerException(String s) {
            super(s);
        }
    }

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

    public Storage() throws Exception {
        this(System.getenv("INC_ENVIRONMENT_ID"),
                System.getenv("INC_API_KEY"),
                System.getenv("INC_ENDPOINT"),
                System.getenv("INC_SECRET_KEY") != null,
                System.getenv("INC_SECRET_KEY"));
    }

    public Storage(String environmentID, String apiKey, String secretKey) throws StorageServerException, IOException {
        this(environmentID, apiKey, null, secretKey != null, secretKey);
    }

    public Storage(String environmentID, String apiKey, String endpoint, boolean encrypt, String secretKey) throws StorageServerException, IOException {
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
        load_country_endpoints();

        if (encrypt) {
            mCrypto = new Crypto(secretKey, environmentID);
        }

    }

    private String http(String endpoint, String method, String body, boolean allowNone) throws StorageServerException, IOException{
        URL url = new URL(endpoint);
        //System.out.println(url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Authorization", "Bearer "+mAPIKey);
        con.setRequestProperty("x-env-id", mEnvID);
        con.setRequestProperty("Content-Type", "application/json");
        if (body != null){
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(body.getBytes());
            os.flush();
            os.close();
        }
        //System.out.println(con);
        int status = con.getResponseCode();
        BufferedReader in = null;
        if (status < 400) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        }
        else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        //System.out.println(content);

        if (allowNone && status == 404) return null;
        if (status >= 400)
            throw new StorageServerException(status + " " + endpoint + " - " + content);

        return content.toString();
    }

    private void load_country_endpoints() throws StorageServerException, IOException {
        String content = http(PORTALBACKEND_URI+"/countries", "GET", null, false);
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

    private void checkParameters(String country, String key) throws StorageException{
        if (country == null) throw new StorageClientException("Missing country");
        if (key == null) throw new StorageClientException("Missing key");
    }

    public void write(String country, String key, String body, String profile_key, String range_key, String key2, String key3) throws StorageException, GeneralSecurityException, IOException{
        country = country.toLowerCase();
        checkParameters(country, key);
        Data data = new Data(country, key, body, profile_key, range_key, key2, key3);
        String url = getEndpoint(country, "/v2/storage/records/"+country);
        http(url, "POST", data.toString(mCrypto), false);
    }

    public Data read(String country, String key) throws StorageException, IOException, GeneralSecurityException {
        country = country.toLowerCase();
        checkParameters(country, key);
        if (mCrypto != null) key = mCrypto.createKeyHash(key);
        String url = getEndpoint(country, "/v2/storage/records/" + country + "/" + key);
        String content = http(url, "GET", null, true);
        if (content == null) return null;
        Data d = Data.fromString(content,  mCrypto);
        d.setCountry(country);
        return d;
    }

    public String delete(String country, String key) throws StorageException, IOException {
        country = country.toLowerCase();
        checkParameters(country, key);
        if (mCrypto != null) key = mCrypto.createKeyHash(key);
        String url = getEndpoint(country, "/v2/storage/records/" + country + "/" + key);
        return http(url, "DELETE", null, false);
    }

    public BatchData find(String country, FindFilter filter, FindOptions options) throws StorageException, IOException, GeneralSecurityException {
        if (country == null) throw new StorageClientException("Missing country");
        country = country.toLowerCase();
        String url = getEndpoint(country, "/v2/storage/records/"+country+"/find");
        String postData = new JSONObject()
            .put("filter", filter.toJSONObject(mCrypto))
            .put("options", options.toJSONObject())
            .toString();

        String content = http(url, "POST", postData, false);

        if (content == null) return null;
        return BatchData.fromString(content, mCrypto);
    }

}
