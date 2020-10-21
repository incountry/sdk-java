package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.Storage;
import com.incountry.residence.sdk.StorageConfig;
import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponseCodes;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.ApiKeyTokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
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
    private static final String APPLICATION_JSON = "application/json";

    @Test
    void testWithIllegalUrl() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageClientException ex = assertThrows(StorageClientException.class, () -> agent.request(null, "someBody", null, null, 0, new RequestParameters("GET", new HashMap<>(), APPLICATION_JSON, false, null)));
        assertEquals("URL can't be null", ex.getMessage());
    }

    @Test
    void testFakeEndpointException() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        String url = "https://" + UUID.randomUUID().toString() + ".localhost";
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(url,
                "someBody", null, null, 0, new RequestParameters("GET", new HashMap<>())));
        assertEquals("Server request error: [URL=" + url + ", method=GET]", ex.getMessage());
    }

    @RepeatedTest(3)
    void testWithFakeHttpServer(RepetitionInfo repeatInfo) throws IOException, StorageException {
        iterateLogLevel(repeatInfo, HttpAgentImpl.class);
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();
        HttpAgent agent;
        if (repeatInfo.getCurrentRepetition() == 2) {
            agent = new HttpAgentImpl(TOKEN_CLIENT, null, HttpClients.createDefault());
        } else {
            agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        }
        assertNotNull(agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters("POST", ApiResponseCodes.DELETE)).getContent());
        assertNotNull(agent.request(ENDPOINT, "", null, null, 0, new RequestParameters("POST", ApiResponseCodes.DELETE)).getContent());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, null, null, null, 0, new RequestParameters("POST", ApiResponseCodes.DELETE)));
        assertEquals("Server request error: POST", ex.getMessage());
        server.stop(0);
    }

    @Test
    void testPatchMethodWithFakeHttpServer() throws IOException, StorageException {
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();

        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        assertNotNull(agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters("PATCH", ApiResponseCodes.DELETE)).getContent());
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerFileUpload() throws IOException, StorageException {
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT);
        server.start();

        String postMethod = "POST";
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        assertNotNull(agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters(postMethod, ApiResponseCodes.DELETE, APPLICATION_JSON, true, "file.txt")).getContent());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, "", null, null, 0, new RequestParameters(postMethod, ApiResponseCodes.DELETE, APPLICATION_JSON, true, "file.txt")));
        assertEquals("Server request error: POST", ex.getMessage());
        assertEquals(StorageClientException.class, ex.getCause().getClass());
        assertEquals("Body can't be null", ex.getCause().getMessage());
        ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, null, null, null, 0, new RequestParameters(postMethod, ApiResponseCodes.DELETE, APPLICATION_JSON, true, "file.txt")));
        assertEquals("Server request error: POST", ex.getMessage());
        assertEquals(StorageClientException.class, ex.getCause().getClass());
        assertEquals("Body can't be null", ex.getCause().getMessage());

        String putMethod = "PUT";
        assertNotNull(agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters(putMethod, ApiResponseCodes.DELETE, APPLICATION_JSON, true, "file.txt")).getContent());
        assertNotNull(agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters(putMethod, ApiResponseCodes.DELETE, "", true, "file.txt")).getContent());
        assertNotNull(agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters(putMethod, ApiResponseCodes.DELETE, null, true, "file.txt")).getContent());
        ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, "", null, null, 0, new RequestParameters(putMethod, ApiResponseCodes.DELETE, APPLICATION_JSON, true, "file.txt")));
        assertEquals("Server request error: PUT", ex.getMessage());
        assertEquals(StorageClientException.class, ex.getCause().getClass());
        assertEquals("Body can't be null", ex.getCause().getMessage());
        ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, null, null, null, 0, new RequestParameters(putMethod, ApiResponseCodes.DELETE, APPLICATION_JSON, true, "file.txt")));
        assertEquals("Server request error: PUT", ex.getMessage());
        assertEquals(StorageClientException.class, ex.getCause().getClass());
        assertEquals("Body can't be null", ex.getCause().getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestWithNullRequestParameters() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageClientException ex = assertThrows(StorageClientException.class, () -> agent.request(ENDPOINT, "<body>", null, null, 0, null));
        assertEquals("Request parameters can't be null", ex.getMessage());
    }

    @Test
    void testDownloadFile() throws IOException, StorageException {
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT, "/attachments/file_id");
        server.start();

        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        assertNotNull(agent.request("http://localhost:8769/attachments/file_id", "<body>", null, null, 0, new RequestParameters("GET", ApiResponseCodes.DELETE)).getContent());
        server.stop(0);

        server = new FakeHttpServer("{}", respCode, PORT, "/attachments");
        server.start();
        assertNotNull(agent.request("http://localhost:8769/attachments/", "<body>", null, null, 0, new RequestParameters("GET", ApiResponseCodes.DELETE)).getContent());
        server.stop(0);
    }

    @Test
    void testWithDifferentUrlsAndMethods() throws IOException, StorageException {
        int respCode = 200;
        FakeHttpServer server = new FakeHttpServer("{}", respCode, PORT, "/attachments/file_id/meta");
        server.start();

        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        assertNotNull(agent.request("http://localhost:8769/attachments/file_id/meta", "<body>", null, null, 0, new RequestParameters("GET", ApiResponseCodes.DELETE)).getContent());
        agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        assertNotNull(agent.request("http://localhost:8769/attachments/file_id/meta", "<body>", null, null, 0, new RequestParameters("POST", ApiResponseCodes.DELETE)).getContent());
        server.stop(0);

        server = new FakeHttpServer("{}", 405, PORT, "/attachments/file_id");
        server.start();
        HttpAgent agent1 = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent1.request("http://localhost:8769/attachments/file_id", "<body>", null, null, 0, new RequestParameters("POST", ApiResponseCodes.DELETE)));
        assertTrue(ex.getMessage().contains("Code=405"));
        server.stop(0);
    }

    @Test
    void testWithNullResponse() throws IOException, StorageException {
        int respCode = 204;
        FakeHttpServer server = new FakeHttpServer("", respCode, PORT, "/attachments/file_id");
        server.start();

        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        assertNotNull(agent.request("http://localhost:8769/attachments/file_id", "<body>", null, null, 0, new RequestParameters("DELETE", ApiResponseCodes.DELETE_ATTACHMENT)).getContent());
        server.stop(0);
    }


    @Test
    void testWithFakeHttpServerBadCode() throws IOException {
        int respCode = 555;
        String content = "{}";
        FakeHttpServer server = new FakeHttpServer(content, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters("POST", ApiResponseCodes.DELETE)));
        assertEquals(String.format("Code=%d, endpoint=[%s], content=[%s]", respCode, ENDPOINT, content), ex.getMessage());
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerBadCodeRefreshToken() throws IOException, StorageServerException, StorageClientException {
        List<Integer> respCodeList = Arrays.asList(401, 401, 401, 401, 401, 200);
        String content = "{}";
        String method = "POST";
        String expectedErrorString = String.format("Code=401, endpoint=[%s], content=[%s]", ENDPOINT, content);
        FakeHttpServer server = new FakeHttpServer(content, respCodeList, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        StorageServerException ex1 = assertThrows(StorageServerException.class, () ->
                agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters(method, ApiResponseCodes.DELETE)));
        assertEquals(expectedErrorString, ex1.getMessage());
        StorageServerException ex2 = assertThrows(StorageServerException.class, () ->
                agent.request(ENDPOINT, "<body>", null, null, 2, new RequestParameters(method, ApiResponseCodes.DELETE)));
        assertEquals(expectedErrorString, ex2.getMessage());
        assertEquals(content, agent.request(ENDPOINT, "<body>", null, null, 1, new RequestParameters(method, ApiResponseCodes.DELETE)).getContent());
        server.stop(0);
    }

    @Test
    void testWithFakeHttpServerIgnoredStatus() throws IOException, StorageServerException, StorageClientException {
        int respCode = 404;
        FakeHttpServer server = new FakeHttpServer((String) null, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        assertNull(agent.request(ENDPOINT, "<body>", null, null, 0, new RequestParameters("POST", ApiResponseCodes.READ)).getContent());
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
                -> agent.request(ENDPOINT, "someBody", null, null, 0, new RequestParameters("GET", new HashMap<>())));
        assertEquals(String.format("Code=%d, endpoint=[%s], content=[ok]", respCode, ENDPOINT), ex.getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestWithIllegalUrl() {
        HttpAgent agent = new HttpAgentImpl(TOKEN_CLIENT, "envId", HttpClients.createDefault());
        String url = " ";
        StorageServerException ex = assertThrows(StorageServerException.class, ()
                -> agent.request(url, "someBody", null, null, 0, new RequestParameters("GET", new HashMap<>())));
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
