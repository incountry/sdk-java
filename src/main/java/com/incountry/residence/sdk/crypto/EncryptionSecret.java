package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

public class EncryptionSecret extends Secret {
    public EncryptionSecret(byte[] secretBytes, int version) throws StorageClientException {
        super(secretBytes, version);
    }

    @Override
    public String toString() {
        return toString(EncryptionSecret.class.getSimpleName());
    }
}
