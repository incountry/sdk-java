package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

/**
 * Basic implementation
 */
public class StorageImpl implements Storage {
    private static final Logger LOG = LogManager.getLogger(StorageImpl.class);
    //params from OS env
    private static final String PARAM_ENV_ID = "INC_ENVIRONMENT_ID";
    private static final String PARAM_API_KEY = "INC_API_KEY";
    private static final String PARAM_ENDPOINT = "INC_ENDPOINT";
    //error messages
    private static final String MSG_ERR_PASS_ENV = "Please pass environment_id param or set INC_ENVIRONMENT_ID env var";
    private static final String MSG_ERR_PASS_DAO = "Please pass dao param";
    private static final String MSG_ERR_PASS_API_KEY = "Please pass api_key param or set INC_API_KEY env var";
    private static final String MSG_ERR_NULL_BATCH = "Can't write empty batch";
    private static final String MSG_ERR_NULL_COUNTRY = "Country can't be null";
    private static final String MSG_ERR_NULL_KEY = "Key can't be null";
    private static final String MSG_ERR_NULL_FILTERS = "Filters can't be null";
    private static final String MSG_ERR_NULL_RECORD = "Can't write null record";
    private static final String MSG_ERR_MIGR_NOT_SUPPORT = "Migration is not supported when encryption is off";
    private static final String MSG_ERR_MIGR_ERROR_LIMIT = "Limit can't be < 1";
    private static final String MSG_ERR_CUSTOM_ENCRYPTION_ACCESSOR = "Custom encryption can be used only with not null SecretKeyAccessor";

    private static final String MSG_FOUND_NOTHING = "Nothing was found";
    private static final String LOG_SECURE = "[SECURE]";
    private static final String LOG_SECURE2 = "[SECURE[";


    private CryptoManager cryptoManager;
    private Dao dao;
    private boolean isEncrypted;

    private static String loadFromEnv(String key) {
        return System.getenv(key);
    }

    private StorageImpl() {
    }

    /**
     * creating Storage instance with ENV variables without encryption
     *
     * @return instance of Storage
     * @throws StorageClientException if configuration validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    public static Storage getInstance() throws StorageClientException, StorageServerException {
        return getInstance((SecretKeyAccessor) null);
    }

    /**
     * creating Storage instance with ENV variables
     *
     * @param secretKeyAccessor Instance of SecretKeyAccessor class. Used to fetch encryption secret
     * @return instance of Storage
     * @throws StorageClientException if configuration validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    public static Storage getInstance(SecretKeyAccessor secretKeyAccessor) throws StorageClientException, StorageServerException {
        return getInstance(loadFromEnv(PARAM_ENV_ID),
                loadFromEnv(PARAM_API_KEY),
                loadFromEnv(PARAM_ENDPOINT),
                secretKeyAccessor);
    }

    /**
     * creating Storage instance
     *
     * @param environmentID     Required to be passed in, or as environment variable INC_API_KEY
     * @param apiKey            Required to be passed in, or as environment variable INC_ENVIRONMENT_ID
     * @param endpoint          Optional. Defines API URL. Default endpoint will be used if this param is null
     * @param secretKeyAccessor Instance of SecretKeyAccessor class. Used to fetch encryption secret
     * @return instance of Storage
     * @throws StorageClientException if configuration validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    public static Storage getInstance(String environmentID, String apiKey, String endpoint, SecretKeyAccessor secretKeyAccessor)
            throws StorageClientException, StorageServerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StorageImpl constructor params (environmentID={} , apiKey={} , endpoint={}, secretKeyAccessor={})",
                    environmentID != null ? LOG_SECURE2 + environmentID.hashCode() + "]]" : null,
                    apiKey != null ? LOG_SECURE2 + apiKey.hashCode() + "]]" : null,
                    endpoint,
                    secretKeyAccessor != null ? LOG_SECURE : null);
        }
        StorageImpl instance = getInstanceWithoutCrypto(environmentID, apiKey, endpoint, secretKeyAccessor);
        instance.cryptoManager = new CryptoManager(secretKeyAccessor, environmentID);
        return ProxyUtils.createLoggingProxyForPublicMethods(instance);
    }

    /**
     * creating Storage instance
     *
     * @param config Configuration for Storage initialization
     * @return instance of Storage
     * @throws StorageClientException if configuration validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    public static Storage getInstance(StorageConfig config)
            throws StorageClientException, StorageServerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StorageImpl constructor params (environmentID={} , apiKey={} , endpoint={}, secretKeyAccessor={}, cryptoList={})",
                    config.getEnvId() != null ? LOG_SECURE2 + config.getEnvId().hashCode() + "]]" : null,
                    config.getApiKey() != null ? LOG_SECURE2 + config.getApiKey().hashCode() + "]]" : null,
                    config.getEndPoint(),
                    config.getSecretKeyAccessor() != null ? LOG_SECURE : null,
                    config.getCustomEncryptionList()
            );
        }
        if (config.getSecretKeyAccessor() == null && !(config.getCustomEncryptionList() == null || config.getCustomEncryptionList().isEmpty())) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION_ACCESSOR);
            throw new StorageClientException(MSG_ERR_CUSTOM_ENCRYPTION_ACCESSOR);
        }
        StorageImpl instance = getInstanceWithoutCrypto(config.getEnvId(), config.getApiKey(), config.getEndPoint(), config.getSecretKeyAccessor());
        instance.cryptoManager = new CryptoManager(config.getSecretKeyAccessor(), config.getEnvId(), config.getCustomEncryptionList());
        return ProxyUtils.createLoggingProxyForPublicMethods(instance);
    }

    public static Storage getInstance(String environmentID, SecretKeyAccessor secretKeyAccessor, Dao
            dao) throws StorageClientException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StorageImpl constructor params (environmentID={} , secretKeyAccessor={} , dao={})",
                    environmentID != null ? LOG_SECURE2 + environmentID.hashCode() + "]]" : null,
                    secretKeyAccessor != null ? LOG_SECURE : null,
                    dao != null ? LOG_SECURE : null
            );
        }
        checkNotNull(environmentID, MSG_ERR_PASS_ENV);
        checkNotNull(dao, MSG_ERR_PASS_DAO);
        StorageImpl instance = new StorageImpl();
        instance.isEncrypted = secretKeyAccessor != null;
        instance.cryptoManager = new CryptoManager(secretKeyAccessor, environmentID);
        instance.dao = dao;
        return ProxyUtils.createLoggingProxyForPublicMethods(instance);
    }

    private static StorageImpl getInstanceWithoutCrypto(String environmentID, String apiKey, String endpoint, SecretKeyAccessor secretKeyAccessor)
            throws StorageClientException, StorageServerException {
        checkNotNull(environmentID, MSG_ERR_PASS_ENV);
        checkNotNull(apiKey, MSG_ERR_PASS_API_KEY);
        StorageImpl instance = new StorageImpl();
        instance.dao = new HttpDaoImpl(apiKey, environmentID, endpoint);
        instance.isEncrypted = secretKeyAccessor != null;
        return instance;
    }

    private static void checkNotNull(Object parameter, String nullErrorMessage) throws StorageClientException {
        if (parameter == null) {
            LOG.error(nullErrorMessage);
            throw new StorageClientException(nullErrorMessage);
        }
    }


    private void checkParameters(String country, String key) throws StorageClientException {
        checkNotNull(country, MSG_ERR_PASS_ENV);
        checkNotNull(key, MSG_ERR_NULL_KEY);
    }

    public Record write(String country, Record record) throws
            StorageClientException, StorageServerException, StorageCryptoException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("write params (country={} , record={})",
                    country,
                    record != null ? LOG_SECURE2 + record.hashCode() + "]]" : null);
        }
        checkNotNull(record, MSG_ERR_NULL_RECORD);
        checkParameters(country, record.getKey());
        dao.createRecord(country, record, cryptoManager);
        return record;
    }


    public Record read(String country, String key) throws StorageClientException, StorageServerException, StorageCryptoException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("read params (country={} , key={})",
                    country,
                    key != null ? LOG_SECURE : null);
        }
        checkParameters(country, key);
        Record record = dao.read(country, key, cryptoManager);
        if (LOG.isTraceEnabled()) {
            LOG.trace("read results ({})", record != null ? record.hashCode() : null);
        }
        return record;
    }

    public MigrateResult migrate(String country, int limit) throws
            StorageClientException, StorageServerException, StorageCryptoException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("migrate params (country={} , limit={})",
                    country,
                    limit);
        }
        if (!isEncrypted) {
            LOG.error(MSG_ERR_MIGR_NOT_SUPPORT);
            throw new StorageClientException(MSG_ERR_MIGR_NOT_SUPPORT);
        }
        if (limit < 1) {
            LOG.error(MSG_ERR_MIGR_ERROR_LIMIT);
            throw new StorageClientException(MSG_ERR_MIGR_ERROR_LIMIT);
        }
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(limit, 0)
                .versionNotEq(String.valueOf(cryptoManager.getCurrentSecretVersion()));
        BatchRecord batchRecord = find(country, builder);
        batchWrite(country, batchRecord.getRecords());
        MigrateResult result = new MigrateResult(batchRecord.getCount(), batchRecord.getTotal() - batchRecord.getCount());
        if (LOG.isTraceEnabled()) {
            LOG.trace("batchWrite results={}", result);
        }
        return result;
    }

    public BatchRecord batchWrite(String country, List<Record> records) throws
            StorageClientException, StorageServerException, StorageCryptoException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("batchWrite params (country={} , records={})",
                    country,
                    BatchRecord.toString(records));
        }
        if (records == null || records.isEmpty()) {
            LOG.error(MSG_ERR_NULL_BATCH);
            throw new StorageClientException(MSG_ERR_NULL_BATCH);
        } else {
            for (Record record : records) {
                checkParameters(country, record.getKey());
            }
            dao.createBatch(records, country, cryptoManager);
        }
        return new BatchRecord(records, 0, 0, 0, 0, null);
    }

    public boolean delete(String country, String key) throws StorageClientException, StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("delete params (country={} , key={})",
                    country,
                    key != null ? LOG_SECURE : null);
        }
        checkParameters(country, key);
        dao.delete(country, key, cryptoManager);
        return true;
    }

    public BatchRecord find(String country, FindFilterBuilder builder) throws
            StorageClientException, StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("find params (country={} , builder={})", country, builder);
        }
        checkNotNull(country, MSG_ERR_NULL_COUNTRY);
        checkNotNull(builder, MSG_ERR_NULL_FILTERS);
        BatchRecord batchRecord = dao.find(country, builder.copy(), cryptoManager);
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
    public Record findOne(String country, FindFilterBuilder builder) throws
            StorageClientException, StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("findOne params (country={} , builder={})",
                    country,
                    builder);
        }
        BatchRecord findResults = find(country, builder != null ? builder.copy().limitAndOffset(1, 0) : null);
        List<Record> records = findResults.getRecords();
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
