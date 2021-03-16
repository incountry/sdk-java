package com.incountry.residence.sdk;

import com.incountry.residence.sdk.version.SdkInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SdkInfoTest {

    @Test
    void knownPropertiesTest() {
        assertNotEquals("unknown", SdkInfo.getInstance().getProperty(SdkInfo.PROPERTY_VERSION));
        assertNotEquals("unknown", SdkInfo.getInstance().getProperty(SdkInfo.PROPERTY_USER_AGENT));
    }

    @Test
    void versionIsValidTest() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("sdk.properties"));
        assertEquals(props.getProperty("artifactVersion"), SdkInfo.getInstance().getProperty(SdkInfo.PROPERTY_VERSION));
        assertEquals(props.getProperty("userAgent"), SdkInfo.getInstance().getProperty(SdkInfo.PROPERTY_USER_AGENT));
    }
}