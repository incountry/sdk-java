package com.incountry.residence.sdk.tools.keyaccessor.key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecretKey {
    private static final Logger LOG = LoggerFactory.getLogger(SecretKey.class);

    private String secret;
    private int version;
    private Boolean isKey;

    public SecretKey() {
    }

    public SecretKey(String secret, int version, Boolean isKey) {
        setVersion(version);
        this.secret = secret;
        this.isKey = isKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        if (version < 0) {
            String message = "Version must be >= 0";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        this.version = version;
    }

    public Boolean getIsKey() {
        return isKey;
    }

    public void setIsKey(Boolean key) {
        isKey = key;
    }
}
