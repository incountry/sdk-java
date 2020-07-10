package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenClientTest {

    private static final int PORT = 8765;
    private static final String ENV_ID = "envId";
    private static final String DEFAULT_AUTH_ENDPOINT = "http://localhost:" + PORT;
    private static final String AUDIENCE_URL = "https://localhost";

    private TokenClient getTokenClient() throws StorageException {
        return new OAuthTokenClient(DEFAULT_AUTH_ENDPOINT, null, ENV_ID, "<client_id>", "<client_secret>", HttpClients.createDefault());
    }

    @RepeatedTest(3)
    void tokenGeneratorTest(RepetitionInfo repeatInfo) throws StorageException, IOException {
        iterateLogLevel(repeatInfo, OAuthTokenClient.class);
        List<String> responseList = Collections.singletonList(
                "{'access_token'='1234567889' , 'expires_in'='1' , 'token_type'='bearer', 'scope'='" + ENV_ID + "'}"
        );
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        TokenClient tokenClient = getTokenClient();
        for (int i = 0; i < 1_000; i++) {
            assertNotNull(tokenClient.getToken(AUDIENCE_URL, null));
        }

        tokenClient.refreshToken(false, AUDIENCE_URL, null);
        tokenClient.refreshToken(true, AUDIENCE_URL, null);
        server.stop(0);
    }

    @Test
    void defaultAuthClientPositiveTest() throws StorageException, IOException {
        List<String> responseList = Collections.singletonList(
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='bearer', 'scope'='" + ENV_ID + "'}"
        );
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        assertNotNull(getTokenClient().getToken(AUDIENCE_URL, null));
        server.stop(0);
    }

    @Test
    void defaultAuthClientPositiveTestWithoutMask() throws StorageException, IOException {
        List<String> responseList = Collections.singletonList(
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='bearer', 'scope'='" + ENV_ID + "'}"
        );
        TokenClient tokenClient = new OAuthTokenClient(DEFAULT_AUTH_ENDPOINT, null, ENV_ID, "<client_id>", "<client_secret>", HttpClients.createDefault());
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        assertNotNull(tokenClient.getToken(AUDIENCE_URL, null));
        assertNotNull(tokenClient.getToken(AUDIENCE_URL, null));
        server.stop(0);
    }

    @Test
    void defaultAuthClientNegativeTest() throws StorageException, IOException {
        int respCode = 401;
        String errorMessage = "someError";
        FakeHttpServer server = new FakeHttpServer(Arrays.asList(errorMessage, null), respCode, PORT);
        server.start();
        TokenClient tokenClient = getTokenClient();
        StorageServerException ex = assertThrows(StorageServerException.class, () -> tokenClient.getToken(AUDIENCE_URL, null));
        assertEquals("Error in parsing authorization response: '" + errorMessage + "'", ex.getMessage());

        ex = assertThrows(StorageServerException.class, () -> tokenClient.getToken(AUDIENCE_URL, null));
        assertEquals("Error in parsing authorization response: ''", ex.getMessage());
        server.stop(0);
    }

    @Test
    void defaultAuthClientNegativeTestBadToken() throws StorageException, IOException {
        Map<String, String> responsesWithExpectedExceptions = new HashMap<String, String>() {{
            put("{'access_token'='1234567889' , 'expires_in'='0' , 'token_type'='bearer'}", "Token TTL is invalid");
            put("{'access_token'='1234567889' , 'expires_in'='-1' , 'token_type'='bearer'}", "Token TTL is invalid");
            put("{'access_token'='1234567889' , 'expires_in'='abcd' , 'token_type'='bearer'}", "Error in parsing authorization response: '{'access_token'='1234567889' , 'expires_in'='abcd' , 'token_type'='bearer'}'");
            put("{'access_token'='1234567889' , 'expires_in'='' , 'token_type'='bearer'}", "Error in parsing authorization response: '{'access_token'='1234567889' , 'expires_in'='' , 'token_type'='bearer'}'");
            put("{'access_token'='1234567889' , 'expires_in'=null , 'token_type'='bearer'}", "Token TTL is invalid");
            put("{'access_token'='1234567889' , 'token_type'='bearer'}", "Token TTL is invalid");
            put("{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='bearer1'}", "Token type is invalid");
            put("{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'=''}", "Token type is invalid");
            put("{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'=null}", "Token type is invalid");
            put("{'access_token'='1234567889' , 'expires_in'='1000' }", "Token type is invalid");
            put("{'access_token'='' , 'expires_in'='1000' , 'token_type'='bearer'}", "Token is null");
            put("{'access_token'=null , 'expires_in'='1000' , 'token_type'='bearer'}", "Token is null");
            put("{'expires_in'='1000' , 'token_type'='bearer'}", "Token is null");
            put("{'access_token'='1234567889' , 'expires_in'='1000', 'token_type'='bearer', 'scope'='invalid_scope'}", "Token scope is invalid");
        }};
        int respCode = 200;

        List<String> responseList = new ArrayList<>(responsesWithExpectedExceptions.keySet());
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        TokenClient tokenClient = getTokenClient();
        for (String s : responseList) {
            StorageServerException ex = assertThrows(StorageServerException.class, () -> tokenClient.getToken(AUDIENCE_URL, null));
            assertEquals(responsesWithExpectedExceptions.get(s), ex.getMessage());
        }
        server.stop(0);
    }

    @Test
    void negativeTestAccessTokenEmpty() throws IOException, StorageException {
        List<String> responseList = Collections.singletonList(
                "{'access_token'='' , 'expires_in'='1000' , 'token_type'='bearer', 'scope'='" + ENV_ID + "'}"
        );
        TokenClient tokenClient = new OAuthTokenClient(DEFAULT_AUTH_ENDPOINT, null, ENV_ID, "<client_id>", "<client_secret>", HttpClients.createDefault());
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        StorageServerException ex = assertThrows(StorageServerException.class, () -> tokenClient.getToken(AUDIENCE_URL, null));
        assertEquals("Token is null", ex.getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestTokenTypeNotEqualBearer() throws IOException, StorageException {
        List<String> responseList = Collections.singletonList(
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='test', 'scope'='" + ENV_ID + "'}"
        );
        TokenClient tokenClient = new OAuthTokenClient(DEFAULT_AUTH_ENDPOINT, null, ENV_ID, "<client_id>", "<client_secret>", HttpClients.createDefault());
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        StorageServerException ex = assertThrows(StorageServerException.class, () -> tokenClient.getToken(AUDIENCE_URL, null));
        assertEquals("Token type is invalid", ex.getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestWrongScope() throws IOException, StorageException {
        List<String> responseList = Collections.singletonList(
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='bearer', 'scope'='" + "test" + "'}"
        );
        TokenClient tokenClient = new OAuthTokenClient(DEFAULT_AUTH_ENDPOINT, null, ENV_ID, "<client_id>", "<client_secret>", HttpClients.createDefault());
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        StorageServerException ex = assertThrows(StorageServerException.class, () -> tokenClient.getToken(AUDIENCE_URL, null));
        assertEquals("Token scope is invalid", ex.getMessage());
        server.stop(0);
    }

    @Test
    void testCreationWithMask() throws StorageException {
        Map<String, String> authEndpoints = new HashMap<>();
        authEndpoints.put("emea", "auth-emea-localhost.localhost");
        authEndpoints.put("apac", "auth-apac-localhost.localhost");
        TokenClient tokenClient = new OAuthTokenClient("auth-emea-localhost.localhost", authEndpoints, ENV_ID, "<client_id>", "<client_secret>", HttpClients.createDefault());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> tokenClient.getToken("audience-null", null));
        assertEquals("Unexpected exception during authorization, params [OAuth URL=auth-emea-localhost.localhost, audience=audience-null]", ex.getMessage());
        assertEquals(ClientProtocolException.class, ex.getCause().getClass());
        assertThrows(StorageServerException.class, () -> tokenClient.getToken("audience-emea", "emea"));
        assertThrows(StorageServerException.class, () -> tokenClient.getToken("audience-apac", "apac"));
        assertThrows(StorageServerException.class, () -> tokenClient.getToken("audience-amer", "amer"));
        assertThrows(StorageServerException.class, () -> tokenClient.getToken("audience-wrong_value", "wrong_value"));
    }

    @Test
    void testNegativeTokenClientCreation() {
        Map<String, String> fakeMap = new HashMap<>();
        fakeMap.put("key", null);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new OAuthTokenClient(null, fakeMap, null, null, null, HttpClients.createDefault()));
        assertEquals("Can't use param 'authEndpoints' without setting 'defaultAuthEndpoint'", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new OAuthTokenClient("defaultEndPoint", fakeMap, null, null, null, HttpClients.createDefault()));
        assertEquals("Parameter 'authEndpoints' contains null keys/values", ex.getMessage());

        fakeMap.clear();
        fakeMap.put(null, "value");
        ex = assertThrows(StorageClientException.class, () -> new OAuthTokenClient("defaultEndPoint", fakeMap, null, null, null, HttpClients.createDefault()));
        assertEquals("Parameter 'authEndpoints' contains null keys/values", ex.getMessage());

        fakeMap.clear();
        fakeMap.put("key", "");
        ex = assertThrows(StorageClientException.class, () -> new OAuthTokenClient("defaultEndPoint", fakeMap, null, null, null, HttpClients.createDefault()));
        assertEquals("Parameter 'authEndpoints' contains null keys/values", ex.getMessage());
    }

    @Test
    void testPositiveTokenClientCreation() throws StorageException {
        TokenClient tokenClient = new OAuthTokenClient(null, new HashMap<>(), null, null, null, HttpClients.createDefault());
        assertNotNull(tokenClient);

        tokenClient = new OAuthTokenClient(null, null, null, null, null, HttpClients.createDefault());
        assertNotNull(tokenClient);
    }
}
