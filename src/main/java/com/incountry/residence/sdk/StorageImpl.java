package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.ApiKeyTokenClient;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Basic implementation
 */
public class StorageImpl implements Storage {
    private static final Logger LOG = LogManager.getLogger(StorageImpl.class);
    //error messages
    private static final String MSG_ERR_PASS_ENV = "Please pass environment_id param or set INC_ENVIRONMENT_ID env var";
    private static final String MSG_ERR_AUTH_DUPL = "Either apiKey or clientId/clientSecret can be used at the same moment, not both";
    private static final String MSG_ERR_PASS_API_KEY = "Please pass api_key param or set INC_API_KEY env var";
    private static final String MSG_ERR_NULL_BATCH = "Can't write empty batch";
    private static final String MSG_ERR_NULL_COUNTRY = "Country can't be null";
    private static final String MSG_ERR_NULL_FILE_ID = "File ID can't be null";
    private static final String MSG_ERR_NULL_KEY = "Key can't be null";
    private static final String MSG_ERR_NULL_FILTERS = "Filters can't be null";
    private static final String MSG_ERR_NULL_RECORD = "Can't write null record";
    private static final String MSG_ERR_MIGR_NOT_SUPPORT = "Migration is not supported when encryption is off";
    private static final String MSG_ERR_MIGR_ERROR_LIMIT = "Limit can't be < 1";
    private static final String MSG_ERR_CUSTOM_ENCRYPTION_ACCESSOR = "Custom encryption can be used only with not null SecretKeyAccessor";
    private static final String MSG_ERR_PASS_CLIENT_ID = "Please pass clientId in configuration or set INC_CLIENT_ID env var";
    private static final String MSG_ERR_PASS_CLIENT_SECRET = "Please pass clientSecret in configuration or set INC_CLIENT_SECRET env var";
    private static final String MSG_ERR_PASS_AUTH = "Please pass (clientId, clientSecret) in configuration or set (INC_CLIENT_ID, INC_CLIENT_SECRET) env vars";
    private static final String MSG_ERR_ILLEGAL_TIMEOUT = "Connection timeout can't be <1. Expected 'null' or positive value, received=%d";
    private static final String MSG_ERR_CONNECTION_POOL = "HTTP pool size can't be < 1. Expected 'null' or positive value, received=%d";
    private static final String MSG_ERR_MAX_CONNECTIONS_PER_ROUTE = "Max HTTP connections count per route can't be < 1. Expected 'null' or positive value, received=%d";
    private static final String MSG_ERR_NULL_FILE_NAME = "File name can't be null";
    private static final String MSG_ERR_NULL_FILE_MIME_TYPE = "MIME type can't be null";
    private static final String MSG_ERR_NULL_FILE_INPUT_STREAM = "File input stream can't be null";
    private static final String MSG_ERR_NOT_AVAILABLE_FILE_INPUT_STREAM = "File input stream is not available";

    private static final String MSG_FOUND_NOTHING = "Nothing was found";
    private static final String MSG_SIMPLE_SECURE = "[SECURE]";
    private static final int DEFAULT_HTTP_TIMEOUT = 30;
    private static final int DEFAULT_MAX_HTTP_CONNECTIONS = 20;

    private CryptoManager cryptoManager;
    private Dao dao;
    private boolean encrypted;

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
        StorageConfig config = new StorageConfig()
                .setSecretKeyAccessor(secretKeyAccessor)
                .useEnvIdFromEnv()
                .useApiKeyFromEnv()
                .useEndPointFromEnv()
                .useClientIdFromEnv()
                .useClientSecretFromEnv();
        return getInstance(config);
    }

    /**
     * creating Storage instance
     *
     * @param environmentID     Required to be passed in, or as environment variable INC_API_KEY with {@link #getInstance()}
     * @param apiKey            Required to be passed in, or as environment variable INC_ENVIRONMENT_ID with {@link #getInstance()}
     * @param endpoint          Optional. Defines API URL. Default endpoint will be used if this param is null
     * @param secretKeyAccessor Instance of SecretKeyAccessor class. Used to fetch encryption secret
     * @return instance of Storage
     * @throws StorageClientException if configuration validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    public static Storage getInstance(String environmentID, String apiKey, String endpoint, SecretKeyAccessor secretKeyAccessor)
            throws StorageClientException, StorageServerException {
        StorageConfig config = new StorageConfig()
                .setSecretKeyAccessor(secretKeyAccessor)
                .setEnvId(environmentID)
                .setApiKey(apiKey)
                .setEndPoint(endpoint);
        return getInstance(config);
    }

    /**
     * creating Storage instance
     *
     * @param config A container with configuration for Storage initialization
     * @return instance of Storage
     * @throws StorageClientException if configuration validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    public static Storage getInstance(StorageConfig config)
            throws StorageClientException, StorageServerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StorageImpl constructor params config={}", config);
        }
        if (config.getSecretKeyAccessor() == null && !(config.getCustomEncryptionConfigsList() == null || config.getCustomEncryptionConfigsList().isEmpty())) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION_ACCESSOR);
            throw new StorageClientException(MSG_ERR_CUSTOM_ENCRYPTION_ACCESSOR);
        }
        return getInstance(config, null);
    }

    public static Storage getInstance(String environmentID, SecretKeyAccessor secretKeyAccessor, Dao dao) throws StorageClientException, StorageServerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StorageImpl constructor params (environmentID={} , secretKeyAccessor={} , dao={})",
                    environmentID != null ? String.format(StorageConfig.MSG_SECURE, environmentID.hashCode()) : null,
                    secretKeyAccessor,
                    dao
            );
        }
        StorageConfig config = new StorageConfig()
                .setEnvId(environmentID)
                .setSecretKeyAccessor(secretKeyAccessor);
        return getInstance(config, dao);
    }

    private static Storage getInstance(StorageConfig config, Dao dao)
            throws StorageClientException, StorageServerException {
        checkNotNull(config.getEnvId(), MSG_ERR_PASS_ENV);
        if (config.getApiKey() != null && config.getClientId() != null) {
            LOG.error(MSG_ERR_AUTH_DUPL);
            throw new StorageClientException(MSG_ERR_AUTH_DUPL);
        }
        StorageImpl instance = new StorageImpl();
        instance.dao = initDao(config, dao);
        instance.encrypted = config.getSecretKeyAccessor() != null;
        instance.cryptoManager = new CryptoManager(config.getSecretKeyAccessor(), config.getEnvId(), config.getCustomEncryptionConfigsList(), config.isNormalizeKeys());
        return ProxyUtils.createLoggingProxyForPublicMethods(instance);
    }

    private static CloseableHttpClient initHttpClient(Integer httpTimeout, Integer poolSize, Integer connectionsPerRoute) {
        if (httpTimeout == null) {
            httpTimeout = DEFAULT_HTTP_TIMEOUT;
        }
        httpTimeout *= 1000; //expected value in ms
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(httpTimeout)
                .setSocketTimeout(httpTimeout)
                .build();
        HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(requestConfig);
        if (poolSize == null) {
            poolSize = DEFAULT_MAX_HTTP_CONNECTIONS;
        }
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(poolSize);
        connectionManager.setDefaultMaxPerRoute(connectionsPerRoute != null ? connectionsPerRoute : poolSize);
        builder.setConnectionManager(connectionManager);

        return builder.build();
    }

    private static Dao initDao(StorageConfig config, Dao dao) throws StorageServerException, StorageClientException {
        if (dao == null) {
            Integer httpTimeout = config.getHttpTimeout();
            Integer httpPoolSize = config.getMaxHttpPoolSize();
            Integer connectionsPerRoute = config.getMaxHttpConnectionsPerRoute();
            checkPositiveOrNull(httpTimeout, MSG_ERR_ILLEGAL_TIMEOUT);
            checkPositiveOrNull(httpPoolSize, MSG_ERR_CONNECTION_POOL);
            checkPositiveOrNull(connectionsPerRoute, MSG_ERR_MAX_CONNECTIONS_PER_ROUTE);

            CloseableHttpClient httpClient = initHttpClient(httpTimeout, httpPoolSize, connectionsPerRoute);
            TokenClient tokenClient;
            if (config.getClientId() != null && config.getClientSecret() != null) {
                checkNotNull(config.getClientId(), MSG_ERR_PASS_CLIENT_ID);
                checkNotNull(config.getClientSecret(), MSG_ERR_PASS_CLIENT_SECRET);
                tokenClient = new OAuthTokenClient(config.getDefaultAuthEndpoint(),
                        config.getAuthEndpoints(),
                        config.getEnvId(),
                        config.getClientId(),
                        config.getClientSecret(),
                        httpClient
                );
                tokenClient = ProxyUtils.createLoggingProxyForPublicMethods(tokenClient);
            } else if (config.getApiKey() != null) {
                checkNotNull(config.getApiKey(), MSG_ERR_PASS_API_KEY);
                tokenClient = new ApiKeyTokenClient(config.getApiKey());
            } else {
                LOG.error(MSG_ERR_PASS_AUTH);
                throw new StorageClientException(MSG_ERR_PASS_AUTH);
            }
            return new HttpDaoImpl(config.getEnvId(),
                    config.getEndPoint(),
                    config.getEndpointMask(),
                    config.getCountriesEndpoint(),
                    tokenClient,
                    httpClient);
        } else {
            return dao;
        }
    }

    private static void checkPositiveOrNull(Integer intValue, String errorMessage) throws StorageClientException {
        if (intValue != null && intValue < 1) {
            String errMessage = String.format(errorMessage, intValue);
            LOG.error(errMessage);
            throw new StorageClientException(errMessage);
        }
    }

    private static void checkNotNull(Object parameter, String nullErrorMessage) throws StorageClientException {
        if (parameter == null || String.valueOf(parameter).isEmpty()) {
            LOG.error(nullErrorMessage);
            throw new StorageClientException(nullErrorMessage);
        }
    }

    private void checkParameters(String country, String key) throws StorageClientException {
        checkNotNull(country, MSG_ERR_NULL_COUNTRY);
        checkNotNull(key, MSG_ERR_NULL_KEY);
    }

    private void checkAttachmentParameters(String country, String key, String fileId) throws StorageClientException {
        checkNotNull(fileId, MSG_ERR_NULL_FILE_ID);
        checkParameters(country, key);
    }


    public Record write(String country, Record record) throws
            StorageClientException, StorageServerException, StorageCryptoException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("write params (country={} , record={})",
                    country,
                    record != null ? String.format(StorageConfig.MSG_SECURE, record.hashCode()) : null);
        }
        checkNotNull(record, MSG_ERR_NULL_RECORD);
        checkParameters(country, record.getRecordKey());
        dao.createRecord(country, record, cryptoManager);
        return record;
    }


    public Record read(String country, String recordKey) throws StorageClientException, StorageServerException, StorageCryptoException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("read params (country={} , recordKey={})",
                    country,
                    recordKey != null ? MSG_SIMPLE_SECURE : null);
        }
        checkParameters(country, recordKey);
        Record record = dao.read(country, recordKey, cryptoManager);
        if (LOG.isTraceEnabled()) {
            LOG.trace("read results ({})", record != null ? record.hashCode() : null);
        }
        return record;
    }

    public MigrateResult migrate(String country, int limit) throws
            StorageClientException, StorageServerException, StorageCryptoException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("migrate params (country={} , limit={})", country, limit);
        }
        if (!encrypted) {
            LOG.error(MSG_ERR_MIGR_NOT_SUPPORT);
            throw new StorageClientException(MSG_ERR_MIGR_NOT_SUPPORT);
        }
        if (limit < 1) {
            LOG.error(MSG_ERR_MIGR_ERROR_LIMIT);
            throw new StorageClientException(MSG_ERR_MIGR_ERROR_LIMIT);
        }
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(limit, 0)
                .keyNotEq(StringField.VERSION, String.valueOf(cryptoManager.getCurrentSecretVersion()));
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
                checkParameters(country, record.getRecordKey());
            }
            dao.createBatch(records, country, cryptoManager);
        }
        return new BatchRecord(records, 0, 0, 0, 0, null);
    }

    public boolean delete(String country, String recordKey) throws StorageClientException, StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("delete params (country={} , key={})",
                    country,
                    recordKey != null ? MSG_SIMPLE_SECURE : null);
        }
        checkParameters(country, recordKey);
        dao.delete(country, recordKey, cryptoManager);
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

    @Override
    public String addAttachment(String country, String recordKey, InputStream fileStream, boolean upsert) throws StorageClientException, StorageServerException {
        try {
            if (fileStream == null || fileStream.available() == 0) {
                LOG.error(MSG_ERR_NULL_FILE_INPUT_STREAM);
                throw new StorageClientException(MSG_ERR_NULL_FILE_INPUT_STREAM);
            }
        } catch (IOException ex) {
            LOG.error(MSG_ERR_NOT_AVAILABLE_FILE_INPUT_STREAM);
            throw new StorageClientException(MSG_ERR_NOT_AVAILABLE_FILE_INPUT_STREAM, ex);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("addAttachment params (country={} , key={}, fileStreamHash={}, upsert={})",
                    country, recordKey, fileStream.hashCode(), upsert);
        }
        checkParameters(country, recordKey);
        return dao.addAttachment(country, recordKey, fileStream, upsert);
    }

    @Override
    public boolean deleteAttachment(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("addAttachment params (country={} , key={}, fileId={})",
                    country, recordKey, fileId);
        }
        checkAttachmentParameters(country, recordKey, fileId);
        dao.deleteAttachment(country, recordKey, fileId);
        LOG.trace("deleteAttachment method result: true");
        return true;
    }

    @Override
    public AttachedFile getAttachmentFile(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("getAttachmentFile params (country={} , key={}, fileId={})",
                    country, recordKey, fileId);
        }
        checkAttachmentParameters(country, recordKey, fileId);
        return dao.getAttachmentFile(country, recordKey, fileId);
    }

    @Override
    public void updateAttachmentMeta(String country, String recordKey, String fileId, String fileName, String mimeType) throws StorageClientException, StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("updateAttachmentMeta params (country={} , key={}, fileId={}, fileName={}, mimeType={})",
                    country, recordKey, fileId, fileName, mimeType);
        }
        checkNotNull(fileName, MSG_ERR_NULL_FILE_NAME);
        checkNotNull(mimeType, MSG_ERR_NULL_FILE_MIME_TYPE);
        checkAttachmentParameters(country, recordKey, fileId);
        dao.updateAttachmentMeta(country, recordKey, fileId, fileName, mimeType);
    }

    @Override
    public AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("getAttachmentMeta params (country={} , key={}, fileId={})",
                    country, recordKey, fileId);
        }
        checkAttachmentParameters(country, recordKey, fileId);
        AttachmentMeta attachmentMeta = dao.getAttachmentMeta(country, recordKey, fileId);
        LOG.trace("getAttachmentMeta method result: {}", attachmentMeta);
        return attachmentMeta;
    }
}
