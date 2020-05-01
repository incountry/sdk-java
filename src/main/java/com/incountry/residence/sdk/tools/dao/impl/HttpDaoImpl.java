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

    public static final String DEFAULT_ENDPOINT = "https://us.api.incountry.io";

    private static final Logger LOG = LogManager.getLogger(HttpDaoImpl.class);
    private static final String MSG_ERROR_RESPONSE = "Response error: expected 'OK', but recieved: ";
    private static final String PORTAL_COUNTRIES_URI = "https://portal-backend.incountry.com/countries";
    private static final String URI_ENDPOINT_PART = ".api.incountry.io";
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
    private HttpAgent agent;
    private String endPoint = DEFAULT_ENDPOINT;
    private boolean defaultEndpoint = true;
    private long lastLoadedTime;

    public HttpDaoImpl(String apiKey, String environmentId, String endPoint) throws StorageServerException {
        this(endPoint, ProxyUtils.createLoggingProxyForPublicMethods(
                new HttpAgentImpl(apiKey, environmentId, CHARSET)));
    }

    public HttpDaoImpl(String endPoint, HttpAgent agent) throws StorageServerException {
        if (endPoint != null && !endPoint.equals(DEFAULT_ENDPOINT)) {
            this.endPoint = endPoint;
            this.defaultEndpoint = false;
        }
        this.agent = agent;
        if (defaultEndpoint) {
            loadCountries();
        }
    }

    private void loadCountries() throws StorageServerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start loading country list");
        }
        String content;

        synchronized (popMap) {
            popMap.clear();
            content = agent.request(PORTAL_COUNTRIES_URI, URI_GET, null, ApiResponse.COUNTRY);
            popMap.putAll(JsonUtils.getCountries(content, URI_HTTPS, URI_ENDPOINT_PART));
            lastLoadedTime = System.currentTimeMillis();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded country list: {}", popMap.keySet());
        }
    }

    private String getEndpoint(String path, String country) throws StorageClientException, StorageServerException {
        if (!path.startsWith(URI_DELIMITER)) {
            path = URI_DELIMITER + path;
        }
        if (defaultEndpoint) {
            //update country list cache every 1 min
            POP pop;
            synchronized (popMap) {
                if (System.currentTimeMillis() - lastLoadedTime > DEFAULT_UPDATE_INTERVAL) {
                    loadCountries();
                }
                pop = popMap.get(country.toLowerCase());
            }
            if (pop == null) {
                loadCountries();
                synchronized (popMap) {
                    pop = popMap.get(country.toLowerCase());
                }
            }
            if (pop == null) {
                String message = "Country " + country + " has no PoPAPI";
                LOG.error(message);
                throw new StorageClientException(message);
            }
            return pop.getHost() + path;
        } else {
            return endPoint + path;
        }
    }

    private String createUrl(String country, String keyHash) throws StorageClientException, StorageServerException {
        return getEndpoint(concatUrl(country, URI_DELIMITER, keyHash), country);
    }

    @Override
    public void createRecord(String country, Record record, CryptoManager cryptoManager) throws StorageClientException, StorageCryptoException, StorageServerException {
        String url = getEndpoint(concatUrl(country), country);
        String response = agent.request(url, URI_POST, JsonUtils.toJsonString(record, cryptoManager), ApiResponse.WRITE);
        validatePlainTextResponse("ok", response);
    }

    @Override
    public void createBatch(List<Record> records, String country, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException {
        String recListJson = JsonUtils.toJsonString(records, cryptoManager);
        String url = getEndpoint(concatUrl(country, URI_BATCH_WRITE), country);
        String response = agent.request(url, URI_POST, recListJson, ApiResponse.BATCH_WRITE);
        validatePlainTextResponse("ok", response);
    }

    @Override
    public Record read(String country, String recordKey, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException {
        String key = cryptoManager != null ? cryptoManager.createKeyHash(recordKey) : recordKey;
        String url = createUrl(country, key);
        String response = agent.request(url, URI_GET, null, ApiResponse.READ);
        if (response == null) {
            return null;
        } else {
            return JsonUtils.recordFromString(response, cryptoManager);
        }
    }

    @Override
    public void delete(String country, String key, CryptoManager cryptoManager) throws StorageClientException, StorageServerException {
        String newKey = cryptoManager != null ? cryptoManager.createKeyHash(key) : key;
        String url = createUrl(country, newKey);
        String response = agent.request(url, URI_DELETE, null, ApiResponse.DELETE);
        validatePlainTextResponse("{}", response);
    }

    @Override
    public BatchRecord find(String country, FindFilterBuilder builder, CryptoManager cryptoManager) throws StorageClientException, StorageServerException {
        String url = getEndpoint(concatUrl(country, URI_FIND), country);
        String postData = JsonUtils.toJsonString(builder.build(), cryptoManager);
        String content = agent.request(url, URI_POST, postData, ApiResponse.FIND);
        if (content == null) {
            return new BatchRecord(new ArrayList<>(), 0, 0, 0, 0, null);
        }
        return JsonUtils.batchRecordFromString(content, cryptoManager);
    }

    private String concatUrl(String country, String... other) {
        StringBuilder builder = new StringBuilder(STORAGE_URL);
        builder.append(country.toLowerCase());
        if (other != null) {
            for (String one : other) {
                builder.append(one);
            }
        }
        return builder.toString();
    }

    private void validatePlainTextResponse(String expected, String response) throws StorageServerException {
        if (response == null || !response.equalsIgnoreCase(expected)) {
            String message = MSG_ERROR_RESPONSE + response;
            LOG.error(message);
            throw new StorageServerException(message);
        }
    }
}
