package com.incountry.residence.sdk.crypto.testimpl;

import com.incountry.residence.sdk.tools.crypto.DefaultCrypto;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;

import java.nio.charset.StandardCharsets;

public class PseudoCustomCrypto extends DefaultCrypto {
    private boolean current;
    private Integer encryptCountBeforeException;
    private Integer decryptCountBeforeException;
    private boolean exceptionType;

    public PseudoCustomCrypto(boolean current) {
        super(StandardCharsets.UTF_8);
        this.current = current;
    }

    public PseudoCustomCrypto(boolean current, Integer encryptCountBeforeException, Integer decryptCountBeforeException, boolean exceptionType) {
        super(StandardCharsets.UTF_8);
        this.current = current;
        this.exceptionType = exceptionType;
        this.encryptCountBeforeException = encryptCountBeforeException;
        this.decryptCountBeforeException = decryptCountBeforeException;
    }


    @Override
    public boolean isCurrent() {
        return current;
    }

    @Override
    public String getVersion() {
        return PseudoCustomCrypto.class.getSimpleName();
    }


    @Override
    public String encrypt(String text, SecretKey secretKey) throws StorageCryptoException {
        if (encryptCountBeforeException == null) {
            return super.encrypt(text, secretKey);
        } else if (encryptCountBeforeException == 0) {
            if (exceptionType) {
                throw new StorageCryptoException("");
            } else {
                throw new NullPointerException();
            }
        } else {
            encryptCountBeforeException -= 1;
            return super.encrypt(text, secretKey);
        }
    }

    @Override
    public String decrypt(String cipherText, SecretKey secretKey) throws StorageCryptoException {
        if (decryptCountBeforeException == null) {
            return super.decrypt(cipherText, secretKey);
        } else if (decryptCountBeforeException == 0) {
            if (exceptionType) {
                throw new StorageCryptoException("");
            } else {
                throw new NullPointerException();
            }
        } else {
            decryptCountBeforeException -= 1;
            return super.decrypt(cipherText, secretKey);
        }
    }
}
