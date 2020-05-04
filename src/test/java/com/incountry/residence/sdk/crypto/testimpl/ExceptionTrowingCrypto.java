package com.incountry.residence.sdk.crypto.testimpl;

import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;

public class ExceptionTrowingCrypto implements Crypto {
    private boolean current;

    public ExceptionTrowingCrypto(boolean current) {
        this.current = current;
    }

    @Override
    public String encrypt(String text, SecretKey secretKey) {
        throw new NullPointerException();
    }

    @Override
    public String decrypt(String cipherText, SecretKey secretKey) {
        throw new NullPointerException();
    }

    @Override
    public String getVersion() {
        return ExceptionTrowingCrypto.class.getSimpleName();
    }

    @Override
    public boolean isCurrent() {
        return current;
    }
}
