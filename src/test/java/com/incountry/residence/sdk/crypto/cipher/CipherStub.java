package com.incountry.residence.sdk.crypto.cipher;

import com.incountry.residence.sdk.tools.crypto.ciphers.AbstractCipher;
import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;

import java.nio.charset.StandardCharsets;

public class CipherStub extends AbstractCipher {

    public CipherStub(String code) {
        super(code, StandardCharsets.UTF_8);
    }

    @Override
    public String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) {
        return new String(secretKey.getSecretBytes(), StandardCharsets.UTF_8) + new String(textBytes, StandardCharsets.UTF_8);
    }

    @Override
    public String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey) {
            boolean isCorrect = true;
            for (int i = 0; i < secretKey.getSecretBytes().length; i++) {
                if (cipherTextBytes[i] != secretKey.getSecretBytes()[i]) {
                    isCorrect = false;
                }
            }

            if (!isCorrect) {
                throw new RuntimeException("Wrong secret key for ciphered text");
            }
            int textLength = cipherTextBytes.length - secretKey.getSecretBytes().length;
            byte[] textBytes = new byte[textLength];
            System.arraycopy(cipherTextBytes, secretKey.getSecretBytes().length, textBytes, 0, textLength);
            return new String(textBytes, StandardCharsets.UTF_8);
    }
}
