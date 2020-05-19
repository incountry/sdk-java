package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.AuthClient;
import com.incountry.residence.sdk.tools.http.TokenGenerator;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DefaultTokenGenerator implements TokenGenerator {

    private static final Logger LOG = LogManager.getLogger(DefaultTokenGenerator.class);
    private static final String MSG_REFRESH_TOKEN = "refreshToken force={}, audienceUrl={}";

    private final String apiKey;
    private final Map<String, Map.Entry<String, Long>> tokenMap = new HashMap<>();
    private final AuthClient authClient;

    public DefaultTokenGenerator(final AuthClient authClient) {
        this.authClient = ProxyUtils.createLoggingProxyForPublicMethods(authClient);
        this.apiKey = null;
    }

    public DefaultTokenGenerator(final String apiKey) {
        this.apiKey = apiKey;
        this.authClient = null;
    }

    @Override
    public String getToken(String audienceUrl) throws StorageServerException {
        if (authClient != null) {
            return refreshToken(false, audienceUrl);
        } else {
            return apiKey;
        }
    }

    public synchronized String refreshToken(boolean force, String audienceUrl) throws StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace(MSG_REFRESH_TOKEN, force, audienceUrl);
        }
        Map.Entry<String, Long> token = tokenMap.get(audienceUrl);
        if (force || token == null || token.getValue() < System.currentTimeMillis()) {
            token = authClient.newToken(audienceUrl);
            tokenMap.put(audienceUrl, token);
        }
        return token.getKey();
    }

    @Override
    public boolean canRefreshToken() {
        return apiKey == null;
    }
}
