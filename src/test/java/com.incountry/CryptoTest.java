package com.incountry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class CryptoTest {
    @Test
    public void testEncryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("supersecret");
        String plainText = "some plain text";
        String encrypted = crypto.encrypt(plainText);
        String decrypted = crypto.decrypt(encrypted);
        assert decrypted.equals(plainText);
        assert !encrypted.equals(plainText);
    }
}