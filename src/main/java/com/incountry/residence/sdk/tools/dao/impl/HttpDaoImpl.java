package com.incountry.residence.sdk.tools.dao.impl;

import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.tools.containers.MetaInfoTypes;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class HttpDaoImpl implements Dao {

    private static final Logger LOG = LogManager.getLogger(HttpDaoImpl.class);

    private static final String DEFAULT_ENDPOINT = "https://us-mt-01.api.incountry.io";
    private static final int RETRY_CNT = 1;
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
    private static final long DEFAULT_UPDATE_INTERVAL = 300_000;
    private static final String MSG_ERR_LOAD_COUNTRIES = "Error during country list loading";
    private static final String MSG_ERR_COUNTRIES_ARE_EMPTY = "Country list is empty";

    private Map<String, POP> popMap = new HashMap<>();

    private final HttpAgent httpAgent;
    private final String endPointUrl;
    private final String endPointMask;
    private final boolean isDefaultEndpoint;
    private final String countriesEndpoint;
    private final AtomicLong lastLoadedTime = new AtomicLong(0);

    public HttpDaoImpl(String environmentId, String endPoint, String endpointMask, String countriesEndpoint, TokenClient tokenClient, CloseableHttpClient httpClient) {
        this(endPoint, endpointMask, countriesEndpoint,
                ProxyUtils.createLoggingProxyForPublicMethods(
                        new HttpAgentImpl(
                                ProxyUtils.createLoggingProxyForPublicMethods(tokenClient),
                                environmentId,
                                httpClient)));
    }

    public HttpDaoImpl(String endPoint, String endpointMask, String countriesEndpoint, HttpAgent agent) {
        isDefaultEndpoint = (endPoint == null);
        this.endPointUrl = isDefaultEndpoint ? DEFAULT_ENDPOINT : endPoint;
        this.countriesEndpoint = countriesEndpoint == null ? DEFAULT_COUNTRY_ENDPOINT : countriesEndpoint;
        this.endPointMask = endpointMask;
        this.httpAgent = agent;
        if (isDefaultEndpoint) {
            loadCountries();
        }
    }

    private void loadCountries() {
        //update country list cache every 5 min
        if (System.currentTimeMillis() - lastLoadedTime.get() < DEFAULT_UPDATE_INTERVAL) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start loading country list");
        }
        synchronized (lastLoadedTime) {
            if (System.currentTimeMillis() - lastLoadedTime.get() < DEFAULT_UPDATE_INTERVAL) {
                return;
            }
            try {
                ApiResponse response = httpAgent.request(countriesEndpoint, null, null, null, RETRY_CNT, new RequestParameters(URI_GET, ApiResponseCodes.COUNTRY));
                String content = response.getContent();
                ConcurrentHashMap<String, POP> newCountryMap = new ConcurrentHashMap<>(JsonUtils.getMidiPops(content, URI_HTTPS, endPointMask != null ? endPointMask : DEFAULT_ENDPOINT_MASK));
                if (newCountryMap.size() > 0) {
                    popMap = newCountryMap;
                }
            } catch (StorageException ex) {
                LOG.error(MSG_ERR_LOAD_COUNTRIES, ex);
            }
            lastLoadedTime.set(System.currentTimeMillis());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded country list: {}", popMap.keySet());
        }
    }

    private EndPoint getEndpoint(String country) throws StorageServerException {
        if (isDefaultEndpoint) {
            POP pop = getMidPop(country);
            if (pop != null) { //mid pop for default endpoint
                return new EndPoint(pop.getHost(), pop.getHost(), pop.getRegion(DEFAULT_REGION));
            }
            String mainUrl = URI_HTTPS + DEFAULT_COUNTRY + (endPointMask == null ? DEFAULT_ENDPOINT_MASK : endPointMask);
            return new EndPoint(mainUrl, getAudienceForMiniPop(mainUrl, country), DEFAULT_REGION);
        }
        return new EndPoint(endPointUrl, getAudienceForMiniPop(endPointUrl, country), DEFAULT_REGION);
    }

    private POP getMidPop(String country) throws StorageServerException {
        loadCountries();
        Map<String, POP> tempMap = popMap;
        if (popMap.isEmpty()) {
            throw new StorageServerException(MSG_ERR_COUNTRIES_ARE_EMPTY);
        }
        return tempMap.get(country);
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
        httpAgent.request(url, body, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_POST, ApiResponseCodes.WRITE));
    }

    @Override
    public void createBatch(List<Record> records, String country, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException {
        String lowerCountry = country.toLowerCase();
        String recListJson = JsonUtils.toJsonString(records, cryptoManager);
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endPoint.mainUrl, lowerCountry, URI_BATCH_WRITE);
        httpAgent.request(url, recListJson, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_POST, ApiResponseCodes.BATCH_WRITE));
    }

    @Override
    public Record read(String country, String recordKey, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, recordKey);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_GET, ApiResponseCodes.READ));
        return response.getContent() == null ? null : JsonUtils.recordFromString(response.getContent(), cryptoManager);
    }

    @Override
    public void delete(String country, String recordKey) throws StorageServerException, StorageClientException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, recordKey);
        httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_DELETE, ApiResponseCodes.DELETE));
    }

    @Override
    public BatchRecord find(String country, FindFilter findFilter, CryptoManager cryptoManager) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endpoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endpoint.mainUrl, lowerCountry, URI_FIND);
        String postData = JsonUtils.toJsonString(findFilter, cryptoManager);
        ApiResponse response = httpAgent.request(url, postData, endpoint.audience, endpoint.region, RETRY_CNT, new RequestParameters(URI_POST, ApiResponseCodes.FIND));
        if (response.getContent() == null) {
            return new BatchRecord(new ArrayList<>(), 0, 0, 0, 0, null);
        }
        return JsonUtils.batchRecordFromString(response.getContent(), cryptoManager);
    }

    @Override
    public AttachmentMeta addAttachment(String country, String recordKey, InputStream inputStream, String fileName, boolean upsert, String mimeType) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS);
        String method = upsert ? URI_PUT : URI_POST;
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(method, ApiResponseCodes.ADD_ATTACHMENT, mimeType, inputStream, fileName));
        return JsonUtils.getDataFromAttachmentMetaJson(response.getContent());
    }

    @Override
    public void deleteAttachment(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId);
        httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_DELETE, ApiResponseCodes.DELETE_ATTACHMENT));
    }

    @Override
    public AttachedFile getAttachmentFile(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_GET, ApiResponseCodes.GET_ATTACHMENT_FILE));
        InputStream content = response.getContent() == null ? null : new ByteArrayInputStream(response.getContent().getBytes(StandardCharsets.UTF_8));
        String fileName = null;
        if (response.getMetaInfo() != null) {
            fileName = response.getMetaInfo().get(MetaInfoTypes.NAME);
        }
        return new AttachedFile(content, fileName);
    }

    @Override
    public AttachmentMeta updateAttachmentMeta(String country, String recordKey, String fileId, String fileName, String mimeType) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId, URI_META);
        ApiResponse response = httpAgent.request(url, JsonUtils.createUpdatedMetaJson(fileName, mimeType), endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_PATCH, ApiResponseCodes.UPDATE_ATTACHMENT_META));
        return JsonUtils.getDataFromAttachmentMetaJson(response.getContent());
    }

    @Override
    public AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId, URI_META);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_GET, ApiResponseCodes.GET_ATTACHMENT_META));
        return JsonUtils.getDataFromAttachmentMetaJson(response.getContent());
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
