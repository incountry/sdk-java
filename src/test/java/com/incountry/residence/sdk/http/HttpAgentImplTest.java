package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.Storage;
import com.incountry.residence.sdk.StorageConfig;
import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.ApiKeyTokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpAgentImplTest {

    private static final Logger LOG = LogManager.getLogger(HttpAgentImplTest.class);

    private static final int PORT = 8769;
    private static final String ENDPOINT = "http://localhost:" + PORT;
    private static final TokenClient TOKEN_CLIENT = new ApiKeyTokenClient("<api_key>");


    @Test
    void testNullEndpointException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("Server request error: GET", ex.getMessage());
    }

    @Test
    void testNullApiKeyException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("Server request error: GET", ex.getMessage());
    }

    @Test
    void testNullEnvIdException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, null, HttpClients.createDefault());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("Server request error: GET", ex.getMessage());
    }

    @Test
    void testFakeEndpointException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
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
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
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
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, "POST", "<body>", ApiResponse.DELETE, null, null, 0));
        assertEquals(String.format("Code=%d, endpoint=[%s], content=[%s]", respCode, ENDPOINT, content), ex.getMessage());
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerBadCodeRefreshToken() throws IOException, StorageServerException {
        List<Integer> respCodeList = Arrays.asList(401, 401, 401, 401, 401, 200);
        String content = "{}";
        String method = "POST";
        String expectedErrorString = String.format("Code=401, endpoint=[%s], content=[%s]", ENDPOINT, content);
        FakeHttpServer server = new FakeHttpServer(content, respCodeList, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
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
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        assertNull(agent.request(ENDPOINT, "POST", "<body>", ApiResponse.READ, null, null, 0));
        server.stop(0);
    }

    @Test
    void testExpectedExceptionInsteadOfNPE() throws IOException {
        int respCode = 201;
        String response = "ok";
        FakeHttpServer server = new FakeHttpServer(response, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageServerException ex = assertThrows(StorageServerException.class, ()
                -> agent.request(ENDPOINT, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals(String.format("Code=%d, endpoint=[%s], content=[ok]", respCode, ENDPOINT), ex.getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestWithIllegalUrl() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        String url = " ";
        StorageServerException ex = assertThrows(StorageServerException.class, ()
                -> agent.request(url, "GET", "someBody", new HashMap<>(), null, null, 0));
        assertEquals("URL error", ex.getMessage());
        assertEquals(URISyntaxException.class, ex.getCause().getClass());
        assertEquals("Illegal character in path at index 0: " + url, ex.getCause().getMessage());
    }

    @Test
    void positiveHttpPoolTest() throws IOException, StorageClientException, StorageServerException, InterruptedException, ExecutionException {
        String envId = "envId";
        List<String> responseList = Arrays.asList(
                "{'access_token'='1234567889' , 'expires_in'='300' , 'token_type'='bearer', 'scope'='" + envId + "'}",
                "ok"
        );
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer(responseList, respCode, PORT);
        server.start();

        int poolSize = 12;

        StorageConfig config = new StorageConfig()
                .setDefaultAuthEndpoint("http://localhost:" + PORT)
                .setEndPoint("http://localhost:" + PORT)
                .setEnvId(envId)
                .setClientId("<clientId>")
                .setClientSecret("<clientSecret>")
                .setMaxHttpPoolSize(poolSize)
                .setMaxHttpConnectionsPerRoute(poolSize / 3);
        final Storage multipleConnectionStorage = StorageImpl.getInstance(config);

        ExecutorService executorService = Executors.newFixedThreadPool(poolSize / 2);
        List<Future<StorageException>> futureList = new ArrayList<>();
        int taskCount = poolSize * 10;
        for (int i = 0; i < taskCount; i++) {
            final int numb = i;
            futureList.add(executorService.submit(() -> {
                try {
                    Thread currentThread = Thread.currentThread();
                    currentThread.setName("positiveHttpPoolTest #" + numb);
                    LOG.trace("Run thread {}", currentThread.getName());
                    multipleConnectionStorage.delete("us", "someKey");
                } catch (StorageException exception) {
                    LOG.error("Exception in positiveHttpPoolTest", exception);
                    return exception;
                }
                return null;
            }));
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        int successfulTaskCount = 0;
        for (Future<StorageException> one : futureList) {
            assertTrue(one.isDone());
            if (one.get() == null) {
                successfulTaskCount += 1;
            }
        }
        assertEquals(taskCount, successfulTaskCount);
        server.stop(0);
    }
}
