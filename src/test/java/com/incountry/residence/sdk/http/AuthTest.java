package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthTest {

    private static final int PORT = 8765;
    private static final String ENV_ID = "envId";
    private static final String AUTH_URL = "http://localhost:" + PORT;

    private TokenClient getTokenClient() {
        return new OAuthTokenClient(AUTH_URL, ENV_ID, "<client_id>", "<client_secret>");
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
            assertNotNull(tokenClient.getToken("http://test"));
        }
        tokenClient.refreshToken(false, "http://test");
        tokenClient.refreshToken(true, "http://test");
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
        assertNotNull(getTokenClient().getToken("http://test"));
        server.stop(0);
    }

    @Test
    void defaultAuthClientNegativeTest() throws IOException {
        int respCode = 401;
        FakeHttpServer server = new FakeHttpServer("error", respCode, PORT);
        server.start();
        assertThrows(StorageServerException.class, () -> getTokenClient().getToken("http://test"));
        server.stop(0);
    }

    @Test
    void defaultAuthClientNegativeTestBadToken() throws IOException {
        List<String> responseList = Arrays.asList(
                "{'access_token'='1234567889' , 'expires_in'='0' , 'token_type'='bearer'}",
                "{'access_token'='1234567889' , 'expires_in'='-1' , 'token_type'='bearer'}",
                "{'access_token'='1234567889' , 'expires_in'='abcd' , 'token_type'='bearer'}",
                "{'access_token'='1234567889' , 'expires_in'='' , 'token_type'='bearer'}",
                "{'access_token'='1234567889' , 'expires_in'=null , 'token_type'='bearer'}",
                "{'access_token'='1234567889' , 'token_type'='bearer'}",
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='bearer1'}",
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'=''}",
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'=null}",
                "{'access_token'='1234567889' , 'expires_in'='1000' }",
                "{'access_token'='' , 'expires_in'='1000' , 'token_type'='bearer'}",
                "{'access_token'='' , 'expires_in'='1000' , 'token_type'='bearer'}",
                "{'access_token'=null , 'expires_in'='1000' , 'token_type'='bearer'}",
                "{'expires_in'='1000' , 'token_type'='bearer'}",
                "{'access_token'='1234567889' , 'expires_in'='1000', 'token_type'='bearer', 'scope'='invalid_scope'}"
        );
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();
        for (int i = 0; i < responseList.size(); i++) {
            assertThrows(StorageServerException.class, () -> getTokenClient().getToken("http://test"));
        }
        server.stop(0);
    }
}
