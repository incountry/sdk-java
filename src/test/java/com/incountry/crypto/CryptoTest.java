package com.incountry.crypto;

import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CryptoTest {

    @Test
    public void testEncryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("supersecret");

        String[] plainTexts = {"",
                "Howdy", // <-- English
                "Привет медвед", // <-- Russian
                "مرحبا", // <-- Arabic
                "हाय", // <-- Hindi
                "안녕", // <-- Korean
                "こんにちは", // Japanese
                "你好", // <- Chinese
        };

        for (String plainText: plainTexts) {
            String encrypted = crypto.encrypt(plainText);
            String decrypted = crypto.decrypt(encrypted);
            assertEquals(decrypted, plainText);
            assertNotEquals(encrypted, plainText);
        }
    }

    @Test
    public void testLegacyDecryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("password");
        String encrypted = "7765618db31daf5366a6fc3520010327";
        String decrypted = crypto.decrypt(encrypted);
        assertEquals(decrypted, "InCountry");
    }

    @Test
    public void testV1Decryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("password");
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        String decrypted = crypto.decrypt(encrypted);
        assertEquals(decrypted, "InCountry");
    }

}