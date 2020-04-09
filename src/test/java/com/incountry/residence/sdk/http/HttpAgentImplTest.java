package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpAgentImplTest {

    @Test
    public void testIoException() {
        HttpAgent agent = new HttpAgentImpl("apiKey", "envId", Charset.defaultCharset());
        assertThrows(IOException.class, () -> agent.request(null, "GET", "someBody", true));
        assertThrows(MalformedURLException.class, () -> agent.request(null, "GET", "someBody", true));
    }
}
