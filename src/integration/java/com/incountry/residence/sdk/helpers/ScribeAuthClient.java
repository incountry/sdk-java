package com.incountry.residence.sdk.helpers;


import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.AuthClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.Map;

public class ScribeAuthClient implements AuthClient {

    private static final Logger LOG = LogManager.getLogger(ScribeAuthClient.class);

    private static final String MSG_ERR_TOKEN = "Exception while getting token";
    private static final String MSG_ERR_INVALID_TOKEN = "Authorization server returned expired token [token=%s, expireTime=%s]";
    private String authUrl = System.getenv("INT_INC_AUTH_ENDPOINT");
    private String clientId = System.getenv("INT_INC_CLIENT_ID");
    private String secret = System.getenv("INT_INC_CLIENT_SECRET");

    public ScribeAuthClient() {
    }

    public ScribeAuthClient(String login, String secret, String authUrl) {
        setCredentials(login, secret, authUrl);
    }

    public void setCredentials(String clientId, String secret, String authUrl) {
        this.clientId = clientId;
        this.secret = secret;
        this.authUrl = authUrl;
    }

    public Map.Entry<String, Long> newToken() throws StorageServerException {
        OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(secret)
                .build(new HydraAuthApi(authUrl));
        OAuth2AccessToken token;
        try {
            token = service.getAccessTokenClientCredentialsGrant();
        } catch (Exception e) {
            throw new StorageServerException(MSG_ERR_TOKEN, e);
        }
        if (token.getExpiresIn() == null || token.getExpiresIn() < 0) {
            String message = String.format(MSG_ERR_INVALID_TOKEN, token.getAccessToken(), token.getExpiresIn());
            LOG.error(message);
            throw new StorageServerException(message);
        }
        Long expiredTime = System.currentTimeMillis() + token.getExpiresIn() * 1_000L;
        return new AbstractMap.SimpleEntry<>(token.getAccessToken(), expiredTime);
    }

    private static class HydraAuthApi extends DefaultApi20 {
        String authUrl;

        HydraAuthApi(String authUrl) {
            this.authUrl = authUrl;
        }

        @Override
        public String getAccessTokenEndpoint() {
            return authUrl;
        }

        @Override
        protected String getAuthorizationBaseUrl() {
            return authUrl;
        }
    }

    public static void main(String... args) throws StorageServerException {
        ScribeAuthClient client = new ScribeAuthClient();
        Object o = client.newToken();
        LOG.debug(o);
    }
}
