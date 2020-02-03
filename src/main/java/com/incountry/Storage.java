package com.incountry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.crypto.impl.CryptoImpl;
import com.incountry.exceptions.StorageCryptoException;
import com.incountry.http.HttpAgent;
import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.response.BatchResponse;
import com.incountry.response.Metadata;
import com.incountry.response.SingleResponse;
import org.json.JSONObject;
import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.http.impl.HttpAgentImpl;

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
    private CryptoImpl mCrypto = null;
    private HashMap<String, POP> mPoplist;
    private HttpAgent httpAgent = null;

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

    public Storage(String environmentID, String apiKey, SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
        this(environmentID, apiKey, null, secretKeyAccessor != null, secretKeyAccessor);
    }

    public Storage(String environmentID, String apiKey, String endpoint, boolean encrypt, SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
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
        httpAgent = new HttpAgentImpl(apiKey, environmentID);

        loadCountryEndpoints();

        if (encrypt) {
            mCrypto = new CryptoImpl(secretKeyAccessor.getKey(), environmentID);
        }

    }

    /**
     * Load endpoint from server
     * @throws StorageServerException if server return one of client error responses
     * @throws IOException if server connection failed
     */
    private void loadCountryEndpoints() throws StorageServerException {
        String content;
        try {
           content = httpAgent.request(PORTALBACKEND_URI + "/countries", "GET", null, false);
        } catch (IOException e) {
            throw new StorageServerException("Server request error", e);
        }

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
            return mEndpoint + path;
        if (path.charAt(0) != '/') path = "/" + path;
        if (mPoplist.containsKey(country))
            return mPoplist.get(country).host + path;
        return mEndpoint + path;
    }

    private void checkParameters(String country, String key) {
        if (country == null) throw new NullPointerException("Missing country");
        if (key == null) throw new NullPointerException("Missing key");
    }

    public void write(Record record) throws StorageServerException, StorageCryptoException {
        String country = record.getCountry().toLowerCase();
        checkParameters(country, record.getKey());
        String url = getEndpoint(country, "/v2/storage/records/"+country);
        try {
            httpAgent.request(url, "POST", record.toJsonString(mCrypto), false);
        } catch (IOException e) {
            throw new StorageServerException("Server request error", e);
        }
    }

    private String createUrl(String country, String recordKey) {
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
    public SingleResponse read(String country, String recordKey) throws StorageServerException, StorageCryptoException {
        SingleResponse response = new SingleResponse();
        String url = createUrl(country, recordKey);
        String content;
        try {
            content = httpAgent.request(url, "GET", null, true);
        } catch (IOException e) {
            throw new StorageServerException("Server request error", e);
        }
        if (content == null) {
            return response;
        }
        Record record = Record.fromString(content,  mCrypto);
        record.setCountry(country);
        response.setRecord(record);

        return response;

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
    public Metadata migrate(String country, int limit) throws StorageServerException, StorageCryptoException {
        if (mCrypto == null) {
            throw new NullPointerException("Migration is not supported when encryption is off");
        }
        Integer secretKeyCurrentVersion = mCrypto.getCurrentSecretVersion();
        FindFilter findFilter = new FindFilter();
        findFilter.setVersionParam(new FilterStringParam(secretKeyCurrentVersion.toString(), true));
        BatchRecord batchRecord = find(country, findFilter,  new FindOptions(limit, 0)).getBatchRecord();
        batchWrite(country, batchRecord.getRecords());
        Metadata metadata = new Metadata();
        metadata.setMigrated(batchRecord.getCount());
        metadata.setTotalLeft(batchRecord.getTotal() - batchRecord.getCount());

        return metadata;
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
    public boolean batchWrite(String country, List<Record> records) throws StorageServerException, StorageCryptoException {
        country = country.toLowerCase();
        List<JsonObject> recordsStrings = new ArrayList<>();
        for (Record record : records) {
            checkParameters(country, record.getKey());
            recordsStrings.add(record.toJsonObject(mCrypto));
        }
        String url = getEndpoint(country, "/v2/storage/records/"  + country + "/batchWrite");
        try {
            httpAgent.request(url, "POST", "{ \"records\" : " + new Gson().toJson(recordsStrings) + "}", false);
        } catch (IOException e) {
            throw new StorageServerException("Server request error", e);
        }

        return true;
    }

    public SingleResponse updateOne(String country, FindFilter filter, Record record) throws StorageServerException, StorageCryptoException {
    	FindOptions options = new FindOptions(1, 0);
    	BatchRecord existingRecords = find(country, filter, options).getBatchRecord() ;

    	if (existingRecords.getTotal() > 1) {
    		throw new StorageServerException("Multiple records found");
    	}
    	if (existingRecords.getTotal() <= 0) {
    		throw new StorageServerException("Record not found");
    	}

    	Record foundRecord = existingRecords.getRecords().get(0);

    	Record updatedRecord = Record.merge(foundRecord, record);

    	write(updatedRecord);
        SingleResponse response = new SingleResponse();
        response.setRecord(updatedRecord);
    	return response;
    }

    /**
     * Delete record from remote storage
     * @param country country identifier
     * @param recordKey record unique identifier
     * @throws StorageException if country or recordKey is null
     * @throws IOException if server connection failed
     */
    public void delete(String country, String recordKey) throws StorageServerException {
        String url = createUrl(country, recordKey);
        try {
            httpAgent.request(url, "DELETE", null, false);
        } catch (IOException e) {
            throw new StorageServerException("Server request error", e);
        }
    }

    public void setHttpAgent(HttpAgent agent) {
        httpAgent = agent;
    }

    public BatchResponse find(String country, FindFilter filter, FindOptions options) throws StorageServerException, StorageCryptoException {
        if (country == null) throw new NullPointerException("Missing country");
        country = country.toLowerCase();
        String url = getEndpoint(country, "/v2/storage/records/"+country+"/find");

        String postData = new JSONObject()
            .put("filter", filter.toJSONObject(mCrypto))
            .put("options", options.toJSONObject())
            .toString();

        BatchResponse response = new BatchResponse();
        String content;

        try {
            content = httpAgent.request(url, "POST", postData, false);
        } catch (IOException e) {
            throw new StorageServerException("Server request error", e);
        }

        if (content == null) {
            return response;
        }
        response.setBatchRecord(BatchRecord.fromString(content, mCrypto));
        return response;
    }
    
    public SingleResponse findOne(String country, FindFilter filter, FindOptions options) throws StorageException, IOException, GeneralSecurityException {
        BatchRecord findResults =  find(country, filter, options).getBatchRecord();
        List<Record> records = findResults.getRecords();

    	if (records.isEmpty()) {
    		return null;
    	}
    	SingleResponse response = new SingleResponse();
    	response.setRecord(records.get(0));
    	return response;
    }

}
