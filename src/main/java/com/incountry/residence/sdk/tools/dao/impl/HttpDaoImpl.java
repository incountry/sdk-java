package com.incountry.residence.sdk.tools.dao.impl;

import com.incountry.residence.sdk.tools.models.CustomEnum;
import com.incountry.residence.sdk.tools.models.HttpParameters;
import com.incountry.residence.sdk.tools.models.ApiResponse;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpDaoImpl implements Dao {

    private static final String DEFAULT_ENDPOINT = "https://us-mt-01.api.incountry.io";
    private static final int RETRY_CNT = 1;

    private static final Logger LOG = LogManager.getLogger(HttpDaoImpl.class);
    private static final String DEFAULT_COUNTRY_ENDPOINT = "https://portal-backend.incountry.com/countries";
    private static final String DEFAULT_ENDPOINT_MASK = "-mt-01.api.incountry.io";
    private static final String DEFAULT_COUNTRY = "us";
    private static final String DEFAULT_REGION = "emea";
    private static final String STORAGE_URL = "v2/storage/records";
    private static final String URI_HTTPS = "https://";
    private static final String URI_POST = "POST";
    private static final String URI_PUT = "PUT";
    private static final String URI_PATCH = "PATCH";
    private static final String URI_GET = "GET";
    private static final String URI_DELETE = "DELETE";
    private static final String URI_FIND = "/find";
    private static final String URI_BATCH_WRITE = "/batchWrite";
    private static final String URI_META = "meta";
    private static final String URI_DELIMITER = "/";
    private static final String URI_ATTACHMENTS = "attachments";
    private static final String APPLICATION_JSON = "application/json";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final long DEFAULT_UPDATE_INTERVAL = 60_000;

    private static final String MSG_ERR_USER_INPUT_STREAM = "User's InputStream reading error";

    private final Map<String, POP> popMap = new HashMap<>();

    private final HttpAgent httpAgent;
    private final String endPointUrl;
    private final String endPointMask;
    private final boolean isDefaultEndpoint;
    private final String countriesEndpoint;
    private volatile long lastLoadedTime;

    public HttpDaoImpl(String environmentId, String endPoint, String endpointMask, String countriesEndpoint, TokenClient tokenClient, CloseableHttpClient httpClient) throws StorageServerException, StorageClientException {
        this(endPoint, endpointMask, countriesEndpoint,
                ProxyUtils.createLoggingProxyForPublicMethods(
                        new HttpAgentImpl(
                                ProxyUtils.createLoggingProxyForPublicMethods(tokenClient),
                                environmentId,
                                httpClient)));
    }

    public HttpDaoImpl(String endPoint, String endpointMask, String countriesEndpoint, HttpAgent agent) throws StorageServerException, StorageClientException {
        isDefaultEndpoint = (endPoint == null);
        this.endPointUrl = isDefaultEndpoint ? DEFAULT_ENDPOINT : endPoint;
        this.countriesEndpoint = countriesEndpoint == null ? DEFAULT_COUNTRY_ENDPOINT : countriesEndpoint;
        this.endPointMask = endpointMask;
        this.httpAgent = agent;
        if (isDefaultEndpoint) {
            synchronized (popMap) {
                loadCountries();
            }
        }
    }

    private void loadCountries() throws StorageServerException, StorageClientException {
        //update country list cache every 1 min
        if (System.currentTimeMillis() - lastLoadedTime < DEFAULT_UPDATE_INTERVAL) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start loading country list");
        }
        String content;
        popMap.clear();
        ApiResponse response = httpAgent.request(countriesEndpoint, null, null, null, RETRY_CNT, new HttpParameters(URI_GET, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.COUNTRY, APPLICATION_JSON));
        content = response.getContent();
        popMap.putAll(JsonUtils.getMidiPops(content, URI_HTTPS, endPointMask != null ? endPointMask : DEFAULT_ENDPOINT_MASK));
        lastLoadedTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded country list: {}", popMap.keySet());
        }
    }

    private EndPoint getEndpoint(String country) throws StorageServerException, StorageClientException {
        if (isDefaultEndpoint) {
            POP pop = getPopIfCountryIsMidPop(country);
            if (pop != null) { //mid pop for default endpoint
                return new EndPoint(pop.getHost(), pop.getHost(), pop.getRegion(DEFAULT_REGION));
            }
            String mainUrl = URI_HTTPS + DEFAULT_COUNTRY + (endPointMask == null ? DEFAULT_ENDPOINT_MASK : endPointMask);
            return new EndPoint(mainUrl, getAudienceForMiniPop(mainUrl, country), DEFAULT_REGION);
        }
        return new EndPoint(endPointUrl, getAudienceForMiniPop(endPointUrl, country), DEFAULT_REGION);
    }

    private POP getPopIfCountryIsMidPop(String country) throws StorageServerException, StorageClientException {
        synchronized (popMap) {
            loadCountries();
            return popMap.get(country);
        }
    }

    private String getAudienceForMiniPop(String mainUrl, String country) {
        String mask = endPointMask;
        if (isDefaultEndpoint && mask == null) {
            mask = DEFAULT_ENDPOINT_MASK;
        }
        if (mask == null) {
            return mainUrl;
        } else {
            String secondaryUrl = URI_HTTPS + country + mask;
            String resultAudience = mainUrl;
            if (!mainUrl.equals(secondaryUrl)) {
                resultAudience += " " + secondaryUrl;
            }
            return resultAudience;
        }
    }

    @Override
    public void createRecord(String country, Record record, CryptoManager cryptoManager) throws StorageClientException, StorageCryptoException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endPoint.mainUrl, lowerCountry);
        String body = JsonUtils.toJsonString(record, cryptoManager);
        httpAgent.request(url, body, endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(URI_POST, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.WRITE, APPLICATION_JSON));
    }

    @Override
    public void createBatch(List<Record> records, String country, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException {
        String lowerCountry = country.toLowerCase();
        String recListJson = JsonUtils.toJsonString(records, cryptoManager);
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endPoint.mainUrl, lowerCountry, URI_BATCH_WRITE);
        httpAgent.request(url, recListJson, endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(URI_POST, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.BATCH_WRITE, APPLICATION_JSON));
    }

    @Override
    public Record read(String country, String recordKey, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException {
        String lowerCountry = country.toLowerCase();
        String key = cryptoManager != null ? cryptoManager.createKeyHash(recordKey) : recordKey;
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, key);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(URI_GET, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.READ, APPLICATION_JSON));
        if (response.getContent() == null) {
            return null;
        } else {
            return JsonUtils.recordFromString(response.getContent(), cryptoManager);
        }
    }

    @Override
    public void delete(String country, String key, CryptoManager cryptoManager) throws StorageServerException, StorageClientException {
        String lowerCountry = country.toLowerCase();
        String recordHash = cryptoManager != null ? cryptoManager.createKeyHash(key) : key;
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, recordHash);
        httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(URI_DELETE, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.DELETE, APPLICATION_JSON));
    }

    @Override
    public BatchRecord find(String country, FindFilterBuilder builder, CryptoManager cryptoManager) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endpoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endpoint.mainUrl, lowerCountry, URI_FIND);
        String postData = JsonUtils.toJsonString(builder.build(), cryptoManager);
        ApiResponse response = httpAgent.request(url, postData, endpoint.audience, endpoint.region, RETRY_CNT, new HttpParameters(URI_POST, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.FIND, APPLICATION_JSON));
        if (response.getContent() == null) {
            return new BatchRecord(new ArrayList<>(), 0, 0, 0, 0, null);
        }
        return JsonUtils.batchRecordFromString(response.getContent(), cryptoManager);
    }

    @Override
    public String addAttachment(String country, String recordKey, InputStream fileInputStream, boolean upsert) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS);
        String method = upsert ? URI_PUT : URI_POST;
        String body;
        try {
            body = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8.name());
        } catch (IOException ex) {
            LOG.error(MSG_ERR_USER_INPUT_STREAM);
            throw new StorageClientException(MSG_ERR_USER_INPUT_STREAM, ex);
        }
        ApiResponse response = httpAgent.request(url, body, endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(method, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.WRITE, MULTIPART_FORM_DATA));
        return response.getContent();
    }

    @Override
    public void deleteAttachment(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId);
        httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(URI_DELETE, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.DELETE, APPLICATION_JSON));
    }

    @Override
    public AttachedFile getAttachmentFile(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(URI_GET, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.READ, APPLICATION_JSON));
        InputStream content = new ByteArrayInputStream(response.getContent().getBytes(StandardCharsets.UTF_8));
        String fileExtension = null;
        if (response.getMetaInfo().containsKey(CustomEnum.EXTENSION)) {
            fileExtension = response.getMetaInfo().get(CustomEnum.EXTENSION);
        }
        return new AttachedFile(content, fileExtension);
    }

    @Override
    public void updateAttachmentMeta(String country, String recordKey, String fileId, String fileName, String mimeType) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId, URI_META);
        httpAgent.request(url, JsonUtils.createUpdatedMetaJson(fileName, mimeType), endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(URI_PATCH, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.WRITE, APPLICATION_JSON));
    }

    @Override
    public AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId, URI_META);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new HttpParameters(URI_GET, com.incountry.residence.sdk.tools.dao.impl.ApiResponse.READ, APPLICATION_JSON));
        return (AttachmentMeta) JsonUtils.getDataFromJson(response.getContent(), AttachmentMeta.class);
    }

    private String getAttachmentUrl(String... urlParts) {
        return String.join(URI_DELIMITER, urlParts);
    }

    private String getRecordUrl(String endPoint, String country, String keyHash) {
        return new StringBuilder(endPoint)
                .append(URI_DELIMITER)
                .append(STORAGE_URL)
                .append(URI_DELIMITER)
                .append(country)
                .append(URI_DELIMITER)
                .append(keyHash).toString();
    }

    private String getRecordActionUrl(String endpoint, String country, String... other) {
        StringBuilder builder = new StringBuilder(endpoint).append(URI_DELIMITER).append(STORAGE_URL).append(URI_DELIMITER);
        builder.append(country);
        for (String part : other) {
            builder.append(part);
        }
        return builder.toString();
    }

    private static class EndPoint {
        String mainUrl;
        String audience;
        String region;

        EndPoint(String mainUrl, String audience, String region) {
            this.mainUrl = mainUrl;
            this.audience = audience;
            this.region = region;
        }
    }
}
