package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

public class CustomEncryptionKey extends Secret {
    public CustomEncryptionKey(byte[] secretBytes, int version) throws StorageClientException {
        super(secretBytes, version);
    }

    @Override
    public String toString() {
        return toString(CustomEncryptionKey.class.getSimpleName());
    }
}
