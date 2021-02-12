package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EncryptionKey extends Secret {

    private static final Logger LOG = LogManager.getLogger(EncryptionKey.class);

    private static final int KEY_LENGTH = 32;

    private static final String MSG_ERR_KEY_LEN = "Wrong key length for secret key with 'isKey==true'. Should be "
            + KEY_LENGTH + "-byte array";

    public EncryptionKey(int version, byte[] secretBytes) throws StorageClientException {
        super(version, secretBytes);
        if (secretBytes.length != KEY_LENGTH) {
            LOG.error(MSG_ERR_KEY_LEN);
            throw new StorageClientException(MSG_ERR_KEY_LEN);
        }
    }

    @Override
    public String toString() {
        return toString(this.getClass().getName());
    }
}
