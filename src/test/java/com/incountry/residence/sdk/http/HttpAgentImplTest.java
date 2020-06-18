package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpAgentImplTest {
    private static final int PORT = 8769;
    private static final String ENDPOINT = "http://localhost:" + PORT;

    @Test
    void testException() {
        HttpAgent agent = new HttpAgentImpl("apiKey", "envId", StandardCharsets.UTF_8);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>()));
    }

    @Test
    void testExpectedExceptionInsteadOfNPE() throws IOException {
        int respCode = 201;
        String response = "ok";
        FakeHttpServer server = new FakeHttpServer(response, respCode, PORT);
        server.start();
        HttpAgent agent = new HttpAgentImpl("apiKey", "envId", StandardCharsets.UTF_8);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> agent.request(ENDPOINT, "GET", "someBody", new HashMap<>()));
        assertEquals("Code=201, endpoint=[http://localhost:8769], content=[]", ex.getMessage());
        server.stop(0);
    }
}
