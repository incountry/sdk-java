package com.incountry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.crypto.Crypto;
import com.incountry.crypto.impl.CryptoImpl;
import com.incountry.exceptions.StorageCryptoException;
import com.incountry.http.HttpAgent;
import com.incountry.keyaccessor.SecretKeyAccessor;
import org.json.JSONObject;
import com.incountry.exceptions.StorageServerException;
import com.incountry.http.impl.HttpAgentImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Storage {
    private static final String PORTALBACKEND_URI = "https://portal-backend.incountry.com";
    private static final String DEFAULT_ENDPOINT = "https://us.api.incountry.io";
    private static final String STORAGE_URL = "/v2/storage/records/";
    private static final String SERVER_ERROR_MESSAGE = "Server request error";

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
    private HttpAgent httpAgent = null;

    public Storage() throws StorageServerException {
        this(null);
    }

    public Storage(SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
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
     * @throws StorageServerException if server connection failed or server response error
     */
    private void loadCountryEndpoints() throws StorageServerException {
        String content;
        try {
           content = httpAgent.request(PORTALBACKEND_URI + "/countries", "GET", null, false);
        } catch (IOException e) {
            throw new StorageServerException(SERVER_ERROR_MESSAGE, e);
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

    private String createUrl(String country, String recordKey) {
        country = country.toLowerCase();
        checkParameters(country, recordKey);
        if (mCrypto != null) recordKey = mCrypto.createKeyHash(recordKey);
        return getEndpoint(country, STORAGE_URL + country + "/" + recordKey);
    }

    public void setHttpAgent(HttpAgent agent) {
        httpAgent = agent;
    }

    /**
     * Write data to remote storage
     * @param record object which encapsulate data which must be written in storage
     * @return recorded record
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if encryption failed
     */
    public Record write(Record record) throws StorageServerException, StorageCryptoException {
        String country = record.getCountry().toLowerCase();
        checkParameters(country, record.getKey());
        String url = getEndpoint(country, "/v2/storage/records/"+country);
        try {
            httpAgent.request(url, "POST", record.toJsonString(mCrypto), false);
        } catch (IOException e) {
            throw new StorageServerException(SERVER_ERROR_MESSAGE, e);
        }
        return new Record();
    }

    /**
     * Read data from remote storage
     * @param country country identifier
     * @param recordKey record unique identifier
     * @return Record object which contains required data
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    public Record read(String country, String recordKey) throws StorageServerException, StorageCryptoException {
        String url = createUrl(country, recordKey);
        String content;
        try {
            content = httpAgent.request(url, "GET", null, true);
        } catch (IOException e) {
            throw new StorageServerException(SERVER_ERROR_MESSAGE, e);
        }
        if (content == null) {
            return null;
        }
        Record record = Record.fromString(content,  mCrypto);
        record.setCountry(country);

        return record;
    }

    /**
     * Make batched key-rotation-migration of records
     * @param country country identifier
     * @param limit batch-limit parameter
     * @return MigrateResult object which contain total records left to migrate and total amount of migrated records
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if encryption failed
     */
    public MigrateResult migrate(String country, int limit) throws StorageServerException, StorageCryptoException {
        if (mCrypto == null) {
            throw new StorageCryptoException("Migration is not supported when encryption is off");
        }
        Integer secretKeyCurrentVersion = mCrypto.getCurrentSecretVersion();
        FindFilter findFilter = new FindFilter();
        findFilter.setVersionParam(new FilterStringParam(secretKeyCurrentVersion.toString(), true));
        BatchRecord batchRecord = find(country, findFilter,  new FindOptions(limit, 0));
        batchWrite(country, batchRecord.getRecords());
        MigrateResult migrateResult = new MigrateResult(batchRecord.getCount(),batchRecord.getTotal() - batchRecord.getCount());

        return migrateResult;
    }

    /**
     * Write multiple records at once in remote storage
     * @param country country identifier
     * @param records record list
     * @return BatchRecord object which contains list of recorded records
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if record encryption failed
     */
    public BatchRecord batchWrite(String country, List<Record> records) throws StorageServerException, StorageCryptoException {
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
            throw new StorageServerException(SERVER_ERROR_MESSAGE, e);
        }

        return new BatchRecord(records, 0, 0, 0, 0);
    }

    public Record updateOne(String country, FindFilter filter, Record record) throws StorageServerException, StorageCryptoException {
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
     * @return true if delete was successful
     * @throws StorageServerException if server connection failed
     */
    public boolean delete(String country, String recordKey) throws StorageServerException {
        String url = createUrl(country, recordKey);
        try {
            httpAgent.request(url, "DELETE", null, false);
        } catch (IOException e) {
            throw new StorageServerException(SERVER_ERROR_MESSAGE, e);
        }
        return true;
    }

    /**
     * Find records in remote storage
     * @param country country identifier
     * @param filter object representing find filters
     * @param options find options
     * @return BatchRecord object which contains required records
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    public BatchRecord find(String country, FindFilter filter, FindOptions options) throws StorageServerException, StorageCryptoException {
        if (country == null) throw new NullPointerException("Country cannot be null");
        country = country.toLowerCase();
        String url = getEndpoint(country, STORAGE_URL + country + "/find");

        String postData = new JSONObject()
            .put("filter", filter.toJSONObject(mCrypto))
            .put("options", options.toJSONObject())
            .toString();

        String content;

        try {
            content = httpAgent.request(url, "POST", postData, false);
        } catch (IOException e) {
            throw new StorageServerException(SERVER_ERROR_MESSAGE, e);
        }

        if (content == null) {
            return null;
        }
        return BatchRecord.fromString(content, mCrypto);
    }

    /**
     * Find one record in remote storage
     * @param country country identifier
     * @param filter object representing find filters
     * @param options find options
     * @return Record object which contains required data
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    public Record findOne(String country, FindFilter filter, FindOptions options) throws StorageServerException, StorageCryptoException {
        BatchRecord findResults = find(country, filter, options);
        List<Record> records = findResults.getRecords();

    	if (records.isEmpty()) {
    		return null;
    	}
    	return records.get(0);
    }

}
