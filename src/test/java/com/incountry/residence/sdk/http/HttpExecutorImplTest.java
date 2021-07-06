package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.Storage;
import com.incountry.residence.sdk.StorageConfig;
import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpExecutor;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpExecutorImpl;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static com.incountry.residence.sdk.StorageConfig.DEFAULT_RETRY_BASE_DELAY;
import static com.incountry.residence.sdk.StorageConfig.DEFAULT_RETRY_MAX_DELAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpExecutorImplTest {

    private static final Logger LOG = LogManager.getLogger(HttpExecutorImplTest.class);

    private static final int PORT = 8769;
    private static final String ENDPOINT = "http://localhost:" + PORT;
    private static final TokenClient TOKEN_CLIENT = (force, audience, region) -> "<token>";
    private static final String APPLICATION_JSON = "application/json";

    @Test
    void testWithIllegalUrl() {
        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        RequestParameters params = new RequestParameters("GET", APPLICATION_JSON, null, null);
        StorageClientException ex = assertThrows(StorageClientException.class, () ->
                agent.request(null, "someBody", null, null, 0, params));
        assertEquals("URL can't be null", ex.getMessage());
    }

    @Test
    void testFakeEndpointException() {
        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        String url = "https://" + UUID.randomUUID() + ".localhost";
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(url,
                "someBody", null, null, 0, new RequestParameters("GET")));
        assertEquals("Server request error: [URL=" + url + ", method=GET]", ex.getMessage());
    }

    @RepeatedTest(3)
    void testWithFakeHttpServer(RepetitionInfo repeatInfo) throws IOException, StorageException {
        iterateLogLevel(repeatInfo, HttpExecutorImpl.class);
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();
        HttpExecutor agent;
        if (repeatInfo.getCurrentRepetition() == 2) {
            agent = new HttpExecutorImpl(TOKEN_CLIENT, null, HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        } else {
            agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        }
        RequestParameters params = new RequestParameters("POST");
        assertNotNull(agent.request(ENDPOINT, "<body>", null, null, 0, params).getContent());
        assertNotNull(agent.request(ENDPOINT, "", null, null, 0, params).getContent());
        StorageClientException ex = assertThrows(StorageClientException.class, () ->
                agent.request(ENDPOINT, null, null, null, 0, params));
        assertEquals("HTTP request body can't be null", ex.getMessage());
        server.stop(0);
    }

    @Test
    void testPatchMethodWithFakeHttpServer() throws IOException, StorageException {
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();

        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        RequestParameters params = new RequestParameters("PATCH");
        assertNotNull(agent.request(ENDPOINT, "<body>", null, null, 0, params).getContent());
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerFileUpload() throws IOException, StorageException {
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();

        String postMethod = "POST";
        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        RequestParameters params = new RequestParameters(postMethod, APPLICATION_JSON, genStream(), "file.txt");
        assertNotNull(agent.request(ENDPOINT, null, null, null, 0, params).getContent());

        String putMethod = "PUT";
        RequestParameters params4 = new RequestParameters(putMethod, APPLICATION_JSON, genStream(), "file.txt");
        assertNotNull(agent.request(ENDPOINT, null, null, null, 0, params4).getContent());
        RequestParameters params5 = new RequestParameters(putMethod, "", genStream(), "file.txt");
        assertNotNull(agent.request(ENDPOINT, null, null, null, 0, params5).getContent());
        RequestParameters params6 = new RequestParameters(putMethod, null, genStream(), "file.txt");
        assertNotNull(agent.request(ENDPOINT, null, null, null, 0, params6).getContent());
        server.stop(0);
    }

    private InputStream genStream() {
        return new ByteArrayInputStream("<body>".getBytes(StandardCharsets.UTF_8));
    }

    private InputStream genEmptyStream() {
        return new ByteArrayInputStream(new byte[0]);
    }


    @Test
    void negativeTestWithNullRequestParameters() {
        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        StorageClientException ex = assertThrows(StorageClientException.class, () ->
                agent.request(ENDPOINT, "<body>", null, null, 0, null));
        assertEquals("Request parameters can't be null", ex.getMessage());
    }

    @Test
    void testDownloadFile() throws IOException, StorageException {
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT, "/attachments/file_id");
        server.start();

        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        String url = "http://localhost:8769/attachments/file_id";
        RequestParameters params = new RequestParameters("GET");
        ApiResponse response = agent.request(url, "<body>", null, null, 0, params);
        assertNotNull(response);
        assertNull(response.getContent());
        assertNotNull(response.getInputStream());
        server.stop(0);

        server = new FakeHttpServer("{}", respCode, PORT, "/attachments");
        server.start();
        response = agent.request("http://localhost:8769/attachments/", "<body>", null, null, 0, new RequestParameters("GET"));
        assertNotNull(response);
        assertNull(response.getContent());
        assertNotNull(response.getInputStream());
        server.stop(0);
    }

    @Test
    void testWithDifferentUrlsAndMethods() throws IOException, StorageException {
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT, "/attachments/file_id/meta");
        server.start();

        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        assertNotNull(agent.request("http://localhost:8769/attachments/file_id/meta", "<body>", null, null, 0, new RequestParameters("GET")).getContent());
        agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        assertNotNull(agent.request("http://localhost:8769/attachments/file_id/meta", "<body>", null, null, 0, new RequestParameters("POST")).getContent());
        server.stop(0);

        server = new FakeHttpServer("{}", 405, PORT, "/attachments/file_id");
        server.start();
        HttpExecutor agent1 = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        ApiResponse response = agent1.request("http://localhost:8769/attachments/file_id", "<body>", null, null, 0, new RequestParameters("POST"));
        assertEquals(405, response.getResponseCode());
        server.stop(0);
    }

    @Test
    void testWithNullResponse() throws IOException, StorageException {
        int respCode = 204;
        FakeHttpServer server = new FakeHttpServer("", respCode, PORT, "/attachments/file_id");
        server.start();

        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        ApiResponse response = agent.request("http://localhost:8769/attachments/file_id", "<body>", null, null, 0, new RequestParameters("DELETE"));
        assertNotNull(response);
        assertNull(response.getInputStream());
        assertNull(response.getContent());
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerIgnoredStatus() throws IOException, StorageServerException, StorageClientException {
        int respCode = 404;
        FakeHttpServer server = new FakeHttpServer((String) null, respCode, PORT);
        server.start();
        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        assertTrue(agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters("POST")).getContent().isEmpty());
        server.stop(0);
    }

    @Test
    void testRetry() throws IOException, StorageServerException, StorageClientException {
        FakeHttpServer server = new FakeHttpServer("{}", Arrays.asList(401, 200, 401), PORT);
        server.start();
        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        assertEquals(200, agent.request(ENDPOINT, "<body>", null, null, 1, new RequestParameters("POST")).getResponseCode());
        assertEquals(401, agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters("POST")).getResponseCode());
        server.stop(0);
    }

    @Test
    void negativeTestWithIllegalUrl() {
        HttpExecutor agent = new HttpExecutorImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault(), DEFAULT_RETRY_BASE_DELAY, DEFAULT_RETRY_MAX_DELAY);
        String url = " ";
        StorageClientException ex = assertThrows(StorageClientException.class, ()
                -> agent.request(url, "someBody", null, null, 0, new RequestParameters("GET")));
        assertEquals("URL error", ex.getMessage());
        assertEquals(URISyntaxException.class, ex.getCause().getClass());
        assertEquals("Illegal character in path at index 0: " + url, ex.getCause().getMessage());
    }

    @Test
    void positiveHttpPoolTest() throws IOException, StorageClientException, InterruptedException, ExecutionException, StorageCryptoException {
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
                .setEnvironmentId(envId)
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
