package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.utils.HttpConnection;

public class ApiKeyTokenClient implements TokenClient {

    private final String apiKey;

    public ApiKeyTokenClient(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getToken(String audience, String region) {
        return apiKey;
    }

    @Override
    public String refreshToken(boolean force, String audience, String region) {
        return apiKey;
    }

    @Override
    public HttpConnection getHttpConnection() {
        return new HttpConnection();
    }
}
