package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.http.TokenClient;

public class ApiKeyTokenClient implements TokenClient {

    private final String apiKey;

    public ApiKeyTokenClient(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getToken(String audienceUrl, String countryCode) {
        return apiKey;
    }

    @Override
    public String refreshToken(boolean force, String audienceUrl, String countryCode) {
        return apiKey;
    }
}
