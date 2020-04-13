package com.incountry.residence.sdk.tools.dao.impl;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpDaoImpl implements Dao {

    private static final Logger LOG = LoggerFactory.getLogger(HttpDaoImpl.class);

    private static final String DEFAULT_ENDPOINT = "https://us.api.incountry.io";
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

    //error messages
    private static final String MSG_ERR_GET_COUNTRIES = "Unable to retrieve available countries list";
    private static final String MSG_SERVER_ERROR = "Server request error";


    private Map<String, POP> popMap;
    private HttpAgent agent;
    private String endPoint;
    private boolean defaultEndpoint = false;

    public HttpDaoImpl(String apiKey, String environmentId, String endPoint) throws StorageServerException {
        this(endPoint, new HttpAgentImpl(apiKey, environmentId, Charset.defaultCharset()));
    }

    public HttpDaoImpl(String endPoint, HttpAgent agent) throws StorageServerException {
        if (endPoint == null) {
            endPoint = DEFAULT_ENDPOINT;
            this.defaultEndpoint = true;
        }
        this.endPoint = endPoint;
        this.agent = agent;
        loadCounties();
    }

    private void loadCounties() throws StorageServerException {
        String content;
        try {
            content = agent.request(PORTAL_COUNTRIES_URI, URI_GET, null, false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_ERR_GET_COUNTRIES, e);
        }
        popMap = new HashMap<>();
        for (Map.Entry<String, String> pair : JsonUtils.getCountryEntryPoint(content)) {
            popMap.put(pair.getValue(), new POP(URI_HTTPS + pair.getKey() + URI_ENDPOINT_PART, pair.getValue()));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded county list: {}", popMap.keySet());
        }
    }

    private String getEndpoint(String path, String country) {
        if (!path.startsWith(URI_DELIMITER)) {
            path = URI_DELIMITER + path;
        }
        POP pop = popMap.get(country.toLowerCase());
        if (defaultEndpoint && pop != null) {
            return pop.getHost() + path;
        }
        return endPoint + path;
    }

    private String createUrl(String country, String recordKeyHash) {
        return getEndpoint(concatUrl(country, URI_DELIMITER, recordKeyHash), country);
    }

    @Override
    public void createRecord(String country, Record record, Crypto crypto) throws StorageCryptoException, StorageServerException {
        String url = getEndpoint(concatUrl(country), country);
        try {
            agent.request(url, URI_POST, JsonUtils.toJsonString(record, crypto), false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
        }
    }

    @Override
    public void createBatch(List<Record> records, String country, Crypto crypto) throws StorageServerException, StorageCryptoException {
        String recListJson = JsonUtils.toJsonString(records, crypto);
        String url = getEndpoint(concatUrl(country, URI_BATCH_WRITE), country);
        try {
            agent.request(url, URI_POST, recListJson, false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
        }
    }

    @Override
    public Record read(String country, String recordKey, Crypto crypto) throws StorageServerException, StorageCryptoException {
        String key = crypto != null ? crypto.createKeyHash(recordKey) : recordKey;
        String url = createUrl(country, key);
        String response;
        try {
            response = agent.request(url, URI_GET, null, true);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
        }
        if (response == null) {
            return null;
        } else {
            return JsonUtils.recordFromString(response, crypto);
        }
    }

    @Override
    public void delete(String country, String recordKey, Crypto crypto) throws StorageServerException {
        String key = crypto != null ? crypto.createKeyHash(recordKey) : recordKey;
        String url = createUrl(country, key);
        try {
            agent.request(url, URI_DELETE, null, false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
        }
    }

    @Override
    public BatchRecord find(String country, FindFilterBuilder builder, Crypto crypto) throws StorageServerException {
        String url = getEndpoint(concatUrl(country, URI_FIND), country);
        String postData = JsonUtils.toJsonString(builder.build(), crypto);
        String content;
        try {
            content = agent.request(url, URI_POST, postData, false);
        } catch (IOException e) {
            throw new StorageServerException(MSG_SERVER_ERROR, e);
        }
        if (content == null) {
            return new BatchRecord(new ArrayList<>(), 0, 0, 0, 0, null);
        }
        return JsonUtils.batchRecordFromString(content, crypto);
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
}
