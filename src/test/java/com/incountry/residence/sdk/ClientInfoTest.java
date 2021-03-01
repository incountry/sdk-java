package com.incountry.residence.sdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ClientInfoTest {

    Properties props = new Properties();

    @BeforeEach
    void init() throws IOException {
        props.load(getClass().getClassLoader().getResourceAsStream("client.properties"));
    }

    @Test
    void versionIsKnownTest() {
        assertNotEquals("unknown", ClientInfo.getInstance().version());

    }

    @Test
    void versionIsValidTest() {
        assertEquals(props.getProperty("clientVersion"), ClientInfo.getInstance().version());
    }
}