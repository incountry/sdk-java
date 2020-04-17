package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpAgentImplTest {

    @Test
    public void testException() {
        HttpAgent agent = new HttpAgentImpl("apiKey", "envId", StandardCharsets.UTF_8);
        assertThrows(StorageServerException.class, () -> agent.request(null, "GET", "someBody", true));
    }
}