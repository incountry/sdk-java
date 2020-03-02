package com.incountry.crypto;

import com.incountry.crypto.impl.CryptoImpl;
import com.incountry.exceptions.StorageCryptoException;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CryptoTest {

    private SecretKeysData secretKeysData;
    private String secret;
    private Integer keyVersion;

    @Before
    public void init() {
        secret = "password";
        keyVersion = 0;

        secretKeysData = new SecretKeysData();
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(keyVersion);
        secretKeysData.setSecrets(new ArrayList<SecretKey>() {{
            add(secretKey);
        }});
        secretKeysData.setCurrentVersion(keyVersion);
    }

    @Test
    public void testWithNormalEncryption() throws StorageCryptoException {
        CryptoImpl crypto = new CryptoImpl(secretKeysData);

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
            Pair<String, Integer> encrypted = crypto.encrypt(plainText);
            String decrypted = crypto.decrypt(encrypted.getValue0(), encrypted.getValue1());
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, encrypted);
        }
    }

    @Test
    public void testWithPTEncryption() throws StorageCryptoException {
        CryptoImpl crypto = new CryptoImpl("");

        String[] plainTexts = {"",
                "Howdy", // <-- English
                "Привет медвед", // <-- Russian
                "مرحبا", // <-- Arabic
                "हाय", // <-- Hindi
                "안녕", // <-- Korean
                "こんにちは", // Japanese
                "你好", // <- Chinese
        };
        String expectedVersion = "pt";

        for (String plainText: plainTexts) {
            Pair<String, Integer> encrypted = crypto.encrypt(plainText);
            String decrypted = crypto.decrypt(encrypted.getValue0(), encrypted.getValue1());

            String actualVersion = encrypted.getValue0().split(":")[0];

            assertEquals(expectedVersion, actualVersion);
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, encrypted);
        }
    }

    @Test
    public void testV1Decryption() throws StorageCryptoException {
        CryptoImpl crypto = new CryptoImpl(secretKeysData);
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        String decrypted = crypto.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testV2Decryption() throws StorageCryptoException {
        CryptoImpl crypto = new CryptoImpl(secretKeysData);
        String encrypted = "2:MyAeMDU3wnlWiqooUM4aStpDvW7JKU0oKBQN4WI0Wyl2vSuSmTIu8TY7Z9ljYeaLfg8ti3mhIJhbLSBNu/AmvMPBZsl6CmSC1KcbZ4kATJQtmZolidyXUGBlXC52xvAnFFGnk2s=";
        String decrypted = crypto.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testVPTDecryptionWithoutEnc() throws StorageCryptoException {
        Crypto crypto = new CryptoImpl("");
        String encrypted = "pt:SW5Db3VudHJ5";
        String decrypted = crypto.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testVPTDecryptionWithEnc() throws StorageCryptoException {
        Crypto crypto = new CryptoImpl(secretKeysData, "");
        String encrypted = "pt:SW5Db3VudHJ5";
        String decrypted = crypto.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

}