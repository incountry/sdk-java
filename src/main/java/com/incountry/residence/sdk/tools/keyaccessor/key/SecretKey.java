package com.incountry.residence.sdk.tools.keyaccessor.key;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SecretKey {
    private static final Logger LOG = LogManager.getLogger(SecretKey.class);
    private static final int KEY_LENGTH = 32;

    private String secret;
    private int version;
    private boolean isKey;

    public SecretKey(String secret, int version, boolean isKey) {
        validateSecretKey(secret, version, isKey);
        this.version = version;
        this.secret = secret;
        this.isKey = isKey;
    }

    public String getSecret() {
        return secret;
    }

    public int getVersion() {
        return version;
    }

    public Boolean getIsKey() {
        return isKey;
    }

    public static void validateSecretKey(String secret, int version, boolean isKey) {
        if (version < 0) {
            String message = "Version must be >= 0";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        if (secret == null || secret.isEmpty()) {
            String message = "Secret can't be null";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        if (isKey && secret.length() != KEY_LENGTH) {
            String message = "Wrong default key length. Should be " + KEY_LENGTH + " characters ‘utf8’ encoded string";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }
}
