package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.util.Map;

public interface AuthClient {

    void setCredentials(String clientId, String secret, String authUrl, String scope);

    Map.Entry<String, Long> newToken(String audienceUrl) throws StorageServerException;
}
