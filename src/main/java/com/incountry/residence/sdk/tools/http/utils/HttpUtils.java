package com.incountry.residence.sdk.tools.http.utils;

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

public class HttpUtils {

    private HttpUtils() {

    }

    public static CloseableHttpClient buildHttpClient(Integer timeout, Integer poolSize) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(poolSize);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .build();
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static HttpRequestBase createRequest(String url, String method, String body) throws UnsupportedEncodingException {
        if (method.equals("POST")) {
            HttpPost request = new HttpPost(url);
            StringEntity entity = new StringEntity(body);
            request.setEntity(entity);
            return request;
        } else if (method.equals("GET")) {
            return new HttpGet(url);
        } else {
            return new HttpDelete(url);
        }
    }
}
