package com.incountry.crypto;

import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CryptoTest {

    @Test
    public void testWithNormalEncryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("supersecret", "");

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
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, encrypted);
        }
    }

    @Test
    public void testWithPTEncryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("");

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

            String expectedVersion = "pt";
            String actualVersion = encrypted.split(":")[0];

            assertEquals(expectedVersion, actualVersion);
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, encrypted);
        }
    }

    @Test
    public void testLegacyDecryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("password", "");
        String encrypted = "7765618db31daf5366a6fc3520010327";
        String decrypted = crypto.decrypt(encrypted);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testV1Decryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("password", "");
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        String decrypted = crypto.decrypt(encrypted);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testV2Decryption() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("password", "");
        String encrypted = "2:MyAeMDU3wnlWiqooUM4aStpDvW7JKU0oKBQN4WI0Wyl2vSuSmTIu8TY7Z9ljYeaLfg8ti3mhIJhbLSBNu/AmvMPBZsl6CmSC1KcbZ4kATJQtmZolidyXUGBlXC52xvAnFFGnk2s=";
        String decrypted = crypto.decrypt(encrypted);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testDecNonPTWithoutEnc() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("");
        String data = "MyAeMDU3wnlWiqooUM4aStpDvW7JKU0oKBQN4WI0Wyl2vSuSmTIu8TY7Z9ljYeaLfg8ti3mhIJhbLSBNu/AmvMPBZsl6CmSC1KcbZ4kATJQtmZolidyXUGBlXC52xvAnFFGnk2s=";
        String encrypted = "2:" + data;
        String decrypted = crypto.decrypt(encrypted);
        assertEquals(data, decrypted);
    }

    @Test
    public void testVPTDecryptionWithoutEnc() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("");
        String encrypted = "pt:SW5Db3VudHJ5";
        String decrypted = crypto.decrypt(encrypted);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testVPTDecryptionWithEnc() throws GeneralSecurityException, IOException {
        Crypto crypto = new Crypto("password", "");
        String encrypted = "pt:SW5Db3VudHJ5";
        String decrypted = crypto.decrypt(encrypted);
        assertEquals("InCountry", decrypted);
    }
}