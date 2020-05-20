package com.incountry.residence.sdk.http.mocks;

import com.incountry.residence.sdk.tools.http.AuthClient;

import java.util.AbstractMap;
import java.util.Map;

public class FakeAuthClient implements AuthClient {

    private final long expireTimeSeconds;

    public FakeAuthClient(long expireTimeSeconds) {
        this.expireTimeSeconds = expireTimeSeconds;
    }

    @Override
    public void setCredentials(String clientId, String secret, String authUrl, String scope) {
    }

    @Override
    public Map.Entry<String, Long> newToken(String audienceUrl) {
        return new AbstractMap.SimpleEntry<>(FakeAuthClient.class.getSimpleName(), System.currentTimeMillis() + expireTimeSeconds * 1_000L);
    }
}
