package com.incountry.residence.sdk.tools.http.utils;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpConnection {

    private static final String CONNECTION_PULL_ERROR = "Illegal connections pool size. Pool size must be not null, zero or negative.";
    private static final String URL_ERROR = "Url error";
    private static final String SERVER_ERROR = "Server request error: %s";
    private static final String NULL_BODY = "Body must not be null";
    private static final String POST = "POST";
    private static final String GET = "GET";

    private CloseableHttpClient httpClient;

    public CloseableHttpClient buildHttpClient(Integer timeout, Integer poolSize) throws StorageServerException {
        if (httpClient != null) {
            return httpClient;
        }
        if (poolSize == null || poolSize < 0 || poolSize == 0) {
            throw new StorageServerException(CONNECTION_PULL_ERROR);
        }
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(poolSize);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return httpClient;
    }

    public HttpRequestBase createRequest(String url, String method, String body) throws UnsupportedEncodingException, StorageServerException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            throw new StorageServerException(URL_ERROR, ex);
        }

        if (method.equals(POST)) {
            if (body == null) {
                throw new StorageServerException(String.format(SERVER_ERROR, method), new NullPointerException(NULL_BODY));
            }
            HttpPost request = new HttpPost(uri);
            StringEntity entity = new StringEntity(body);
            request.setEntity(entity);
            return request;
        } else if (method.equals(GET)) {
            return new HttpGet(uri);
        } else {
            return new HttpDelete(uri);
        }
    }
}
