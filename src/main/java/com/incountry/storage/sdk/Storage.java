package com.incountry.storage.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.storage.sdk.dto.*;
import com.incountry.storage.sdk.tools.JsonUtils;
import com.incountry.storage.sdk.tools.crypto.Crypto;
import com.incountry.storage.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.storage.sdk.tools.exceptions.StorageCryptoException;
import org.json.JSONObject;
import com.incountry.storage.sdk.tools.exceptions.StorageException;
import com.incountry.storage.sdk.tools.exceptions.StorageServerException;
import com.incountry.storage.sdk.tools.http.HttpAgent;
import com.incountry.storage.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.storage.sdk.tools.http.impl.HttpAgentImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Access point of SDK
 */
public class Storage {
    private static final String PORTAL_BACKEND_URI = "https://portal-backend.incountry.com";
    private static final String DEFAULT_ENDPOINT = "https://us.api.incountry.io";
    private static final String STORAGE_URL = "/v2/storage/records/";
    //params from OS env
    private static final String PARAM_ENV_ID = "INC_ENVIRONMENT_ID";
    private static final String PARAM_API_KEY = "INC_API_KEY";
    private static final String PARAM_ENDPOINT = "INC_ENDPOINT";
    //error messages
    private static final String MESSAGE_SERVER_ERROR = "Server request error";
    private static final String MESSAGE_ENV_EXCEPTION = "Please pass environment_id param or set INC_ENVIRONMENT_ID env var";
    private static final String MESSAGE_ERR_GET_COUNTRIES = "Unable to retrieve available countries list";
    private static final String MESSAGE_ERROR_NULL_COUNTRY = "Country cannot be null";
    private static final String MESSAGE_NULL_KEY = "Key cannot be null";
    private static final String MESSAGE_PASS_API_KEY = "Please pass api_key param or set INC_API_KEY env var";

    private String envID;
    private String apiKey;
    private String endpoint;
    private boolean defaultEndpoint = false;
    private Crypto crypto;
    private HashMap<String, POP> poplist;
    private HttpAgent httpAgent;
    private boolean isEncrypted;

    public Storage() throws StorageServerException {
        this(null);
    }

    public Storage(SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
        this(System.getenv(PARAM_ENV_ID),
                System.getenv(PARAM_API_KEY),
                System.getenv(PARAM_ENDPOINT),
                secretKeyAccessor != null,
                secretKeyAccessor);
    }

    public Storage(String environmentID, String apiKey, SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
        this(environmentID, apiKey, null, secretKeyAccessor != null, secretKeyAccessor);
    }

    public Storage(String environmentID, String apiKey, String endpoint, boolean encrypt, SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
        envID = environmentID;
        if (envID == null) {
            throw new IllegalArgumentException(MESSAGE_ENV_EXCEPTION);
        }
        this.apiKey = apiKey;
        if (this.apiKey == null) {
            throw new IllegalArgumentException(MESSAGE_PASS_API_KEY);
        }
        this.endpoint = endpoint;
        if (this.endpoint == null) {
            this.endpoint = DEFAULT_ENDPOINT;
            defaultEndpoint = true;
        }
        poplist = new HashMap<>();
        httpAgent = new HttpAgentImpl(apiKey, environmentID);
        loadCountryEndpoints();
        isEncrypted = encrypt;
        if (encrypt) {
            crypto = new CryptoImpl(secretKeyAccessor.getKey(), environmentID);
        } else {
            crypto = new CryptoImpl(environmentID);
        }
    }

    /**
     * Load endpoint from server
     *
     * @throws StorageServerException if server connection failed or server response error
     */
    private void loadCountryEndpoints() throws StorageServerException {
        String content;
        try {
            content = httpAgent.request(PORTAL_BACKEND_URI + "/countries", "GET", null, false);
        } catch (IOException e) {
            throw new StorageServerException(MESSAGE_ERR_GET_COUNTRIES, e);
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        JsonObject contentJson = gson.fromJson(content, JsonObject.class);
        contentJson.getAsJsonArray("countries").forEach(item -> {
            if (((JsonObject) item).get("direct").getAsBoolean()) {
                String countryCode = ((JsonObject) item).get("id").getAsString().toLowerCase();
                POP pop = new POP("https://" + countryCode + ".api.incountry.io",
                        ((JsonObject) item).get("name").getAsString());
                poplist.put(countryCode, pop);
            }
        });
    }

    private String getEndpoint(String country, String path) {
        if (!defaultEndpoint) {
            return endpoint + path;
        }
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        if (poplist.containsKey(country)) {
            return poplist.get(country).host + path;
        }
        return endpoint + path;
    }

    private void checkParameters(String country, String key) {
        if (country == null) throw new IllegalArgumentException(MESSAGE_ERROR_NULL_COUNTRY);
        if (key == null) throw new IllegalArgumentException(MESSAGE_NULL_KEY);
    }

    private String createUrl(String country, String recordKey) {
        checkParameters(country, recordKey);
        country = country.toLowerCase();
        if (crypto != null) recordKey = crypto.createKeyHash(recordKey);
        return getEndpoint(country, STORAGE_URL + country + "/" + recordKey);
    }

    public void setHttpAgent(HttpAgent agent) {
        httpAgent = agent;
    }

    /**
     * Write data to remote storage
     *
     * @param record object which encapsulate data which must be written in storage
     * @return recorded record
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if encryption failed
     */
    public Record write(Record record) throws StorageServerException, StorageCryptoException {
        String country = record.getCountry().toLowerCase();
        checkParameters(country, record.getKey());
        String url = getEndpoint(country, "/v2/storage/records/" + country);
        try {
            httpAgent.request(url, "POST", JsonUtils.toJsonString(record, crypto), false);
        } catch (IOException e) {
            throw new StorageServerException(MESSAGE_SERVER_ERROR, e);
        }
        return record;
    }

    /**
     * Read data from remote storage
     *
     * @param country   country identifier
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
            throw new StorageServerException(MESSAGE_SERVER_ERROR, e);
        }
        if (content == null) {
            return null;
        }
        Record record = JsonUtils.recordFromString(content, crypto);
        record.setCountry(country);

        return record;
    }

    /**
     * Make batched key-rotation-migration of records
     *
     * @param country country identifier
     * @param limit   batch-limit parameter
     * @return MigrateResult object which contain total records left to migrate and total amount of migrated records
     * @throws StorageException if encryption is off/failed, if server connection failed or server response error
     */
    public MigrateResult migrate(String country, int limit) throws StorageException {
        if (!isEncrypted) {
            throw new StorageException("Migration is not supported when encryption is off");
        }
        Integer secretKeyCurrentVersion = crypto.getCurrentSecretVersion();
        FindFilter findFilter = new FindFilter();
        findFilter.setVersionParam(new FilterStringParam(secretKeyCurrentVersion.toString(), true));
        BatchRecord batchRecord = find(country, findFilter, new FindOptions(limit, 0));
        batchWrite(country, batchRecord.getRecords());

        return new MigrateResult(batchRecord.getCount(), batchRecord.getTotal() - batchRecord.getCount());
    }

    /**
     * Write multiple records at once in remote storage
     *
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
            recordsStrings.add(JsonUtils.toJson(record, crypto));
        }
        String url = getEndpoint(country, "/v2/storage/records/" + country + "/batchWrite");
        try {
            httpAgent.request(url, "POST", "{ \"records\" : " + new Gson().toJson(recordsStrings) + "}", false);
        } catch (IOException e) {
            throw new StorageServerException(MESSAGE_SERVER_ERROR, e);
        }

        return new BatchRecord(records, 0, 0, 0, 0, null);
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
     *
     * @param country   country identifier
     * @param recordKey record unique identifier
     * @return true if delete was successful
     * @throws StorageServerException if server connection failed
     */
    public boolean delete(String country, String recordKey) throws StorageServerException {
        String url = createUrl(country, recordKey);
        try {
            httpAgent.request(url, "DELETE", null, false);
        } catch (IOException e) {
            throw new StorageServerException(MESSAGE_SERVER_ERROR, e);
        }
        return true;
    }

    /**
     * Find records in remote storage
     *
     * @param country country identifier
     * @param filter  object representing find filters
     * @param options find options
     * @return BatchRecord object which contains required records
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    public BatchRecord find(String country, FindFilter filter, FindOptions options) throws StorageServerException, StorageCryptoException {
        if (country == null) throw new IllegalArgumentException("Country cannot be null");
        if (filter == null) throw new IllegalArgumentException("Filters cannot be null");
        if (options == null) throw new IllegalArgumentException("Options cannot be null");
        country = country.toLowerCase();
        String url = getEndpoint(country, STORAGE_URL + country + "/find");

        String postData = new JSONObject()
                .put("filter", JsonUtils.toJson(filter,crypto))
                .put("options", JsonUtils.toJson(options))
                .toString();

        String content;

        try {
            content = httpAgent.request(url, "POST", postData, false);
        } catch (IOException e) {
            throw new StorageServerException(MESSAGE_SERVER_ERROR, e);
        }

        if (content == null) {
            return null;
        }
        return JsonUtils.batchRecordFromString(content, crypto);
    }

    /**
     * Find one record in remote storage
     *
     * @param country country identifier
     * @param filter  object representing find filters
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

    class POP {
        String host;
        String name;

        POP(String h, String n) {
            host = h;
            name = n;
        }
    }
}
