package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Basic implementation
 */
public class StorageImpl implements Storage {
    private static final String PORTAL_COUNTRIES_URI = "https://portal-backend.incountry.com/countries";
    private static final String DEFAULT_ENDPOINT = "https://us.api.incountry.io";
    private static final String URI_ENDPOINT_PART = ".api.incountry.io";
    private static final String STORAGE_URL = "/v2/storage/records/";
    private static final String URI_HTTPS = "https://";
    private static final String URI_POST = "POST";
    private static final String URI_GET = "GET";
    private static final String URI_DELIMITER = "/";
    //params from OS env
    private static final String PARAM_ENV_ID = "INC_ENVIRONMENT_ID";
    private static final String PARAM_API_KEY = "INC_API_KEY";
    private static final String PARAM_ENDPOINT = "INC_ENDPOINT";
    //error messages
    private static final String MSG_SERVER_ERROR = "Server request error";
    private static final String MSG_ENV_EXCEPTION = "Please pass environment_id param or set INC_ENVIRONMENT_ID env var";
    private static final String MSG_ERR_GET_COUNTRIES = "Unable to retrieve available countries list";
    private static final String MSG_ERROR_NULL_COUNTRY = "Country cannot be null";
    private static final String MSG_NULL_KEY = "Key cannot be null";
    private static final String MSG_PASS_API_KEY = "Please pass api_key param or set INC_API_KEY env var";
    private static final String MSG_MIGR_NOT_SUPPORT = "Migration is not supported when encryption is off";
    private static final String MSG_MULTIPLE_FOUND = "Multiple records found";
    private static final String MSG_RECORD_NOT_FOUND = "Record not found";
    private static final String MSG_ERROR_NULL_FILTERS = "Filters cannot be null";

    private String envID;
    private String apiKey;
    private String endpoint;
    private boolean defaultEndpoint = false;
    private Crypto crypto;
    private HashMap<String, POP> poplist;
    private HttpAgent httpAgent;
    private boolean isEncrypted;

    public StorageImpl() throws StorageServerException {
        this(null);
    }

    public StorageImpl(SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
        this(System.getenv(PARAM_ENV_ID),
                System.getenv(PARAM_API_KEY),
                System.getenv(PARAM_ENDPOINT),
                secretKeyAccessor != null,
                secretKeyAccessor);
    }

    public StorageImpl(String environmentID, String apiKey, SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
        this(environmentID, apiKey, null, secretKeyAccessor != null, secretKeyAccessor);
    }

    public StorageImpl(String environmentID, String apiKey, String endpoint, boolean encrypt, SecretKeyAccessor secretKeyAccessor)
            throws StorageServerException {
        envID = environmentID;
        if (envID == null) {
            throw new IllegalArgumentException(MSG_ENV_EXCEPTION);
        }
        this.apiKey = apiKey;
        if (this.apiKey == null) {
            throw new IllegalArgumentException(MSG_PASS_API_KEY);
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
            content = httpAgent.request(PORTAL_COUNTRIES_URI, URI_GET, null, false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_ERR_GET_COUNTRIES, e);
        }
        JsonUtils.getCountryEntryPoint(content, (String code, String name) -> {
            POP pop = new POP(URI_HTTPS + code + URI_ENDPOINT_PART, name);
            poplist.put(name, pop);
        });
    }

    private String getEndpoint(String country, String path) {
        if (!defaultEndpoint) {
            return endpoint + path;
        }
        if (!path.startsWith(URI_DELIMITER)) {
            path = URI_DELIMITER + path;
        }
        if (poplist.containsKey(country)) {
            return poplist.get(country).host + path;
        }
        return endpoint + path;
    }

    private void checkParameters(String country, String key) {
        if (country == null) {
            throw new IllegalArgumentException(MSG_ERROR_NULL_COUNTRY);
        }
        if (key == null) {
            throw new IllegalArgumentException(MSG_NULL_KEY);
        }
    }

    private String createUrl(String country, String recordKey) {
        checkParameters(country, recordKey);
        country = country.toLowerCase();
        if (crypto != null) {
            recordKey = crypto.createKeyHash(recordKey);
        }
        return getEndpoint(country, STORAGE_URL + country + URI_DELIMITER + recordKey);
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
    public Record create(Record record) throws StorageServerException, StorageCryptoException {
        String country = record.getCountry().toLowerCase();
        checkParameters(country, record.getKey());
        String url = getEndpoint(country, STORAGE_URL + country);
        try {
            httpAgent.request(url, URI_POST, JsonUtils.toJsonString(record, crypto), false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
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
            content = httpAgent.request(url, URI_GET, null, true);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
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
            throw new StorageException(MSG_MIGR_NOT_SUPPORT);
        }
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(limit, 0)
                .versionNotEq(String.valueOf(crypto.getCurrentSecretVersion()));
        BatchRecord batchRecord = find(country, builder);
        createBatch(country, batchRecord.getRecords());

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
    public BatchRecord createBatch(String country, List<Record> records) throws StorageServerException, StorageCryptoException {
        country = country.toLowerCase();
        String recListJson = JsonUtils.toJsonString(records, country, crypto, this::checkParameters);
        String url = getEndpoint(country, STORAGE_URL + country + URI_DELIMITER + "batchWrite");
        try {
//            httpAgent.request(url, "POST", "{ \"records\" : " + recordsString + "}", false);
            httpAgent.request(url, URI_POST, recListJson, false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
        }

        return new BatchRecord(records, 0, 0, 0, 0, null);
    }

    public Record updateOne(String country, FindFilterBuilder builder, Record recordForMerging) throws StorageServerException, StorageCryptoException {
        FindFilter filter = builder.limitAndOffset(1, 0).build();
        BatchRecord existingRecords = find(country, builder);

        if (existingRecords.getTotal() > 1) {
            throw new StorageServerException(MSG_MULTIPLE_FOUND);
        }
        if (existingRecords.getTotal() <= 0) {
            throw new StorageServerException(MSG_RECORD_NOT_FOUND);
        }

        Record foundRecord = existingRecords.getRecords().get(0);

        Record updatedRecord = Record.merge(foundRecord, recordForMerging);

        create(updatedRecord);

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
            throw new StorageServerException(MSG_SERVER_ERROR, e);
        }
        return true;
    }

    /**
     * Find records in remote storage
     *
     * @param country country identifier
     * @param builder object representing find filters
     * @return BatchRecord object which contains required records
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    public BatchRecord find(String country, FindFilterBuilder builder) throws StorageServerException, StorageCryptoException {
        if (country == null) {
            throw new IllegalArgumentException(MSG_ERROR_NULL_COUNTRY);
        }
        if (builder == null) {
            throw new IllegalArgumentException(MSG_ERROR_NULL_FILTERS);
        }
        country = country.toLowerCase();
        String url = getEndpoint(country, STORAGE_URL + country + URI_DELIMITER + "find");
        String postData = JsonUtils.toJsonString(builder.build(), crypto);
        String content;
        try {
            content = httpAgent.request(url, URI_POST, postData, false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
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
     * @param builder object representing find filters
     * @return Record object which contains required data
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    public Record findOne(String country, FindFilterBuilder builder) throws StorageServerException, StorageCryptoException {
        BatchRecord findResults = find(country, builder);
        List<Record> records = findResults.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    /**
     * inner class-container to store host lisk with POP API
     */
    class POP {
        String host;
        String name;

        POP(String h, String n) {
            host = h;
            name = n;
        }
    }
}
