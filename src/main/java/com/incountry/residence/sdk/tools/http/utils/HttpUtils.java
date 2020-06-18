package com.incountry.residence.sdk.tools.http.utils;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpUtils {

    public static HttpRequestFactory provideHttpRequestFactory(int poolSize) throws StorageServerException {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        try {
            cm.setMaxTotal(poolSize);
        } catch (IllegalArgumentException ex) {
            throw new StorageServerException("Illegal connections pool size.", ex);
        }

        final CloseableHttpClient httpClient = HttpClients.createMinimal(cm);
        final HttpTransport httpTransport = new ApacheHttpTransport(httpClient);
        return httpTransport.createRequestFactory();
    }

    public static HttpRequest buildRequest(HttpRequestFactory requestFactory, String url, String method, String body, Integer timeoutInMs) throws IOException, IllegalArgumentException {
        HttpContent requestContent = new ByteArrayContent("application/json", body.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = requestFactory
                .buildRequest(method, new GenericUrl(url), method.equals("POST") ? requestContent : null)
                .setConnectTimeout(timeoutInMs)
                .setReadTimeout(timeoutInMs);
        return request;
    }
}
