package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

public class EncryptionSecret extends Secret {
    public EncryptionSecret(int version, byte[] secretBytes) throws StorageClientException {
        super(version, secretBytes);
    }

    @Override
    public String toString() {
        return toString(EncryptionSecret.class.getName());
    }
}
