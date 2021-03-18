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
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.ApiKeyTokenClient;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
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

import static com.incountry.residence.sdk.tools.ValidationHelper.isNullOrEmpty;

/**
 * Basic implementation
 */
public class StorageImpl implements Storage {
    private static final Logger LOG = LogManager.getLogger(StorageImpl.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);
    //error messages
    private static final String MSG_ERR_PASS_ENV = "Please pass environment_id param or set INC_ENVIRONMENT_ID env var";
    private static final String MSG_ERR_AUTH = "Please pass only one parameter combination for authorization: clientId/clientSecret or apiKey or oauthTokenAccessor";
    private static final String MSG_ERR_PASS_API_KEY = "Please pass api_key param or set INC_API_KEY env var";
    private static final String MSG_ERR_NULL_BATCH = "Can't write empty batch";
    private static final String MSG_ERR_NULL_COUNTRY = "Country can't be null";
    private static final String MSG_ERR_NULL_FILE_ID = "File ID can't be null";
    private static final String MSG_ERR_NULL_KEY = "Key can't be null";
    private static final String MSG_ERR_NULL_FILTERS = "Filters can't be null";
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

    private static final int DEFAULT_HTTP_TIMEOUT = 30;
    private static final int DEFAULT_MAX_HTTP_CONNECTIONS = 20;

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
        alternativeAuthCount += config.getApiKey() != null ? 1 : 0;
        alternativeAuthCount += config.getClientId() != null ? 1 : 0;
        alternativeAuthCount += config.getOauthTokenAccessor() != null ? 1 : 0;
        HELPER.check(StorageClientException.class, alternativeAuthCount != 1, MSG_ERR_AUTH);

        this.dao = initDao(config, dao);
        this.hashUtils = new HashUtils(config.getEnvironmentId(), config.isNormalizeKeys());
        CryptoProvider cryptoProvider = config.getCryptoProvider() == null ? new CryptoProvider(null) : config.getCryptoProvider();
        if (config.getSecretKeyAccessor() != null) {
            boolean invalidAccessor;
            try {
                invalidAccessor = config.getSecretKeyAccessor().getSecretsData() == null;
            } catch (Exception ex) {
                LOG.error(MSG_ERR_UNEXPECTED, ex);
                throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
            }
            HELPER.check(StorageClientException.class, invalidAccessor, MSG_ERR_NULL_SECRETS);
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
    public static Storage newStorage(StorageConfig config) throws StorageClientException, StorageCryptoException {
        return newStorage(config, null);
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
    public static Storage newStorage(StorageConfig config, Dao dao) throws StorageClientException, StorageCryptoException {
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

    private static Dao initDao(StorageConfig config, Dao dao) throws StorageClientException {
        if (dao == null) {
            Integer httpTimeout = config.getHttpTimeout();
            Integer httpPoolSize = config.getMaxHttpPoolSize();
            Integer connectionsPerRoute = config.getMaxHttpConnectionsPerRoute();
            checkPositiveOrNull(httpTimeout, MSG_ERR_ILLEGAL_TIMEOUT);
            checkPositiveOrNull(httpPoolSize, MSG_ERR_CONNECTION_POOL);
            checkPositiveOrNull(connectionsPerRoute, MSG_ERR_MAX_CONNECTIONS_PER_ROUTE);

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
                        httpClient
                );
                tokenClient = ProxyUtils.createLoggingProxyForPublicMethods(tokenClient, true);
            } else if (config.getApiKey() != null) {
                HELPER.check(StorageClientException.class, isNullOrEmpty(config.getApiKey()), MSG_ERR_PASS_API_KEY);
                tokenClient = new ApiKeyTokenClient(config.getApiKey());
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
            return new HttpDaoImpl(config.getEnvironmentId(),
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
        boolean invalidParams = intValue != null && intValue < 1;
        HELPER.check(StorageClientException.class, invalidParams, errorMessage, intValue);
    }

    private static void checkFileNameAndMimeType(String fileName, String mimeType) throws StorageClientException {
        boolean invalidParams = isNullOrEmpty(fileName) && isNullOrEmpty(mimeType);
        HELPER.check(StorageClientException.class, invalidParams, MSG_ERR_NULL_FILE_NAME_AND_MIME_TYPE);
    }

    private void checkCountryAndRecordKey(String country, String key) throws StorageClientException {
        HELPER.check(StorageClientException.class, isNullOrEmpty(country), MSG_ERR_NULL_COUNTRY);
        HELPER.check(StorageClientException.class, isNullOrEmpty(key), MSG_ERR_NULL_KEY);
    }

    private void checkAttachmentParameters(String country, String key, String fileId) throws StorageClientException {
        boolean invalidFileId = isNullOrEmpty(fileId);
        HELPER.check(StorageClientException.class, invalidFileId, MSG_ERR_NULL_FILE_ID);
        checkCountryAndRecordKey(country, key);
    }

    public Record write(String country, Record record) throws StorageClientException, StorageServerException, StorageCryptoException {
        HELPER.check(StorageClientException.class, record == null, MSG_ERR_NULL_RECORD);
        checkCountryAndRecordKey(country, record.getRecordKey());
        dao.createRecord(country, transformer.getTransferRecord(record));
        return record;
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
        boolean invalidList = records == null || records.isEmpty();
        HELPER.check(StorageClientException.class, invalidList, MSG_ERR_NULL_BATCH);
        for (Record record : records) {
            HELPER.check(StorageClientException.class, record == null, MSG_ERR_NULL_RECORD);
            checkCountryAndRecordKey(country, record.getRecordKey());
        }
        dao.createBatch(country, transformer.getTransferRecordList(records));
        return records;
    }

    public boolean delete(String country, String recordKey) throws StorageClientException, StorageServerException {
        checkCountryAndRecordKey(country, recordKey);
        dao.delete(country, hashUtils.getSha256Hash(recordKey));
        return true;
    }

    public FindResult find(String country, FindFilter filter) throws StorageClientException, StorageServerException {
        HELPER.check(StorageClientException.class, isNullOrEmpty(country), MSG_ERR_NULL_COUNTRY);
        HELPER.check(StorageClientException.class, filter == null, MSG_ERR_NULL_FILTERS);
        TransferFindResult transferFindResult = dao.find(country, transformer.getTransferFilterContainer(filter));
        return transformer.getFindResult(transferFindResult);
    }

    public Record findOne(String country, FindFilter filter) throws
            StorageClientException, StorageServerException {
        FindResult findResults = find(country, filter != null ? filter.copy().limitAndOffset(1, 0) : null);
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
        return addAttachment(country, recordKey, fileInputStream, fileName, false, null);
    }

    @Override
    public AttachmentMeta addAttachment(String country, String recordKey, InputStream inputStream, String fileName, boolean upsert, String mimeType) throws StorageClientException, StorageServerException {
        checkCountryAndRecordKey(country, recordKey);
        try {
            boolean invalidStream = inputStream == null || inputStream.available() < 0;
            HELPER.check(StorageClientException.class, invalidStream, MSG_ERR_NULL_FILE_INPUT_STREAM);
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
}
