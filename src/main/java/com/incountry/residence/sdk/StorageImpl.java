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
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Basic implementation
 */
public class StorageImpl implements Storage {
    private static final Logger LOG = LoggerFactory.getLogger(StorageImpl.class);
    //params from OS env
    private static final String PARAM_ENV_ID = "INC_ENVIRONMENT_ID";
    private static final String PARAM_API_KEY = "INC_API_KEY";
    private static final String PARAM_ENDPOINT = "INC_ENDPOINT";
    //error messages
    private static final String MSG_PASS_ENV = "Please pass environment_id param or set INC_ENVIRONMENT_ID env var";
    private static final String MSG_PASS_DAO = "Please pass dao param";
    private static final String MSG_PASS_API_KEY = "Please pass api_key param or set INC_API_KEY env var";
    private static final String MSG_ERROR_NULL_COUNTRY = "Country cannot be null";
    private static final String MSG_NULL_KEY = "Key cannot be null";
    private static final String MSG_MIGR_NOT_SUPPORT = "Migration is not supported when encryption is off";
    private static final String MSG_ERROR_NULL_FILTERS = "Filters cannot be null";
    private static final String MSG_FOUND_NOTHING = "Nothing was found";
    private static final String MSG_NULL_RECORD = "Can't write null record";
    private static final String LOG_SECURE = "[SECURE]";
    private static final String LOG_SECURE2 = "[SECURE[";

    private Crypto crypto;
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
                secretKeyAccessor);
    }

    public StorageImpl(String environmentID, String apiKey, String endpoint, SecretKeyAccessor secretKeyAccessor)
            throws StorageServerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StorageImpl constructor params (environmentID={} , apiKey={} , endpoint={}, secretKeyAccessor={})",
                    environmentID != null ? LOG_SECURE2 + environmentID.hashCode() + "]]" : null,
                    apiKey != null ? LOG_SECURE2 + apiKey.hashCode() + "]]" : null,
                    endpoint,
                    secretKeyAccessor != null ? LOG_SECURE : null
            );
        }
        checkEnvironment(environmentID);
        if (apiKey == null) {
            LOG.error(MSG_PASS_API_KEY);
            throw new IllegalArgumentException(MSG_PASS_API_KEY);
        }
        createCrypto(secretKeyAccessor, environmentID);
        dao = new HttpDaoImpl(apiKey, environmentID, endpoint);
    }

    public StorageImpl(String environmentID, SecretKeyAccessor secretKeyAccessor, Dao dao) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StorageImpl constructor params (environmentID={} , secretKeyAccessor={} , dao={})",
                    environmentID != null ? LOG_SECURE2 + environmentID.hashCode() + "]]" : null,
                    secretKeyAccessor != null ? LOG_SECURE : null,
                    dao != null ? LOG_SECURE : null
            );
        }
        checkEnvironment(environmentID);
        createCrypto(secretKeyAccessor, environmentID);
        if (dao == null) {
            LOG.error(MSG_PASS_DAO);
            throw new IllegalArgumentException(MSG_PASS_DAO);
        }
        this.dao = dao;
    }

    private void checkEnvironment(String environmentID) {
        if (environmentID == null) {
            LOG.error(MSG_PASS_ENV);
            throw new IllegalArgumentException(MSG_PASS_ENV);
        }
    }

    private void createCrypto(SecretKeyAccessor secretKeyAccessor, String environmentID) {
        isEncrypted = secretKeyAccessor != null;
        if (isEncrypted) {
            crypto = new CryptoImpl(secretKeyAccessor.getKey(), environmentID);
        } else {
            crypto = new CryptoImpl(environmentID);
        }
    }

    private void checkParameters(String country, String key) {
        if (country == null) {
            LOG.error(MSG_PASS_ENV);
            throw new IllegalArgumentException(MSG_ERROR_NULL_COUNTRY);
        }
        if (key == null) {
            LOG.error(MSG_NULL_KEY);
            throw new IllegalArgumentException(MSG_NULL_KEY);
        }
    }

    public Record write(String country, Record record) throws StorageServerException, StorageCryptoException {
        long currentTime = 0;
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis();
            LOG.debug("write start");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("write params (country={} , record={})",
                    country,
                    record != null ? LOG_SECURE2 + record.hashCode() + "]]" : null);
        }
        if (record == null) {
            LOG.error(MSG_NULL_RECORD);
            throw new IllegalArgumentException(MSG_NULL_RECORD);
        }
        checkParameters(country, record.getKey());
        dao.createRecord(country, record, crypto);
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis() - currentTime;
            LOG.debug("write, latency in ms={}", currentTime);
        }
        return record;
    }


    public Record read(String country, String recordKey) throws StorageServerException, StorageCryptoException {
        long currentTime = 0;
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis();
            LOG.debug("read start");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("read params (country={} , recordKey={})",
                    country,
                    recordKey != null ? LOG_SECURE : null);
        }
        checkParameters(country, recordKey);
        Record record = dao.read(country, recordKey, crypto);
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis() - currentTime;
            LOG.debug("read, latency in ms={}", currentTime);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("read results ({})", record != null ? record.hashCode() : null);
        }
        return record;
    }

    public MigrateResult migrate(String country, int limit) throws StorageException {
        long currentTime = 0;
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis();
            LOG.debug("migrate start");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("migrate params (country={} , limit={})",
                    country,
                    limit);
        }
        if (!isEncrypted) {
            LOG.error(MSG_MIGR_NOT_SUPPORT);
            throw new StorageException(MSG_MIGR_NOT_SUPPORT);
        }
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(limit, 0)
                .versionNotEq(String.valueOf(crypto.getCurrentSecretVersion()));
        BatchRecord batchRecord = find(country, builder);
        batchWrite(country, batchRecord.getRecords());
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis() - currentTime;
            LOG.debug("batchWrite, latency in ms={}", currentTime);
        }
        MigrateResult result = new MigrateResult(batchRecord.getCount(), batchRecord.getTotal() - batchRecord.getCount());
        if (LOG.isTraceEnabled()) {
            LOG.trace("batchWrite results={}", result);
        }
        return result;
    }

    public BatchRecord batchWrite(String country, List<Record> records) throws StorageServerException, StorageCryptoException {
        long currentTime = 0;
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis();
            LOG.debug("batchWrite start");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("batchWrite params (country={} , records={})",
                    country,
                    BatchRecord.toString(records));
        }
        if (records != null && !records.isEmpty()) {
            for (Record one : records) {
                checkParameters(country, one.getKey());
            }
            dao.createBatch(records, country, crypto);
        }
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis() - currentTime;
            LOG.debug("batchWrite, latency in ms={}", currentTime);
        }
        return new BatchRecord(records, 0, 0, 0, 0, null);
    }

    public boolean delete(String country, String recordKey) throws StorageServerException {
        long currentTime = 0;
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis();
            LOG.debug("delete start");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("delete params (country={} , recordKey={})",
                    country,
                    recordKey != null ? LOG_SECURE : null);
        }
        checkParameters(country, recordKey);
        dao.delete(country, recordKey, crypto);
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis() - currentTime;
            LOG.debug("delete, latency in ms={}", currentTime);
        }
        return true;
    }

    public BatchRecord find(String country, FindFilterBuilder builder) throws StorageServerException {
        long currentTime = 0;
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis();
            LOG.debug("find start");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("find params (country={} , builder={})",
                    country,
                    builder);
        }
        if (country == null) {
            LOG.error(MSG_ERROR_NULL_COUNTRY);
            throw new IllegalArgumentException(MSG_ERROR_NULL_COUNTRY);
        }
        if (builder == null) {
            LOG.error(MSG_ERROR_NULL_FILTERS);
            throw new IllegalArgumentException(MSG_ERROR_NULL_FILTERS);
        }

        BatchRecord batchRecord = dao.find(country, builder, crypto);
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis() - currentTime;
            LOG.debug("find, latency in ms={}", currentTime);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("find results ({})", batchRecord);
        }
        return batchRecord;
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
        long currentTime = 0;
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis();
            LOG.debug("findOne start");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("findOne params (country={} , builder={})",
                    country,
                    builder);
        }
        BatchRecord findResults = find(country, builder);
        List<Record> records = findResults.getRecords();
        if (LOG.isDebugEnabled()) {
            currentTime = System.currentTimeMillis() - currentTime;
            LOG.debug("findOne, latency in ms={}", currentTime);
        }
        if (records.isEmpty()) {
            LOG.warn(MSG_FOUND_NOTHING);
            return null;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("findOne results ({})", records.get(0).hashCode());
        }
        return records.get(0);
    }
}
