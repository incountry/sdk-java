package com.incountry;

import org.junit.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

public class CryptoTest {
    @Test
    public void testEncryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("supersecret");
        String plainText = "some plain text";
        String encrypted = crypto.encrypt(plainText);
        String decrypted = crypto.decrypt(encrypted);
        assertEquals(decrypted, plainText);
        assertNotEquals(encrypted, plainText);
    }
}