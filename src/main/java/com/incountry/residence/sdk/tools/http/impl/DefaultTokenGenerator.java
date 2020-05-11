package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.AuthClient;
import com.incountry.residence.sdk.tools.http.TokenGenerator;

import java.util.Map;

public class DefaultTokenGenerator implements TokenGenerator {

    private volatile String token;
    private volatile Long tokenExpire;
    private final AuthClient authClient;

    public DefaultTokenGenerator(final AuthClient authClient) {
        this.authClient = authClient;
        this.tokenExpire = System.currentTimeMillis() - 1L;
    }

    public DefaultTokenGenerator(final String token) {
        this.token = token;
        this.authClient = null;
    }

    @Override
    public String getToken() throws StorageServerException {
        if (authClient != null) {
            refreshToken(false);
        }
        return token;
    }

    public synchronized void refreshToken(boolean force) throws StorageServerException {
        if (force || tokenExpire < System.currentTimeMillis()) {
            Map.Entry<String, Long> newToken = authClient.newToken();
            token = newToken.getKey();
            tokenExpire = newToken.getValue();
        }
    }
}
