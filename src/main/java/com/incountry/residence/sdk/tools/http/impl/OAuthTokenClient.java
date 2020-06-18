package com.incountry.residence.sdk.tools.http.impl;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.utils.HttpUtils;
import com.incountry.residence.sdk.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class OAuthTokenClient implements TokenClient {

    private static final Logger LOG = LogManager.getLogger(OAuthTokenClient.class);
    private static final String MSG_REFRESH_TOKEN = "refreshToken force={}, audience={}";
    private static final String DEFAULT_AUTH_URL = "https://auth.incountry.com/oauth2/token";
    //error messages
    private static final String MSG_ERR_AUTH = "Unexpected exception during authorization";
    private static final String MSG_ERR_NULL_TOKEN = "Token is null";
    private static final String MSG_ERR_EXPIRES = "Token TTL is invalid";
    private static final String MSG_ERR_INVALID_TYPE = "Token type is invalid";
    private static final String MSG_ERR_INVALID_SCOPE = "Token scope is invalid";
    private static final String MSG_ERR_JSON = "Error in parsing authorization response";

    private static final String USER_AGENT = "User-Agent";
    private static final String USER_AGENT_VALUE = "SDK-Java/" + Version.BUILD_VERSION;
    private static final String BODY = "grant_type=client_credentials&audience=%s&scope=%s";
    private static final String BEARER_TOKEN_TYPE = "bearer";
    private static final String BASIC = "Basic ";
    private static final String POST = "POST";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String APPLICATION_URLENCODED = "application/x-www-form-urlencoded";

    private final String basicAuthToken;
    private final String authEndpoint;
    private final String scope;
    private final Integer timeoutInMs;
//    private final Integer poolSize;
    private final HttpRequestFactory requestFactory;
    private final Map<String, Map.Entry<String, Long>> tokenMap = new HashMap<>();

    public OAuthTokenClient(String authEndpoint, String scope, String clientId, String secret, Integer timeoutInMs, Integer poolSize) throws StorageServerException {
        this.authEndpoint = authEndpoint != null ? authEndpoint : DEFAULT_AUTH_URL;
        this.scope = scope;
        this.basicAuthToken = BASIC + getCredentialsBase64(clientId, secret);
        this.timeoutInMs = timeoutInMs;
//        this.poolSize = poolSize;
        this.requestFactory = HttpUtils.provideHttpRequestFactory(poolSize);
    }

    @Override
    public String getToken(String audience) throws StorageServerException {
        return refreshToken(false, audience);
    }

    public synchronized String refreshToken(final boolean force, final String audience) throws StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace(MSG_REFRESH_TOKEN, force, audience);
        }
        Map.Entry<String, Long> token = tokenMap.get(audience);
        if (force || token == null || token.getValue() < System.currentTimeMillis()) {
            token = newToken(audience);
            tokenMap.put(audience, token);
        }
        return token.getKey();
    }

    private HttpRequest addHeaders(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        headers.setAuthorization(basicAuthToken);
        headers.setContentType(APPLICATION_URLENCODED);
        headers.set(USER_AGENT, USER_AGENT_VALUE);
        request.setHeaders(headers);
        return request;
    }

    private Map.Entry<String, Long> newToken(String audience) throws StorageServerException {
        String body = String.format(BODY, audience, scope);
        HttpResponse response = null;
        try {
            HttpRequest request = HttpUtils.buildRequest(requestFactory, authEndpoint, POST, body, timeoutInMs);
            request = addHeaders(request);
            int status = 0;
            String result = "";
            String errorMassage = "";
            try {
                response = request.execute();
                status = response.getStatusCode();
            } catch (HttpResponseException ex) {
                status = ex.getStatusCode();
                errorMassage = ex.getMessage();
            }
            boolean isSuccess = status == 200;

            result = isSuccess ? response.parseAsString() : errorMassage;

            if (!isSuccess) {
                logAndThrowException(result);
            }
            return validateAndGet(result);

        } catch (IOException ex) {
            throw new StorageServerException(MSG_ERR_AUTH, ex);
        } catch (IllegalArgumentException ex) {
            throw new StorageServerException(MSG_ERR_AUTH, ex);
        }
    }

    private Map.Entry<String, Long> validateAndGet(String response) throws StorageServerException {
        try {
            TransferToken token = new GsonBuilder()
                    .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
                    .fromJson(response, TransferToken.class);
            if (token.accessToken == null || token.accessToken.isEmpty()) {
                logAndThrowException(MSG_ERR_NULL_TOKEN);
            }
            if (token.expiresIn == null || token.expiresIn < 1) {
                logAndThrowException(MSG_ERR_EXPIRES);
            }
            if (!BEARER_TOKEN_TYPE.equals(token.tokenType)) {
                logAndThrowException(MSG_ERR_INVALID_TYPE);
            }
            if (!scope.equals(token.scope)) {
                logAndThrowException(MSG_ERR_INVALID_SCOPE);
            }
            return new AbstractMap.SimpleEntry<>(token.accessToken, System.currentTimeMillis() + token.expiresIn);
        } catch (JsonSyntaxException jsonSyntaxException) {
            throw new StorageServerException(MSG_ERR_JSON, jsonSyntaxException);
        }
    }

    private void logAndThrowException(String message) throws StorageServerException {
        LOG.error(message);
        throw new StorageServerException(message);
    }

    private String getCredentialsBase64(String clientId, String secret) {
        return new String(Base64.getEncoder().encode((clientId + ":" + secret).getBytes(CHARSET)), CHARSET);
    }

    private static class TransferToken {
        String accessToken;
        String tokenType;
        String scope;
        Long expiresIn;
    }
}
