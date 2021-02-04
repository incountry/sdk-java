package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

public class CustomEncryptionKey extends Secret {

    public CustomEncryptionKey(int version, byte[] secretBytes) throws StorageClientException {
        super(version, secretBytes);
    }

    @Override
    public String toString() {
        return toString(this.getClass().getName());
    }
}
