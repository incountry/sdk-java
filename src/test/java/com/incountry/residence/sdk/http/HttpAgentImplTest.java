package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.ApiKeyTokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
    private static final Integer HTTP_POOL_SIZE = 2;

    private CloseableHttpClient httpClient;

    @BeforeEach
    public void initializeHttpConnectionsPool() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(HTTP_POOL_SIZE);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_IN_MS)
                .setSocketTimeout(TIMEOUT_IN_MS)
                .build();
        httpClient =  HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Test
    void testNullEndpointException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("Server request error: GET", ex.getMessage());
    }

    @Test
    void testNullApiKeyException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("Server request error: GET", ex.getMessage());
    }

    @Test
    void testNullEnvIdException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, null, httpClient);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("Server request error: GET", ex.getMessage());
    }

    @Test
    void testFakeEndpointException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request("https://" + UUID.randomUUID().toString() + "localhost",
                "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("Server request error: GET", ex.getMessage());
    }

    @RepeatedTest(3)
    void testWithFakeHttpServer(RepetitionInfo repeatInfo) throws IOException, StorageException {
        iterateLogLevel(repeatInfo, HttpAgentImpl.class);
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        assertNotNull(agent.request(ENDPOINT, "POST", "<body>", ApiResponse.DELETE, null, null, 0));
        assertNotNull(agent.request(ENDPOINT, "POST", "", ApiResponse.DELETE, null, null, 0));
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, "POST", null, ApiResponse.DELETE, null, null, 0));
        assertEquals("Server request error: POST", ex.getMessage());
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerBadCode() throws IOException {
        int respCode = 555;
        String content = "{}";
        String method = "POST";
        FakeHttpServer server = new FakeHttpServer(content, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, "POST", "<body>", ApiResponse.DELETE, null, null, 0));
        assertEquals(String.format("Code=%d, endpoint=[%s], content=[%s]", respCode, ENDPOINT, content), ex.getMessage());
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerBadCodeRefreshToken() throws IOException, StorageServerException {
        List<Integer> respCodeList = Arrays.asList(401, 401, 401, 401, 401, 200);
        int errorCode = 401;
        String content = "{}";
        String method = "POST";
        String expectedErrorString = String.format("Code=%d, endpoint=[%s], content=[%s]", errorCode, ENDPOINT, content);
        FakeHttpServer server = new FakeHttpServer(content, respCodeList, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        StorageServerException ex1 = assertThrows(StorageServerException.class, () ->
                agent.request(ENDPOINT, method, "<body>", ApiResponse.DELETE, null, null, 0));
        assertEquals(expectedErrorString, ex1.getMessage());
        StorageServerException ex2 = assertThrows(StorageServerException.class, () ->
                agent.request(ENDPOINT, method, "<body>", ApiResponse.DELETE, null, null, 2));
        assertEquals(expectedErrorString, ex2.getMessage());
        assertEquals(content, agent.request(ENDPOINT, method, "<body>", ApiResponse.DELETE, null, null, 1));
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerIgnoredStatus() throws IOException, StorageServerException {
        int respCode = 404;
        FakeHttpServer server = new FakeHttpServer((String) null, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        assertNull(agent.request(ENDPOINT, "POST", "<body>", ApiResponse.READ, null, null, 0));
        server.stop(0);
    }

    @Test
    void testExpectedExceptionInsteadOfNPE() throws IOException {
        int respCode = 201;
        String response = "ok";
        FakeHttpServer server = new FakeHttpServer(response, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        StorageServerException ex = assertThrows(StorageServerException.class, ()
                -> agent.request(ENDPOINT, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals(String.format("Code=%d, endpoint=[%s], content=[ok]", respCode, ENDPOINT), ex.getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestWithIllegalUrl() throws IOException {
        int respCode = 201;
        String response = "ok";
        FakeHttpServer server = new FakeHttpServer(response, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", httpClient);
        StorageServerException ex = assertThrows(StorageServerException.class, ()
                -> agent.request(" ", "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("URL error", ex.getMessage());
        server.stop(0);
    }
}
