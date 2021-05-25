package com.incountry.residence.sdk.crypto.testimpl;


import com.incountry.residence.sdk.crypto.AbstractCipher;
import com.incountry.residence.sdk.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

import java.util.UUID;

public class InvalidCipher extends AbstractCipher {

    public InvalidCipher() throws StorageClientException {
        super(InvalidCipher.class.getName());
    }

    @Override
    public String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) {
        return UUID.randomUUID().toString();
    }

    @Override
    public String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey) {
        return UUID.randomUUID().toString();
    }
}
