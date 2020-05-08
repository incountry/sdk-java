package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.http.AuthClient;

import java.util.AbstractMap;
import java.util.Map;

public class FakeAuthClient implements AuthClient {

    private long expireTimeSecs;

    public FakeAuthClient(long expireTimeSecs) {
        this.expireTimeSecs = expireTimeSecs;
    }

    @Override
    public void setCredentials(String clientId, String secret, String authUrl) {
    }

    @Override
    public Map.Entry<String, Long> newToken() {
        return new AbstractMap.SimpleEntry<>(FakeAuthClient.class.getSimpleName(), System.currentTimeMillis() + expireTimeSecs * 1_000L);
    }
}
