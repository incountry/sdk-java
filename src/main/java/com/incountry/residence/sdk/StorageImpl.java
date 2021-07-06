package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.oauth.OauthTokenAccessor;
import com.incountry.residence.sdk.tools.DtoTransformer;
import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.http.HttpExecutor;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpExecutorImpl;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import com.incountry.residence.sdk.tools.transfer.TransferRecordList;
import com.incountry.residence.sdk.version.Version;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static com.incountry.residence.sdk.StorageConfig.DEFAULT_HTTP_TIMEOUT;
import static com.incountry.residence.sdk.StorageConfig.DEFAULT_MAX_HTTP_CONNECTIONS;
import static com.incountry.residence.sdk.StorageConfig.DEFAULT_RETRY_BASE_DELAY;
import static com.incountry.residence.sdk.StorageConfig.DEFAULT_RETRY_MAX_DELAY;
import static com.incountry.residence.sdk.tools.ValidationHelper.isNullOrEmpty;

/**
 * Basic implementation
 */
public class StorageImpl implements Storage {
    private static final Logger LOG = LogManager.getLogger(StorageImpl.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);
    //error messages
    private static final String MSG_ERR_PASS_ENV = "Please pass environment_id param or set INC_ENVIRONMENT_ID env var";
    private static final String MSG_ERR_AUTH = "Please use only one authorization: clientId/clientSecret or oauthTokenAccessor";
    private static final String MSG_ERR_NULL_BATCH = "Can't write empty batch";
    private static final String MSG_ERR_NULL_COUNTRY = "Country can't be null";
    private static final String MSG_ERR_NULL_FILE_ID = "File ID can't be null";
    private static final String MSG_ERR_NULL_KEY = "Key can't be null";
    private static final String MSG_ERR_NULL_RECORD = "Can't write null record";
    private static final String MSG_ERR_MIGR_NOT_SUPPORT = "Migration is not supported when encryption is off";
    private static final String MSG_ERR_MIGR_ERROR_LIMIT = "Limit can't be < 1";
    private static final String MSG_ERR_CONFIG = "Storage configuration is null";
    private static final String MSG_ERR_PASS_CLIENT_ID = "Please pass clientId in configuration";
    private static final String MSG_ERR_PASS_CLIENT_SECRET = "Please pass clientSecret in configuration";
    private static final String MSG_ERR_ILLEGAL_TIMEOUT = "Connection timeout can't be <1. Expected 'null' or positive value, received=%d";
    private static final String MSG_ERR_CONNECTION_POOL = "HTTP pool size can't be < 1. Expected 'null' or positive value, received=%d";
    private static final String MSG_ERR_MAX_CONNECTIONS_PER_ROUTE = "Max HTTP connections count per route can't be < 1. Expected 'null' or positive value, received=%d";
    private static final String MSG_ERR_NULL_FILE_NAME_AND_MIME_TYPE = "File name and MIME type can't be null";
    private static final String MSG_ERR_NULL_FILE_INPUT_STREAM = "Input stream can't be null";
    private static final String MSG_ERR_NOT_AVAILABLE_FILE_INPUT_STREAM = "Input stream is not available";
    private static final String MSG_FOUND_NOTHING = "Nothing was found";
    private static final String MSG_ERR_UNEXPECTED = "Unexpected error";
    private static final String MSG_ERR_NULL_SECRETS = "SecretKeyAccessor returns null secret";
    private static final String MSG_ERR_BASE_DELAY = "Retry base delay can't be < 1";
    private static final String MSG_ERR_MAX_DELAY = "Retry max delay can't be less then retry base delay";
    private static final String MSG_ERR_RESPONSE = "Response validation failed. Return data doesn't match the one sent";
    private static final String USER_AGENT_HEADER_NAME = "User-Agent";
    private static final String USER_AGENT_HEADER_VALUE = "SDK-Java/" + Version.BUILD_VERSION;

    private final Dao dao;
    private final HashUtils hashUtils;
    private final DtoTransformer transformer;

    private StorageImpl(StorageConfig config, Dao dao) throws StorageClientException, StorageCryptoException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StorageImpl constructor params config={}", config);
        }
        HELPER.check(StorageClientException.class, config == null, MSG_ERR_CONFIG);
        HELPER.check(StorageClientException.class, isNullOrEmpty(config.getEnvironmentId()), MSG_ERR_PASS_ENV);
        int alternativeAuthCount = 0;
        alternativeAuthCount += config.getClientId() != null ? 1 : 0;
        alternativeAuthCount += config.getOauthTokenAccessor() != null ? 1 : 0;
        HELPER.check(StorageClientException.class, alternativeAuthCount != 1, MSG_ERR_AUTH);

        this.dao = initDao(config, dao);
        this.hashUtils = new HashUtils(config.getEnvironmentId(), config.isNormalizeKeys());
        CryptoProvider cryptoProvider = config.getCryptoProvider() == null ? new CryptoProvider(null) : config.getCryptoProvider();
        if (config.getSecretKeyAccessor() != null) {
            boolean isInvalidAccessor;
            try {
                isInvalidAccessor = config.getSecretKeyAccessor().getSecretsData() == null;
            } catch (Exception ex) {
                LOG.error(MSG_ERR_UNEXPECTED, ex);
                throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
            }
            HELPER.check(StorageClientException.class, isInvalidAccessor, MSG_ERR_NULL_SECRETS);
        }
        cryptoProvider.validateCustomCiphers(config.getSecretKeyAccessor() == null ? null : config.getSecretKeyAccessor().getSecretsData());
        this.transformer = new DtoTransformer(cryptoProvider, hashUtils, config.isHashSearchKeys(), config.getSecretKeyAccessor());
    }

    /**
     * create new Storage instance
     *
     * @param config A container with configuration for Storage initialization
     * @return instance of Storage
     * @throws StorageClientException if configuration validation finished with errors
     * @throws StorageCryptoException if custom cipher validation fails
     */
    public static Storage getInstance(StorageConfig config) throws StorageClientException, StorageCryptoException {
        return getInstance(config, null);
    }

    /**
     * overloaded version for tests and debugging
     *
     * @param config A container with configuration for Storage initialization
     * @param dao    dao, can be mocked for tests
     * @return instance of Storage
     * @throws StorageClientException if parameter validation finished with errors
     * @throws StorageCryptoException if custom cipher validation fails
     */
    public static Storage getInstance(StorageConfig config, Dao dao) throws StorageClientException, StorageCryptoException {
        Storage instance = new StorageImpl(config, dao);
        return ProxyUtils.createLoggingProxyForPublicMethods(instance, true);
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
        if (poolSize == null) {
            poolSize = DEFAULT_MAX_HTTP_CONNECTIONS;
        }
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(poolSize);
        connectionManager.setDefaultMaxPerRoute(connectionsPerRoute != null ? connectionsPerRoute : poolSize);
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .setDefaultHeaders(Collections.singleton(new BasicHeader(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_VALUE)));
        return builder.build();
    }

    private static Dao initDao(StorageConfig config, Dao dao) throws StorageClientException {
        if (dao == null) {
            Integer httpTimeout = config.getHttpTimeout();
            Integer httpPoolSize = config.getMaxHttpPoolSize();
            Integer connectionsPerRoute = config.getMaxHttpConnectionsPerRoute();
            checkPositiveOrNull(httpTimeout, MSG_ERR_ILLEGAL_TIMEOUT);
            checkPositiveOrNull(httpPoolSize, MSG_ERR_CONNECTION_POOL);
            checkPositiveOrNull(connectionsPerRoute, MSG_ERR_MAX_CONNECTIONS_PER_ROUTE);

            Integer retryBaseDelay = config.getRetryBaseDelay();
            retryBaseDelay = retryBaseDelay != null ? retryBaseDelay : DEFAULT_RETRY_BASE_DELAY;
            HELPER.check(StorageClientException.class, retryBaseDelay < 1, MSG_ERR_BASE_DELAY);
            Integer retryMaxDelay = config.getRetryMaxDelay();
            retryMaxDelay = retryMaxDelay != null ? retryMaxDelay : DEFAULT_RETRY_MAX_DELAY;
            HELPER.check(StorageClientException.class, retryMaxDelay < retryBaseDelay, MSG_ERR_MAX_DELAY);

            CloseableHttpClient httpClient = initHttpClient(httpTimeout, httpPoolSize, connectionsPerRoute);
            TokenClient tokenClient;
            if (config.getClientId() != null) {
                HELPER.check(StorageClientException.class, isNullOrEmpty(config.getClientId()), MSG_ERR_PASS_CLIENT_ID);
                HELPER.check(StorageClientException.class, isNullOrEmpty(config.getClientSecret()), MSG_ERR_PASS_CLIENT_SECRET);
                tokenClient = new OAuthTokenClient(config.getDefaultAuthEndpoint(),
                        config.getAuthEndpoints(),
                        config.getEnvironmentId(),
                        config.getClientId(),
                        config.getClientSecret(),
                        httpClient,
                        retryBaseDelay, retryMaxDelay);
                tokenClient = ProxyUtils.createLoggingProxyForPublicMethods(tokenClient, true);
            } else {
                OauthTokenAccessor accessor = config.getOauthTokenAccessor();
                tokenClient = (force, audience, region) -> {
                    try {
                        return accessor.getToken();
                    } catch (Exception ex) {
                        LOG.error(MSG_ERR_UNEXPECTED, ex);
                        throw new StorageServerException(MSG_ERR_UNEXPECTED, ex);
                    }
                };
            }

            HttpExecutor httpExecutor = ProxyUtils.createLoggingProxyForPublicMethods(
                    new HttpExecutorImpl(
                            ProxyUtils.createLoggingProxyForPublicMethods(tokenClient, true),
                            config.getEnvironmentId(),
                            httpClient, retryBaseDelay, retryMaxDelay), false);

            return new HttpDaoImpl(
                    config.getEndPoint(),
                    config.getEndpointMask(),
                    config.getCountriesEndpoint(),
                    httpExecutor);
        } else {
            return dao;
        }
    }

    private static void checkPositiveOrNull(Integer intValue, String errorMessage) throws StorageClientException {
        boolean isInvalidParam = intValue != null && intValue < 1;
        HELPER.check(StorageClientException.class, isInvalidParam, errorMessage, intValue);
    }

    private static void checkFileNameAndMimeType(String fileName, String mimeType) throws StorageClientException {
        boolean isInvalidParams = isNullOrEmpty(fileName) && isNullOrEmpty(mimeType);
        HELPER.check(StorageClientException.class, isInvalidParams, MSG_ERR_NULL_FILE_NAME_AND_MIME_TYPE);
    }

    private void checkCountryAndRecordKey(String country, String key) throws StorageClientException {
        HELPER.check(StorageClientException.class, isNullOrEmpty(country), MSG_ERR_NULL_COUNTRY);
        HELPER.check(StorageClientException.class, isNullOrEmpty(key), MSG_ERR_NULL_KEY);
    }

    private void checkAttachmentParameters(String country, String key, String fileId) throws StorageClientException {
        boolean isInvalidFileId = isNullOrEmpty(fileId);
        HELPER.check(StorageClientException.class, isInvalidFileId, MSG_ERR_NULL_FILE_ID);
        checkCountryAndRecordKey(country, key);
    }

    public Record write(String country, Record newRecord) throws StorageClientException, StorageServerException, StorageCryptoException {
        HELPER.check(StorageClientException.class, newRecord == null, MSG_ERR_NULL_RECORD);
        checkCountryAndRecordKey(country, newRecord.getRecordKey());
        TransferRecord recordedTransferRecord = dao.createRecord(country, transformer.getTransferRecord(newRecord));
        if (recordedTransferRecord == null) {
            return newRecord;
        }
        Record recorderRecord = transformer.getRecord(recordedTransferRecord);
        HELPER.check(StorageServerException.class, !newRecord.equals(recorderRecord), MSG_ERR_RESPONSE);
        return recorderRecord;
    }

    public Record read(String country, String recordKey) throws StorageClientException, StorageServerException, StorageCryptoException {
        checkCountryAndRecordKey(country, recordKey);
        TransferRecord transferRecord = dao.read(country, hashUtils.getSha256Hash(recordKey));
        return transformer.getRecord(transferRecord);
    }

    public MigrateResult migrate(String country, int limit) throws
            StorageClientException, StorageServerException, StorageCryptoException {
        HELPER.check(StorageClientException.class, transformer.getKeyAccessor() == null, MSG_ERR_MIGR_NOT_SUPPORT);
        HELPER.check(StorageClientException.class, limit < 1, MSG_ERR_MIGR_ERROR_LIMIT);
        FindFilter builder = new FindFilter()
                .limitAndOffset(limit, 0)
                .keyNotEq(NumberField.VERSION, Long.valueOf(transformer.getKeyAccessor().getSecretsData().getCurrentSecret().getVersion()));
        FindResult findResult = find(country, builder);
        if (!findResult.getRecords().isEmpty()) {
            batchWrite(country, findResult.getRecords());
        }
        return new MigrateResult(findResult.getRecords().size(),
                findResult.getTotal() - findResult.getRecords().size(),
                findResult.getErrors());
    }

    public List<Record> batchWrite(String country, List<Record> records)
            throws StorageClientException, StorageServerException, StorageCryptoException {
        boolean isInvalidList = records == null || records.isEmpty();
        HELPER.check(StorageClientException.class, isInvalidList, MSG_ERR_NULL_BATCH);
        for (Record currentRecord : records) {
            HELPER.check(StorageClientException.class, currentRecord == null, MSG_ERR_NULL_RECORD);
            checkCountryAndRecordKey(country, currentRecord.getRecordKey());
        }
        TransferRecordList transferRecordList = dao.createBatch(country, transformer.getTransferRecordList(records));
        if (transferRecordList == null) {
            return records;
        }
        List<Record> recordedList = transformer.getRecordList(transferRecordList);
        for (Record oneRecord : recordedList) {
            boolean valid = oneRecord.equals(recordedList.stream().filter(two -> oneRecord.getRecordKey().equals(two.getRecordKey())).findFirst().orElse(null));
            HELPER.check(StorageServerException.class, !valid, MSG_ERR_RESPONSE);
        }
        return recordedList;
    }

    public boolean delete(String country, String recordKey) throws StorageClientException, StorageServerException {
        checkCountryAndRecordKey(country, recordKey);
        dao.delete(country, hashUtils.getSha256Hash(recordKey));
        return true;
    }

    public FindResult find(String country, FindFilter filter) throws StorageClientException, StorageServerException {
        HELPER.check(StorageClientException.class, isNullOrEmpty(country), MSG_ERR_NULL_COUNTRY);
        TransferFindResult transferFindResult = dao.find(country, transformer.getTransferFilterContainer(filter));
        return transformer.getFindResult(transferFindResult);
    }

    public Record findOne(String country, FindFilter filter) throws
            StorageClientException, StorageServerException {
        FindFilter newFilter = filter != null ? filter.copy() : new FindFilter();
        FindResult findResults = find(country, newFilter.limitAndOffset(1, 0));
        List<Record> records = findResults.getRecords();
        if (records.isEmpty()) {
            LOG.warn(MSG_FOUND_NOTHING);
            return null;
        }
        return records.get(0);
    }

    @Override
    public AttachmentMeta addAttachment(String country, String recordKey, InputStream fileInputStream, String fileName) throws StorageClientException, StorageServerException {
        return addAttachment(country, recordKey, fileInputStream, fileName, false, null);
    }

    @Override
    public AttachmentMeta addAttachment(String country, String recordKey, InputStream fileInputStream, String fileName, boolean upsert) throws StorageClientException, StorageServerException {
        return addAttachment(country, recordKey, fileInputStream, fileName, upsert, null);
    }

    @Override
    public AttachmentMeta addAttachment(String country, String recordKey, InputStream fileInputStream, String fileName, String mimeType) throws StorageClientException, StorageServerException {
        return addAttachment(country, recordKey, fileInputStream, fileName, false, mimeType);
    }

    @Override
    public AttachmentMeta addAttachment(String country, String recordKey, InputStream inputStream, String fileName, boolean upsert, String mimeType) throws StorageClientException, StorageServerException {
        checkCountryAndRecordKey(country, recordKey);
        try {
            boolean isInvalidStream = inputStream == null || inputStream.available() < 0;
            HELPER.check(StorageClientException.class, isInvalidStream, MSG_ERR_NULL_FILE_INPUT_STREAM);
        } catch (IOException ex) {
            LOG.error(MSG_ERR_NOT_AVAILABLE_FILE_INPUT_STREAM);
            throw new StorageClientException(MSG_ERR_NOT_AVAILABLE_FILE_INPUT_STREAM, ex);
        }
        return dao.addAttachment(country, hashUtils.getSha256Hash(recordKey), inputStream, fileName, upsert, mimeType);
    }

    @Override
    public boolean deleteAttachment(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        checkAttachmentParameters(country, recordKey, fileId);
        dao.deleteAttachment(country, hashUtils.getSha256Hash(recordKey), fileId);
        return true;
    }

    @Override
    public AttachedFile getAttachmentFile(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        checkAttachmentParameters(country, recordKey, fileId);
        return dao.getAttachmentFile(country, hashUtils.getSha256Hash(recordKey), fileId);
    }

    @Override
    public AttachmentMeta updateAttachmentMeta(String country, String recordKey, String fileId, String fileName, String mimeType) throws StorageClientException, StorageServerException {
        checkFileNameAndMimeType(fileName, mimeType);
        checkAttachmentParameters(country, recordKey, fileId);
        AttachmentMeta updatedMeta = new AttachmentMeta();
        if (!isNullOrEmpty(fileName)) {
            updatedMeta.setFilename(fileName);
        }
        if (!isNullOrEmpty(mimeType)) {
            updatedMeta.setMimeType(mimeType);
        }
        return dao.updateAttachmentMeta(country, hashUtils.getSha256Hash(recordKey), fileId, updatedMeta);
    }

    @Override
    public AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        checkAttachmentParameters(country, recordKey, fileId);
        return dao.getAttachmentMeta(country, hashUtils.getSha256Hash(recordKey), fileId);
    }

    @Override
    public boolean healthCheck(String country) throws StorageClientException, StorageServerException {
        HELPER.check(StorageClientException.class, isNullOrEmpty(country), MSG_ERR_NULL_COUNTRY);
        return dao.healthCheck(country);
    }
}
