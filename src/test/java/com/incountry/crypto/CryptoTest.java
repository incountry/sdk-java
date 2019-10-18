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

}