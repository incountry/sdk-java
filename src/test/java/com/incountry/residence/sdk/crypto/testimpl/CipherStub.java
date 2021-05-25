package com.incountry.residence.sdk.crypto.testimpl;

import com.incountry.residence.sdk.crypto.AbstractCipher;
import com.incountry.residence.sdk.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

public class CipherStub extends AbstractCipher {

    public CipherStub() throws StorageClientException {
        super(CipherStub.class.getName());
    }

    public CipherStub(String name) throws StorageClientException {
        super(name);
    }


    @Override
    public String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) {
        return new String(textBytes, charset) +
                ":@:" +
                secretKey.toString();
    }

    @Override
    public String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey) {
        String encryptedString = new String(cipherTextBytes, charset);
        String[] strings = encryptedString.split(":@:");
        return strings[0];
    }
}
