package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.http.AuthClient;

import java.util.AbstractMap;
import java.util.Map;

public class HttpAuthClient implements AuthClient {
    private String clientId;
    private String clientSecret;
    private String authUrl;


    @Override
    public void setCredentials(String clientId, String secret, String authUrl) {
        this.clientId = clientId;
        this.clientSecret = secret;
        this.authUrl = authUrl;
    }

    @Override
    public Map.Entry<String, Long> newToken() {
        return new AbstractMap.SimpleEntry<>(clientId + authUrl + clientSecret, System.currentTimeMillis());
    }
}
