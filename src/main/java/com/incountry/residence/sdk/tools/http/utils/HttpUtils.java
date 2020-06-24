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

public class HttpUtils {

    private HttpUtils() {

    }

    public static CloseableHttpClient buildHttpClient(Integer timeout, Integer poolSize) throws StorageServerException {
        if (poolSize == null || poolSize < 0 || poolSize == 0) {
            throw new StorageServerException("Illegal connections pool size. Pool size must be not null, zero or negative.");
        }
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(poolSize);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static HttpRequestBase createRequest(String url, String method, String body) throws UnsupportedEncodingException, StorageServerException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            throw new StorageServerException("Url error.", ex);
        }

        if (method.equals("POST")) {
            if (body == null) {
                throw new StorageServerException("Server request error: POST", new NullPointerException("Body must not be null."));
            }
            HttpPost request = new HttpPost(uri);
            StringEntity entity = new StringEntity(body);
            request.setEntity(entity);
            return request;
        } else if (method.equals("GET")) {
            return new HttpGet(uri);
        } else {
            return new HttpDelete(uri);
        }
    }
}
