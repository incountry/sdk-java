package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;

public class CryptoStub implements Crypto {

    private boolean current;

    public CryptoStub(boolean current) {
        this.current = current;
    }

    @Override
    public String encrypt(String text, SecretKey secretKey) {
        return text != null ? text + ":" + secretKey.getSecret() : secretKey.getSecret();
    }

    @Override
    public String decrypt(String cipherText, SecretKey secretKey) {
        return cipherText.equals(secretKey.getSecret()) ? null : cipherText.substring(0, cipherText.lastIndexOf(":" + secretKey.getSecret()));
    }

    @Override
    public String getVersion() {
        return CryptoStub.class.getSimpleName();
    }

    @Override
    public boolean isCurrent() {
        return current;
    }
}
