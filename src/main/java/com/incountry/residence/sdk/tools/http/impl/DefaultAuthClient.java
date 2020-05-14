package com.incountry.residence.sdk.tools.http.impl;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.AuthClient;
import com.incountry.residence.sdk.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map;

public class DefaultAuthClient implements AuthClient {

    private static final Logger LOG = LogManager.getLogger(DefaultAuthClient.class);

    private static final String DEFAULT_AUTH_URL = "https://auth-emea.qa.incountry.com/";
    //error messages
    private static final String MSG_ERR_AUTH = "Unexpected exception during authorization";
    private static final String MSG_ERR_NULL_TOKEN = "Token is null";
    private static final String MSG_ERR_EXPIRES = "Token TTL is invalid";
    private static final String MSG_ERR_INVALID_TYPE = "Token type is invalid";
    private static final String MSG_ERR_JSON = "Error in parsing authorization response";

    private static final String USER_AGENT = "User-Agent";
    private static final String USER_AGENT_VALUE = "SDK-Java/" + Version.BUILD_VERSION;
    private static final String BODY = "grant_type=client_credentials";
    private static final String BEARER_TOKEN_TYPE = "bearer";
    private static final String BASIC = "Basic ";
    private static final String POST = "POST";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String APPLICATION_URLENCODED = "application/x-www-form-urlencoded";

    private String basicAuthToken;
    private String authUrl;

    @Override
    public void setCredentials(String clientId, String secret, String authUrl) {
        this.basicAuthToken = BASIC + getCredentialsBase64(clientId, secret);
        this.authUrl = authUrl != null ? authUrl : DEFAULT_AUTH_URL;
    }

    @Override
    public Map.Entry<String, Long> newToken() throws StorageServerException {
        try {
            HttpURLConnection con = getConnection();
            OutputStream os = con.getOutputStream();
            os.write(BODY.getBytes(CHARSET));
            os.flush();
            os.close();
            int status = con.getResponseCode();
            boolean isSuccess = status == 200;
            InputStream stream = isSuccess ? con.getInputStream() : con.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, CHARSET));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            reader.close();
            if (!isSuccess) {
                logAndThrowException(content.toString());
            }
            return validateAndGet(content.toString());
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
                logAndThrowException(MSG_ERR_NULL_TOKEN);
            }
            if (token.expiresIn == null || token.expiresIn < 1) {
                logAndThrowException(MSG_ERR_EXPIRES);
            }
            if (!BEARER_TOKEN_TYPE.equals(token.tokenType)) {
                logAndThrowException(MSG_ERR_INVALID_TYPE);
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

    private HttpURLConnection getConnection() throws IOException {
        URL url = new URL(authUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(POST);
        connection.setRequestProperty(AUTHORIZATION, basicAuthToken);
        connection.setRequestProperty(CONTENT_TYPE, APPLICATION_URLENCODED);
        connection.setRequestProperty(USER_AGENT, USER_AGENT_VALUE);
        connection.setDoOutput(true);
        return connection;
    }

    private String getCredentialsBase64(String clientId, String secret) {
        return new String(Base64.getEncoder().encode((clientId + ":" + secret).getBytes(CHARSET)), CHARSET);
    }

    private static class TransferToken {
        String accessToken;
        String tokenType;
        Long expiresIn;
    }
}
