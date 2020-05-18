package com.incountry.residence.sdk.helpers;


import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.AuthClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.Map;

import static com.github.scribejava.core.httpclient.HttpClient.DEFAULT_CONTENT_TYPE;
import static com.github.scribejava.core.httpclient.HttpClient.CONTENT_TYPE;


public class ScribeAuthClient implements AuthClient {

    private static final Logger LOG = LogManager.getLogger(ScribeAuthClient.class);

    private static final String MSG_ERR_TOKEN = "Exception while getting token";
    private static final String MSG_ERR_INVALID_TOKEN = "Authorization server returned expired token [token=%s, expireTime=%s, scope=%s]";
    private static final String AUDIENCE = "audience";
    private String authUrl;
    private String clientId;
    private String secret;
    private String scope;

    public void setCredentials(String clientId, String secret, String authUrl, String scope) {
        this.clientId = clientId;
        this.secret = secret;
        this.authUrl = authUrl;
        this.scope = scope;
    }

    public Map.Entry<String, Long> newToken(String audienceUrl) throws StorageServerException {
        DefaultAuthApi authApi = new DefaultAuthApi(authUrl);
        OAuthRequest request = new OAuthRequest(Verb.POST, authUrl);
        request.addHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        authApi.getClientAuthentication().addClientAuthentication(request, clientId, secret);
        request.addParameter(OAuthConstants.SCOPE, scope);
        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS);
        //can't set audience with cosy Service builder
        request.addParameter(AUDIENCE, audienceUrl);
        OAuth20Service service = new ServiceBuilder(clientId).build(authApi);
        OAuth2AccessToken token;
        try {
            token = authApi.getAccessTokenExtractor().extract(service.execute(request));
        } catch (Exception e) {
            throw new StorageServerException(MSG_ERR_TOKEN, e);
        }
        if (token.getExpiresIn() == null || token.getExpiresIn() < 0 || !scope.equals(token.getScope())) {
            String message = String.format(MSG_ERR_INVALID_TOKEN, token.getAccessToken(), token.getExpiresIn(), token.getScope());
            LOG.error(message);
            throw new StorageServerException(message);
        }
        Long expiredTime = System.currentTimeMillis() + token.getExpiresIn() * 1_000L;
        return new AbstractMap.SimpleEntry<>(token.getAccessToken(), expiredTime);
    }

    private static class DefaultAuthApi extends DefaultApi20 {
        String authUrl;

        DefaultAuthApi(String authUrl) {
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
}
