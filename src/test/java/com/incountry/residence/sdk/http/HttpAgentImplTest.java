package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpAgentImplTest {

    @Test
    public void testNullEndpointException() {
        HttpAgent agent = new HttpAgentImpl("apiKey", "envId", StandardCharsets.UTF_8);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>()));
    }

    @Test
    public void testNullApiKeyException() {
        HttpAgent agent = new HttpAgentImpl(null, "envId", StandardCharsets.UTF_8);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>()));
    }

    @Test
    public void testNullEnvIdException() {
        HttpAgent agent = new HttpAgentImpl("apiKey", null, StandardCharsets.UTF_8);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", new HashMap<>()));
    }

    @Test
    public void testFakeEndpointException() {
        HttpAgent agent = new HttpAgentImpl("apiKey", "envId", StandardCharsets.UTF_8);
        assertThrows(StorageServerException.class, () -> agent.request("https://" + UUID.randomUUID().toString() + "localhost", "GET", "someBody", new HashMap<>()));
    }
}