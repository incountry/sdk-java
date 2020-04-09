package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;

import java.util.List;
import java.util.Map;

/**
 * Basic implementation
 */
public class StorageImpl implements Storage {
    //params from OS env
    private static final String PARAM_ENV_ID = "INC_ENVIRONMENT_ID";
    private static final String PARAM_API_KEY = "INC_API_KEY";
    private static final String PARAM_ENDPOINT = "INC_ENDPOINT";
    //error messages
    private static final String MSG_ENV_EXCEPTION = "Please pass environment_id param or set INC_ENVIRONMENT_ID env var";
    private static final String MSG_ERROR_NULL_COUNTRY = "Country cannot be null";
    private static final String MSG_NULL_KEY = "Key cannot be null";
    private static final String MSG_PASS_API_KEY = "Please pass api_key param or set INC_API_KEY env var";
    private static final String MSG_MIGR_NOT_SUPPORT = "Migration is not supported when encryption is off";
    private static final String MSG_MULTIPLE_FOUND = "Multiple records found";
    private static final String MSG_RECORD_NOT_FOUND = "Record not found";
    private static final String MSG_ERROR_NULL_FILTERS = "Filters cannot be null";

    private String envID;
    private String apiKey;

    private Crypto crypto;
    private Map<String, POP> popMap;
    private Dao dao;
    private boolean isEncrypted;

    private static String loadFromEnv(String key) {
        return System.getenv(key);
    }

    public StorageImpl() throws StorageServerException {
        this(null);
    }

    public StorageImpl(SecretKeyAccessor secretKeyAccessor) throws StorageServerException {
        this(loadFromEnv(PARAM_ENV_ID),
                loadFromEnv(PARAM_API_KEY),
                loadFromEnv(PARAM_ENDPOINT),
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
        isEncrypted = encrypt;
        if (encrypt) {
            crypto = new CryptoImpl(secretKeyAccessor.getKey(), environmentID);
        } else {
            crypto = new CryptoImpl(environmentID);
        }
        dao = new HttpDaoImpl(apiKey, environmentID, endpoint);
        loadCountryEndpoints();
    }

    /**
     * Load endpoint from server
     *
     * @throws StorageServerException if server connection failed or server response error
     */
    private void loadCountryEndpoints() throws StorageServerException {
        popMap = dao.loadCounties();
    }

    private void checkParameters(String country, String key) {
        if (country == null) {
            throw new IllegalArgumentException(MSG_ERROR_NULL_COUNTRY);
        }
        if (key == null) {
            throw new IllegalArgumentException(MSG_NULL_KEY);
        }
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }


    public Record write(String country, Record record) throws StorageServerException, StorageCryptoException {
        country = country.toLowerCase();
        checkParameters(country, record.getKey());
        dao.createRecord(country, popMap.get(country), record, crypto);
        return record;
    }

    public Record read(String country, String recordKey) throws StorageServerException, StorageCryptoException {
        checkParameters(country, recordKey);
        return dao.read(country, popMap.get(country), recordKey, crypto);
    }

    public MigrateResult migrate(String country, int limit) throws StorageException {
        if (!isEncrypted) {
            throw new StorageException(MSG_MIGR_NOT_SUPPORT);
        }
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(limit, 0)
                .versionNotEq(String.valueOf(crypto.getCurrentSecretVersion()));
        BatchRecord batchRecord = find(country, builder);
        batchWrite(country, batchRecord.getRecords());
        return new MigrateResult(batchRecord.getCount(), batchRecord.getTotal() - batchRecord.getCount());
    }

    public BatchRecord batchWrite(String country, List<Record> records) throws StorageServerException, StorageCryptoException {
        if (records != null && !records.isEmpty()) {
            for (Record one : records) {
                checkParameters(country, one.getKey());
            }
            country = country.toLowerCase();
            dao.createBatch(records, country, popMap.get(country), crypto);
        }
        return new BatchRecord(records, 0, 0, 0, 0, null);
    }

    public boolean delete(String country, String recordKey) throws StorageServerException {
        checkParameters(country, recordKey);
        dao.delete(country, popMap.get(country), recordKey, crypto);
        return true;
    }

    public BatchRecord find(String country, FindFilterBuilder builder) throws StorageServerException {
        if (country == null) {
            throw new IllegalArgumentException(MSG_ERROR_NULL_COUNTRY);
        }
        if (builder == null) {
            throw new IllegalArgumentException(MSG_ERROR_NULL_FILTERS);
        }
        country = country.toLowerCase();
        return dao.find(country, popMap.get(country), builder, crypto);
    }

    /**
     * Find one record in remote storage
     *
     * @param country country identifier
     * @param builder object representing find filters
     * @return Record object which contains required data
     * @throws StorageServerException if server connection failed or server response error
     */
    public Record findOne(String country, FindFilterBuilder builder) throws StorageServerException {
        BatchRecord findResults = find(country, builder);
        List<Record> records = findResults.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    public Record updateOne(String country, FindFilterBuilder builder, Record recordForMerging) throws StorageServerException, StorageCryptoException {
        BatchRecord existingRecords = find(country, builder);
        if (existingRecords.getTotal() > 1) {
            throw new StorageServerException(MSG_MULTIPLE_FOUND);
        }
        if (existingRecords.getTotal() <= 0) {
            throw new StorageServerException(MSG_RECORD_NOT_FOUND);
        }
        Record foundRecord = existingRecords.getRecords().get(0);
        Record updatedRecord = Record.merge(foundRecord, recordForMerging);
        return write(country, updatedRecord);
    }
}
