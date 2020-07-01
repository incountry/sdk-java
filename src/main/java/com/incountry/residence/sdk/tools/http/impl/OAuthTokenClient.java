package com.incountry.residence.sdk.tools.http.impl;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.utils.HttpConnection;
import com.incountry.residence.sdk.version.Version;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
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
    private static final String MSG_AUTH_URL = "auth URL={}";
    private static final String DEFAULT_APAC_AUTH_URL = "https://auth-apac.incountry.com/oauth2/token";
    private static final String DEFAULT_EMEA_AUTH_URL = "https://auth-emea.incountry.com/oauth2/token";

    private static final String APAC = "apac";
    private static final String EMEA = "emea";

    //error messages
    private static final String MSG_ERR_AUTH = "Unexpected exception during authorization";
    private static final String MSG_ERR_NULL_TOKEN = "Token is null";
    private static final String MSG_ERR_EXPIRES = "Token TTL is invalid";
    private static final String MSG_ERR_INVALID_TYPE = "Token type is invalid";
    private static final String MSG_ERR_INVALID_SCOPE = "Token scope is invalid";
    private static final String MSG_RESPONSE_ERR = "Error in parsing authorization response";
    private static final String MSG_ERR_PARAMS = "Can't use param 'authEndpoints' without setting 'defaultAuthEndpoint'";
    private static final String MSG_ERR_ILLEGAL_AUTH_ENDPOINTS = "Parameter 'authEndpoints' contains null keys/values";

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
    private final String scope;
    private final Map<String, Map.Entry<String, Long>> tokenMap = new HashMap<>();
    private final Map<String, String> regionMap = new HashMap<>();
    private final String defaultAuthEndpoint;
    private final HttpConnection connection;
    private final CloseableHttpClient httpClient;

    public OAuthTokenClient(String defaultAuthEndpoint, Map<String, String> authEndpointMap, String scope, String clientId,
                            String secret, Integer timeoutInMs, PoolingHttpClientConnectionManager connectionManager) throws StorageClientException, StorageServerException {
        if (authEndpointMap != null && !authEndpointMap.isEmpty()) {
            if (isEmpty(defaultAuthEndpoint)) {
                throw new StorageClientException(MSG_ERR_PARAMS);
            }
            if (authEndpointMap.entrySet().stream().anyMatch(entry -> isEmpty(entry.getKey()) || isEmpty(entry.getValue()))) {
                throw new StorageClientException(MSG_ERR_ILLEGAL_AUTH_ENDPOINTS);
            }
        }
        this.connection = new HttpConnection();
        this.scope = scope;
        this.basicAuthToken = BASIC + getCredentialsBase64(clientId, secret);
        this.httpClient = connection.buildHttpClient(timeoutInMs, connectionManager);
        this.defaultAuthEndpoint = defaultAuthEndpoint != null ? defaultAuthEndpoint : DEFAULT_EMEA_AUTH_URL;

        if (authEndpointMap == null || authEndpointMap.isEmpty()) {
            if (defaultAuthEndpoint == null) {
                regionMap.put(APAC, DEFAULT_APAC_AUTH_URL);
                regionMap.put(EMEA, DEFAULT_EMEA_AUTH_URL);
            }
        } else {
            authEndpointMap.forEach((key, value) -> regionMap.put(key.toLowerCase(), value));
        }
    }

    private boolean isEmpty(String row) {
        return row == null || row.isEmpty();
    }

    @Override
    public String getToken(String audience, String region) throws StorageServerException {
        return refreshToken(false, audience, region);
    }

    @Override
    public HttpConnection getHttpConnection() {
        return connection;
    }

    public synchronized String refreshToken(final boolean force, final String audience, String region) throws StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace(MSG_REFRESH_TOKEN, force, audience);
        }
        Map.Entry<String, Long> token = tokenMap.get(audience);
        if (force || token == null || token.getValue() < System.currentTimeMillis()) {
            token = newToken(audience, region);
            tokenMap.put(audience, token);
        }
        return token.getKey();
    }

    private HttpRequestBase addHeaders(HttpRequestBase request) {
        request.addHeader(AUTHORIZATION, basicAuthToken);
        request.addHeader(CONTENT_TYPE, APPLICATION_URLENCODED);
        request.addHeader(USER_AGENT, USER_AGENT_VALUE);
       return request;
    }

    private Map.Entry<String, Long> newToken(String audience, String region) throws StorageServerException {
        String body = String.format(BODY, audience, scope);
        try {
            String authUrl = regionMap.get(region);
            if (authUrl == null) {
                authUrl = defaultAuthEndpoint;
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace(MSG_AUTH_URL, authUrl);
            }

            HttpRequestBase request = connection.createRequest(authUrl, POST, body);
            request = addHeaders(request);

            HttpResponse response = httpClient.execute(request);
            Integer status = response.getStatusLine().getStatusCode();
            String responseContent = EntityUtils.toString(response.getEntity());

            boolean isSuccess = status == 200;

            if (!isSuccess) {
                throw createAndLogException(String.format("%s: '%s'", MSG_RESPONSE_ERR, responseContent));
            }
            return validateAndGet(responseContent);

        } catch (IOException ex) {
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
                throw createAndLogException(MSG_ERR_NULL_TOKEN);
            }
            if (token.expiresIn == null || token.expiresIn < 1) {
                throw createAndLogException(MSG_ERR_EXPIRES);
            }
            if (!BEARER_TOKEN_TYPE.equals(token.tokenType)) {
                throw createAndLogException(MSG_ERR_INVALID_TYPE);
            }
            if (!scope.equals(token.scope)) {
                throw createAndLogException(MSG_ERR_INVALID_SCOPE);
            }
            return new AbstractMap.SimpleEntry<>(token.accessToken, System.currentTimeMillis() + token.expiresIn * 1_000L);
        } catch (JsonSyntaxException jsonSyntaxException) {
            throw new StorageServerException(MSG_RESPONSE_ERR, jsonSyntaxException);
        }
    }

    private StorageServerException createAndLogException(String message) {
        message = message.replaceAll("[\r\n]", "");
        LOG.error(message);
        return new StorageServerException(message);
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
