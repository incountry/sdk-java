package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthTest {

    private static final int PORT = 8765;
    private static final int TIMEOUT_IN_MS = 30_000;
    private static final String ENV_ID = "envId";
    private static final String AUTH_URL = "http://localhost:" + PORT;
    private static final String ENDPOINT_MASK = "localhost";
    private static final String AUDIENCE_URL = "https://localhost";
    private static final String COUNTRY = "us";

    private TokenClient getTokenClient() {
        return new OAuthTokenClient(AUTH_URL, ENV_ID, "<client_id>", "<client_secret>", TIMEOUT_IN_MS);
    }

    @RepeatedTest(3)
    void tokenGeneratorTest(RepetitionInfo repeatInfo) throws StorageServerException, IOException {
        iterateLogLevel(repeatInfo, OAuthTokenClient.class);
        List<String> responseList = Collections.singletonList(
                "{'access_token'='1234567889' , 'expires_in'='1' , 'token_type'='bearer', 'scope'='" + ENV_ID + "'}"
        );
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        TokenClient tokenClient = getTokenClient();
        for (int i = 0; i < 1_000; i++) {
            assertNotNull(tokenClient.getToken(AUDIENCE_URL));
        }
        tokenClient.refreshToken(false, AUDIENCE_URL);
        tokenClient.refreshToken(true, AUDIENCE_URL);
        server.stop(0);
    }

    @Test
    void defaultAuthClientPositiveTest() throws IOException, StorageServerException {
        List<String> responseList = Collections.singletonList(
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='bearer', 'scope'='" + ENV_ID + "'}"
        );
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        assertNotNull(getTokenClient().getToken(AUDIENCE_URL));
        server.stop(0);
    }

    @Test
    void defaultAuthClientPositiveTestWithoutMask() throws IOException, StorageServerException {
        List<String> responseList = Collections.singletonList(
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='bearer', 'scope'='" + ENV_ID + "'}"
        );
        TokenClient tokenClient = new OAuthTokenClient(AUTH_URL, ENV_ID, "<client_id>", "<client_secret>", TIMEOUT_IN_MS);
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        assertNotNull(tokenClient.getToken(AUDIENCE_URL));
        assertNotNull(tokenClient.getToken(AUDIENCE_URL));
        server.stop(0);
    }

    @Test
    void defaultAuthClientNegativeTest() throws IOException {
        int respCode = 401;
        FakeHttpServer server = new FakeHttpServer("error", respCode, PORT);
        server.start();
        StorageServerException ex = assertThrows(StorageServerException.class, () -> getTokenClient().getToken(AUDIENCE_URL));
        assertEquals("error", ex.getMessage());
        server.stop(0);
    }

    @Test
    void defaultAuthClientNegativeTestBadToken() throws IOException {
        Map<String, String> responsesWithExpectedExceptions = new HashMap<String, String>() {{
            put("{'access_token'='1234567889' , 'expires_in'='0' , 'token_type'='bearer'}", "Token TTL is invalid");
            put("{'access_token'='1234567889' , 'expires_in'='-1' , 'token_type'='bearer'}", "Token TTL is invalid");
            put("{'access_token'='1234567889' , 'expires_in'='abcd' , 'token_type'='bearer'}", "Error in parsing authorization response");
            put("{'access_token'='1234567889' , 'expires_in'='' , 'token_type'='bearer'}", "Error in parsing authorization response");
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
        for (int i = 0; i < responseList.size(); i++) {
            StorageServerException ex = assertThrows(StorageServerException.class, () -> getTokenClient().getToken(AUDIENCE_URL));
            assertEquals(responsesWithExpectedExceptions.get(responseList.get(i)), ex.getMessage());
        }
        server.stop(0);
    }
}
