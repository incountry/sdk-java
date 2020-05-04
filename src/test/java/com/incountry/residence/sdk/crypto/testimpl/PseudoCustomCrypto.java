package com.incountry.residence.sdk.crypto.testimpl;

import com.incountry.residence.sdk.tools.crypto.DefaultCrypto;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;

import java.nio.charset.StandardCharsets;

public class PseudoCustomCrypto extends DefaultCrypto {
    private boolean current;
    private Integer encryptBeforeException;
    private Integer decryptBeforeException;
    private boolean exceptionType;

    public PseudoCustomCrypto(boolean current) {
        super(StandardCharsets.UTF_8);
        this.current = current;
    }

    public PseudoCustomCrypto(boolean current, Integer encryptBeforeException, Integer decryptBeforeException, boolean exceptionType) {
        super(StandardCharsets.UTF_8);
        this.current = current;
        this.exceptionType = exceptionType;
        this.encryptBeforeException = encryptBeforeException;
        this.decryptBeforeException = decryptBeforeException;
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
        if (encryptBeforeException == null) {
            return super.encrypt(text, secretKey);
        } else if (encryptBeforeException == 0) {
            if (exceptionType) {
                throw new StorageCryptoException("");
            } else {
                throw new NullPointerException();
            }
        } else {
            encryptBeforeException -= 1;
            return super.encrypt(text, secretKey);
        }
    }

    @Override
    public String decrypt(String cipherText, SecretKey secretKey) throws StorageCryptoException {
        if (decryptBeforeException == null) {
            return super.decrypt(cipherText, secretKey);
        } else if (decryptBeforeException == 0) {
            if (exceptionType) {
                throw new StorageCryptoException("");
            } else {
                throw new NullPointerException();
            }
        } else {
            decryptBeforeException -= 1;
            return super.decrypt(cipherText, secretKey);
        }
    }
}
