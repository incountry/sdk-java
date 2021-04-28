package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;

public abstract class Secret {
    private static final ValidationHelper HELPER = new ValidationHelper(LogManager.getLogger(Secret.class));
    private static final String MSG_ERR_VERSION = "Version must be >= 0";
    private static final String MSG_ERR_NULL_SECRET = "Secret can't be null or empty";
    private final int version;
    private final byte[] secretBytes;

    public int getVersion() {
        return version;
    }

    public byte[] getSecretBytes() {
        return secretBytes;
    }

    protected Secret(byte[] secretBytes, int version) throws StorageClientException {
        validateAbstractSecret(version, secretBytes);
        this.version = version;
        this.secretBytes = secretBytes;
    }

    private static void validateAbstractSecret(int version, byte[] secretBytes) throws StorageClientException {
        HELPER.check(StorageClientException.class, version < 0, MSG_ERR_VERSION);
        boolean isInvalidSecret = secretBytes == null || secretBytes.length == 0;
        HELPER.check(StorageClientException.class, isInvalidSecret, MSG_ERR_NULL_SECRET);
    }


    protected String toString(String typeName) {
        return typeName + "{" +
                "version=" + version +
                ", secretBytes=HASH[" + Arrays.hashCode(secretBytes) + ']' +
                '}';
    }
}
