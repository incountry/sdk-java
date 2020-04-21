package com.incountry.residence.sdk.tools.keyaccessor.key;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SecretKey {
    private static final Logger LOG = LogManager.getLogger(SecretKey.class);

    private static final int KEY_LENGTH = 32;

    private static final String MSG_ERR_VERSION = "Version must be >= 0";
    private static final String MSG_ERR_NULL_SECRET = "Secret can't be null";
    private static final String MSG_ERR_KEY_LEN = "Wrong default key length. Should be "
            + KEY_LENGTH + " characters ‘utf8’ encoded string";

    private String secret;
    private int version;
    private boolean isKey;

    public SecretKey(String secret, int version, boolean isKey) throws StorageClientException {
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

    public static void validateSecretKey(String secret, int version, boolean isKey) throws StorageClientException {
        if (version < 0) {
            LOG.error(MSG_ERR_VERSION);
            throw new StorageClientException(MSG_ERR_VERSION);
        }
        if (secret == null || secret.isEmpty()) {
            LOG.error(MSG_ERR_NULL_SECRET);
            throw new StorageClientException(MSG_ERR_NULL_SECRET);
        }
        if (isKey && secret.length() != KEY_LENGTH) {
            LOG.error(MSG_ERR_KEY_LEN);
            throw new StorageClientException(MSG_ERR_KEY_LEN);
        }
    }
}
