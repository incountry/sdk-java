package com.incountry.residence.sdk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class ClientInfo {
    private static final String UNKNOWN_VERSION = "unknown";
    private static final String VERSION_PROPERTY = "clientVersion";
    private static final String CLIENT_PROPERTY_FILE = "client.properties";
    private static final Logger LOGGER = LogManager.getLogger(ClientInfo.class);
    private static final Properties CLIENT_PROPERTIES = new Properties();
    private static ClientInfo instance;

    public String version() {
        return (CLIENT_PROPERTIES.getProperty(VERSION_PROPERTY) != null) ? CLIENT_PROPERTIES.getProperty(VERSION_PROPERTY) : UNKNOWN_VERSION;
    }

    private ClientInfo() {
        try {
            CLIENT_PROPERTIES.load(this.getClass().getClassLoader().getResourceAsStream(CLIENT_PROPERTY_FILE));
        } catch (IOException | NullPointerException e) {
            LOGGER.error("Unable to load client properties ({}).", CLIENT_PROPERTY_FILE);
        }
    }

    public static ClientInfo getInstance() {
        if (instance == null) {
            instance = new ClientInfo();
        }
        return instance;
    }
}

