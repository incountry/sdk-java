package com.incountry.residence.sdk.crypto.testimpl;

import com.incountry.residence.sdk.crypto.AbstractCipher;
import com.incountry.residence.sdk.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.crypto.cipher.AesGcmPbkdf10kBase64Cipher;
import com.incountry.residence.sdk.tools.crypto.cipher.Cipher;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

import java.nio.charset.StandardCharsets;

public class PseudoCustomCipher extends AbstractCipher {
    private Integer encryptCountBeforeException;
    private Integer decryptCountBeforeException;
    private Cipher defaultCipher = new AesGcmPbkdf10kBase64Cipher(StandardCharsets.UTF_8);
    private boolean exceptionType;

    public PseudoCustomCipher() throws StorageClientException {
        super(PseudoCustomCipher.class.getName());
    }

    public PseudoCustomCipher(Integer encryptCountBeforeException, Integer decryptCountBeforeException, boolean exceptionType) throws StorageClientException {
        super(PseudoCustomCipher.class.getSimpleName());
        this.exceptionType = exceptionType;
        this.encryptCountBeforeException = encryptCountBeforeException;
        this.decryptCountBeforeException = decryptCountBeforeException;
    }

    @Override
    public String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) throws StorageClientException, StorageCryptoException {
        if (encryptCountBeforeException == null) {
            return defaultCipher.encrypt(new String(textBytes, charset), secretKey).getData().substring(2);
        } else if (encryptCountBeforeException == 0) {
            if (exceptionType) {
                throw new StorageCryptoException("");
            } else {
                throw new NullPointerException();
            }
        } else {
            encryptCountBeforeException -= 1;
            return defaultCipher.encrypt(new String(textBytes, charset), secretKey).getData().substring(2);
        }
    }

    @Override
    public String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey) throws StorageClientException, StorageCryptoException {
        if (decryptCountBeforeException == null) {
            return defaultCipher.decrypt(new String(cipherTextBytes, charset), secretKey);
        } else if (decryptCountBeforeException == 0) {
            if (exceptionType) {
                throw new StorageCryptoException("");
            } else {
                throw new NullPointerException();
            }
        } else {
            decryptCountBeforeException -= 1;
            return defaultCipher.decrypt(new String(cipherTextBytes, charset), secretKey);
        }
    }
}
