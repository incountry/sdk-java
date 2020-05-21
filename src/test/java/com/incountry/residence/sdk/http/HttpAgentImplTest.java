package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
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

    private static final int PORT = 8766;
    private static final Integer TIMEOUT_IN_MS = 30_000;

    @Test
    void testNullEndpointException() {
        HttpAgent agent = new HttpAgentImpl("envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
    }

    @Test
    void testNullApiKeyException() {
        HttpAgent agent = new HttpAgentImpl("envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
    }

    @Test
    void testNullEnvIdException() {
        HttpAgent agent = new HttpAgentImpl(null, StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
    }

    @Test
    void testFakeEndpointException() {
        HttpAgent agent = new HttpAgentImpl("envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request("https://" + UUID.randomUUID().toString() + "localhost",
                "GET", "someBody", new HashMap<>(), new ApiKeyTokenClient("<apiKey>"), null, 0));
    }

    @RepeatedTest(3)
    void testWithFakeHttpServer(RepetitionInfo repeatInfo) throws IOException, StorageServerException {
        iterateLogLevel(repeatInfo, HttpAgentImpl.class);
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();
        String url = "http://localhost:" + PORT;
        HttpAgent agent = new HttpAgentImpl("envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertNotNull(agent.request(url, "POST", "<body>", ApiResponse.DELETE, new ApiKeyTokenClient("<apiKey>"), null, 0));
        assertNotNull(agent.request(url, "POST", null, ApiResponse.DELETE, new ApiKeyTokenClient("<apiKey>"), null, 0));
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerBadCode() throws IOException {
        int respCode = 555;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();
        String url = "http://localhost:" + PORT;
        HttpAgent agent = new HttpAgentImpl("envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () -> agent.request(url, "POST", "<body>", ApiResponse.DELETE, new ApiKeyTokenClient("<apiKey>"), null, 0));
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerBadCodeRefreshToken() throws IOException, StorageServerException {
        List<Integer> respCodeList = Arrays.asList(401, 401, 401, 401, 401, 200);
        FakeHttpServer server = new FakeHttpServer("{}", respCodeList, PORT);
        server.start();
        String url = "http://localhost:" + PORT;
        HttpAgent agent = new HttpAgentImpl("envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertThrows(StorageServerException.class, () ->
                agent.request(url, "POST", "<body>", ApiResponse.DELETE, new ApiKeyTokenClient("apiKey"), null, 0));
        assertThrows(StorageServerException.class, () ->
                agent.request(url, "POST", "<body>", ApiResponse.DELETE, new ApiKeyTokenClient("apiKey"), null, 2));
        assertEquals("{}", agent.request(url, "POST", "<body>", ApiResponse.DELETE, new ApiKeyTokenClient("apiKey"), null, 1));
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerIgnoredStatus() throws IOException, StorageServerException {
        int respCode = 404;
        FakeHttpServer server = new FakeHttpServer((String) null, respCode, PORT);
        server.start();
        String url = "http://localhost:" + PORT;
        HttpAgent agent = new HttpAgentImpl("envId", StandardCharsets.UTF_8, TIMEOUT_IN_MS);
        assertNull(agent.request(url, "POST", "<body>", ApiResponse.READ, new ApiKeyTokenClient("<apiKey>"), null, 0));
        server.stop(0);
    }
}
