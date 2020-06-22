package com.incountry.residence.sdk.tools.dao.impl;

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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.charset.Charset;
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
    private static final String STORAGE_URL = "/v2/storage/records/";
    private static final String URI_HTTPS = "https://";
    private static final String URI_POST = "POST";
    private static final String URI_GET = "GET";
    private static final String URI_DELETE = "DELETE";
    private static final String URI_FIND = "/find";
    private static final String URI_BATCH_WRITE = "/batchWrite";
    private static final String URI_DELIMITER = "/";
    private static final long DEFAULT_UPDATE_INTERVAL = 60_000;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final Map<String, POP> popMap = new HashMap<>();

    private final HttpAgent httpAgent;
    private final String endPointUrl;
    private final String endPointMask;
    private final String usingDefaultEndpointMask;
    private final boolean isDefaultEndpoint;
    private final String countriesEndpoint;
    private long lastLoadedTime;

    public HttpDaoImpl(String environmentId, String endPoint, String endpointMask, String countriesEndpoint, TokenClient tokenClient, Integer httpTimeout) throws StorageServerException {
        this(endPoint, endpointMask, countriesEndpoint,
                ProxyUtils.createLoggingProxyForPublicMethods(
                        new HttpAgentImpl(
                                ProxyUtils.createLoggingProxyForPublicMethods(tokenClient),
                                environmentId,
                                CHARSET,
                                httpTimeout)));
    }

    public HttpDaoImpl(String endPoint, String endpointMask, String countriesEndpoint, HttpAgent agent) throws StorageServerException {
        isDefaultEndpoint = (endPoint == null);
        this.endPointUrl = isDefaultEndpoint ? DEFAULT_ENDPOINT : endPoint;
        this.countriesEndpoint = countriesEndpoint == null ? DEFAULT_COUNTRY_ENDPOINT : countriesEndpoint;
        this.endPointMask = endpointMask;
        this.httpAgent = agent;
        this.usingDefaultEndpointMask = initUsingDefaultEndpoint(isDefaultEndpoint, endpointMask);
        if (isDefaultEndpoint) {
            loadCountries();
        }
    }

    private String initUsingDefaultEndpoint(boolean defaultEndpoint, String mask) {
        String resultMask = null;
        if (defaultEndpoint) {
            resultMask = mask != null ? mask : DEFAULT_ENDPOINT_MASK;
        }
        return resultMask;
    }

    private void loadCountries() throws StorageServerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start loading country list");
        }
        String content;
        synchronized (popMap) {
            popMap.clear();
            content = httpAgent.request(countriesEndpoint, URI_GET, null, ApiResponse.COUNTRY, null, null, RETRY_CNT);
            popMap.putAll(JsonUtils.getMidiPops(content, URI_HTTPS, usingDefaultEndpointMask));
            lastLoadedTime = System.currentTimeMillis();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded country list: {}", popMap.keySet());
        }
    }

    private EndPoint getEndpoint(String country) throws StorageServerException {
        if (isDefaultEndpoint) {
            //update country list cache every 1 min
            POP pop;
            synchronized (popMap) {
                if (System.currentTimeMillis() - lastLoadedTime > DEFAULT_UPDATE_INTERVAL) {
                    loadCountries();
                }
                pop = popMap.get(country);
            }
            if (pop != null) { //mid pop for default endpoint
                return new EndPoint(pop.getHost(), pop.getHost(), pop.getRegion(DEFAULT_REGION));
            } else if (endPointMask != null) {
                String mainUrl = URI_HTTPS + DEFAULT_COUNTRY + usingDefaultEndpointMask;
                return new EndPoint(mainUrl, getAudienceForMiniPop(mainUrl, country), DEFAULT_REGION);
            }
        }
        return new EndPoint(endPointUrl, getAudienceForMiniPop(endPointUrl, country), DEFAULT_REGION);
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
        httpAgent.request(url, URI_POST, body, ApiResponse.WRITE, endPoint.audience, endPoint.region, RETRY_CNT);
    }

    @Override
    public void createBatch(List<Record> records, String country, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException {
        String lowerCountry = country.toLowerCase();
        String recListJson = JsonUtils.toJsonString(records, cryptoManager);
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endPoint.mainUrl, lowerCountry, URI_BATCH_WRITE);
        httpAgent.request(url, URI_POST, recListJson, ApiResponse.BATCH_WRITE, endPoint.audience, endPoint.region, RETRY_CNT);
    }

    @Override
    public Record read(String country, String recordKey, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException {
        String lowerCountry = country.toLowerCase();
        String key = cryptoManager != null ? cryptoManager.createKeyHash(recordKey) : recordKey;
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, key);
        String response = httpAgent.request(url, URI_GET, null, ApiResponse.READ, endPoint.audience, endPoint.region, RETRY_CNT);
        if (response == null) {
            return null;
        } else {
            return JsonUtils.recordFromString(response, cryptoManager);
        }
    }

    @Override
    public void delete(String country, String key, CryptoManager cryptoManager) throws StorageServerException {
        String lowerCountry = country.toLowerCase();
        String recordHash = cryptoManager != null ? cryptoManager.createKeyHash(key) : key;
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, recordHash);
        httpAgent.request(url, URI_DELETE, null, ApiResponse.DELETE, endPoint.audience, endPoint.region, RETRY_CNT);
    }

    @Override
    public BatchRecord find(String country, FindFilterBuilder builder, CryptoManager cryptoManager) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endpoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endpoint.mainUrl, lowerCountry, URI_FIND);
        String postData = JsonUtils.toJsonString(builder.build(), cryptoManager);
        String content = httpAgent.request(url, URI_POST, postData, ApiResponse.FIND, endpoint.audience, endpoint.region, RETRY_CNT);
        if (content == null) {
            return new BatchRecord(new ArrayList<>(), 0, 0, 0, 0, null);
        }
        return JsonUtils.batchRecordFromString(content, cryptoManager);
    }

    private String getRecordUrl(String endPoint, String country, String keyHash) {
        return new StringBuilder(endPoint)
                .append(STORAGE_URL)
                .append(country)
                .append(URI_DELIMITER)
                .append(keyHash).toString();
    }

    private String getRecordActionUrl(String endpoint, String country, String... other) {
        StringBuilder builder = new StringBuilder(endpoint).append(STORAGE_URL);
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
