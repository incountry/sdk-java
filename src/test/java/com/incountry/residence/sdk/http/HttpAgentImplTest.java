package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.ApiKeyTokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpAgentImplTest {

    private static final int PORT = 8769;
    private static final String ENDPOINT = "http://localhost:" + PORT;
    private static final Integer TIMEOUT_IN_MS = 30_000;
    private static final TokenClient TOKEN_CLIENT = new ApiKeyTokenClient("<api_key>");

    @Test
    void testNullEndpointException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, 0));
    }

    @Test
    void testNullApiKeyException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, 0));
    }

    @Test
    void testNullEnvIdException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, null, StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, 0));
    }

    @Test
    void testFakeEndpointException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request("https://" + UUID.randomUUID().toString() + "localhost",
                "GET", "someBody", new HashMap<>(), null, 0));
    }

    @RepeatedTest(3)
    void testWithFakeHttpServer(RepetitionInfo repeatInfo) throws IOException, StorageServerException {
        iterateLogLevel(repeatInfo, HttpAgentImpl.class);
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertNotNull(agent.request(ENDPOINT, "POST", "<body>", ApiResponse.DELETE, null, 0));
        assertNotNull(agent.request(ENDPOINT, "POST", null, ApiResponse.DELETE, null, 0));
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerBadCode() throws IOException {
        int respCode = 555;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, "POST", "<body>", ApiResponse.DELETE, null, 0));
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerBadCodeRefreshToken() throws IOException, StorageServerException {
        List<Integer> respCodeList = Arrays.asList(401, 401, 401, 401, 401, 200);
        FakeHttpServer server = new FakeHttpServer("{}", respCodeList, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () ->
                agent.request(ENDPOINT, "POST", "<body>", ApiResponse.DELETE, null, 0));
        assertThrows(StorageServerException.class, () ->
                agent.request(ENDPOINT, "POST", "<body>", ApiResponse.DELETE, null, 2));
        assertEquals("{}", agent.request(ENDPOINT, "POST", "<body>", ApiResponse.DELETE, null, 1));
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerIgnoredStatus() throws IOException, StorageServerException {
        int respCode = 404;
        FakeHttpServer server = new FakeHttpServer((String) null, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertNull(agent.request(ENDPOINT, "POST", "<body>", ApiResponse.READ, null, 0));
        server.stop(0);
    }

    @Test
    void testExpectedExceptionInsteadOfNPE() throws IOException {
        int respCode = 201;
        String response = "ok";
        FakeHttpServer server = new FakeHttpServer(response, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        StorageServerException ex = assertThrows(StorageServerException.class, ()
                -> agent.request(ENDPOINT, "GET", "someBody", new HashMap<>(), null, 0));
        assertEquals("Code=201, endpoint=[http://localhost:8769], content=[]", ex.getMessage());
        server.stop(0);
    }
}
