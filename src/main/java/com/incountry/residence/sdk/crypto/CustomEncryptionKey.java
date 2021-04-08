package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

public class CustomEncryptionKey extends Secret {
    public CustomEncryptionKey(int version, byte[] secretBytes) throws StorageClientException {
        super(version, secretBytes);
    }

    @Override
    public String toString() {
        return toString(CustomEncryptionKey.class.getSimpleName());
    }
}
