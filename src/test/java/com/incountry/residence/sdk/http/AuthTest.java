package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.http.mocks.FakeAuthClient;
import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.impl.DefaultAuthClient;
import com.incountry.residence.sdk.tools.http.impl.DefaultTokenGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthTest {

    @Test
    public void tokenGeneratorTest() throws StorageServerException {
        DefaultTokenGenerator generator = new DefaultTokenGenerator(new FakeAuthClient(0));
        for (int i = 0; i < 1_000; i++) {
            assertNotNull(generator.getToken());
        }
        generator.refreshToken(false);
        generator.refreshToken(true);
    }

    @Test
    public void defaultAuthClientPositiveTest() throws IOException, StorageServerException {
        List<String> responseList = Arrays.asList(
                "{'access_token'='1234567889' , 'expires_in'='1000' , 'token_type'='bearer'}"
        );
        int respCode = 200;
        int port = 8764;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, port);
        server.start();
        String authUrl = "http://localhost:" + port;

        DefaultAuthClient authClient = new DefaultAuthClient();
        authClient.setCredentials("<client_id>", "<client_secret>", authUrl);
        assertNotNull(authClient.newToken());
        server.stop(0);
    }

    @Test
    public void defaultAuthClientNegativeTest() throws IOException {
        int respCode = 401;
        int port = 8765;
        FakeHttpServer server = new FakeHttpServer(Arrays.asList("error"), respCode, port);
        server.start();
        String authUrl = "http://localhost:" + port;
        DefaultAuthClient authClient = new DefaultAuthClient();
        authClient.setCredentials("<client_id>", "<client_secret>", authUrl);
        assertThrows(StorageServerException.class, authClient::newToken);
        server.stop(0);
    }

    @Test
    public void defaultAuthClientNegativeTestBadToken() throws IOException {
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
                "{'expires_in'='1000' , 'token_type'='bearer'}"
        );
        int respCode = 200;
        int port = 8766;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, port);
        server.start();
        String authUrl = "http://localhost:" + port;

        DefaultAuthClient authClient = new DefaultAuthClient();
        authClient.setCredentials("<client_id>", "<client_secret>", authUrl);
        for (int i = 0; i < responseList.size(); i++) {
            assertThrows(StorageServerException.class, authClient::newToken);
        }
        server.stop(0);
    }
}
