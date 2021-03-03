package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;

public class EncryptionKey extends Secret {

    private static final ValidationHelper HELPER = new ValidationHelper(LogManager.getLogger(EncryptionKey.class));
    private static final int KEY_LENGTH = 32;
    private static final String MSG_ERR_LENGTH = "Wrong key length for encryption key . Should be "
            + KEY_LENGTH + "-byte array";

    public EncryptionKey(int version, byte[] secretBytes) throws StorageClientException {
        super(version, secretBytes);
        boolean invalidLength = secretBytes.length != KEY_LENGTH;
        HELPER.check(StorageClientException.class, invalidLength, MSG_ERR_LENGTH);
    }

    @Override
    public String toString() {
        return toString(EncryptionKey.class.getName());
    }
}
