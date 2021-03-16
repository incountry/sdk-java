package com.incountry.residence.sdk.version;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class SdkInfo {
    public static final String PROPERTY_VERSION = "artifactVersion";
    public static final String PROPERTY_USER_AGENT = "userAgent";

    private static final String UNKNOWN = "unknown";
    private static final String CLIENT_PROPERTY_FILE = "sdk.properties";
    private static final Logger LOGGER = LogManager.getLogger(SdkInfo.class);
    private static SdkInfo instance;

    private final Properties properties = new Properties();

    public String getProperty(String code) {
        return (properties.getProperty(code) != null) ? properties.getProperty(code) : UNKNOWN;
    }

    private SdkInfo() {
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(CLIENT_PROPERTY_FILE));
        } catch (IOException | NullPointerException e) {
            LOGGER.error("Unable to load client properties ({}).", CLIENT_PROPERTY_FILE);
        }
    }

    public static SdkInfo getInstance() {
        if (instance == null) {
            instance = new SdkInfo();
        }
        return instance;
    }
}
