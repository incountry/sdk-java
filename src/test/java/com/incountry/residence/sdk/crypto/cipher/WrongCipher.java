package com.incountry.residence.sdk.crypto.cipher;

import com.incountry.residence.sdk.tools.crypto.ciphers.AbstractCipher;
import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class WrongCipher extends AbstractCipher {

    public WrongCipher(String code) {
        super(code, StandardCharsets.UTF_8);
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
