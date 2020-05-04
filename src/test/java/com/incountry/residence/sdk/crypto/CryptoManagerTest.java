package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CryptoManagerTest {

    private SecretsData secretsData;
    private String secret;
    private Integer keyVersion;

    @BeforeEach
    public void init() throws StorageClientException {
        secret = "password";
        keyVersion = 0;
        SecretKey secretKey = new SecretKey(secret, keyVersion, false);
        secretsData = new SecretsData(Arrays.asList(secretKey), keyVersion);

    }

    @Test
    public void testWithNormalEncryption() throws StorageClientException, StorageCryptoException {
        CryptoManager cryptoManager = new CryptoManager(() -> secretsData, null);

        String[] plainTexts = {"",
                "Howdy", // <-- English
                "Привет медвед", // <-- Russian
                "مرحبا", // <-- Arabic
                "हाय", // <-- Hindi
                "안녕", // <-- Korean
                "こんにちは", // Japanese
                "你好", // <- Chinese
        };

        for (String plainText : plainTexts) {
            Map.Entry<String, Integer> encrypted = cryptoManager.encrypt(plainText);
            String decrypted = cryptoManager.decrypt(encrypted.getKey(), encrypted.getValue());
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, encrypted);
        }
    }

    @Test
    public void testWithPTEncryption() throws StorageClientException, StorageCryptoException {
        CryptoManager crypto = new CryptoManager("");

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

        for (String plainText : plainTexts) {
            Map.Entry<String, Integer> encrypted = crypto.encrypt(plainText);
            String decrypted = crypto.decrypt(encrypted.getKey(), encrypted.getValue());
            String actualVersion = encrypted.getKey().split(":")[0];
            assertEquals(expectedVersion, actualVersion);
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, encrypted);
        }
    }

    @Test
    public void testV1Decryption() throws StorageCryptoException, StorageClientException {
        CryptoManager crypto = new CryptoManager(() -> secretsData, null);
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        String decrypted = crypto.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testV2Decryption() throws StorageCryptoException, StorageClientException {
        CryptoManager crypto = new CryptoManager(() -> secretsData, null);
        String encrypted = "2:MyAeMDU3wnlWiqooUM4aStpDvW7JKU0oKBQN4WI0Wyl2vSuSmTIu8TY7Z9ljYeaLfg8ti3mhIJhbLSBNu/AmvMPBZsl6CmSC1KcbZ4kATJQtmZolidyXUGBlXC52xvAnFFGnk2s=";
        String decrypted = crypto.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testVPTDecryptionWithoutEnc() throws StorageCryptoException, StorageClientException {
        CryptoManager cryptoManager = new CryptoManager("");
        String encrypted = "pt:SW5Db3VudHJ5";
        String decrypted = cryptoManager.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    public void testVPTDecryptionWithEnc() throws StorageCryptoException, StorageClientException {
        CryptoManager crypto = new CryptoManager(() -> secretsData, "");
        String encrypted = "pt:SW5Db3VudHJ5";
        String decrypted = crypto.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);

    }

    @Test
    public void testDecryptionErrorOnSecretMismatch() throws StorageClientException {
        secret = "otherpassword";
        keyVersion = 0;
        SecretKey secretKey = new SecretKey(secret, keyVersion, false);
        secretsData = new SecretsData(Arrays.asList(secretKey), keyVersion);
        CryptoManager crypto = new CryptoManager(() -> secretsData, null);
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        assertThrows(StorageCryptoException.class, () -> crypto.decrypt(encrypted, keyVersion));
    }

    @Test
    public void testSecretKeyWithNegativeVersion() {
        assertThrows(StorageClientException.class, () -> new SecretKey(secret, -1, false));
    }

    @Test
    public void testSecretKeyDataWithNegativeVersion() {
        assertThrows(StorageClientException.class, () -> new SecretsData(new ArrayList<>(), -2));
    }

    @Test
    public void testIncorrectKeyAccessor() {
        SecretKeyAccessor accessor1 = () -> null;
        SecretKeyAccessor accessor2 = () -> SecretsDataGenerator.fromPassword("");
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance("envId", "apiKey", "Http://fakeEndpoint", accessor1));
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance("envId", "apiKey", "Http://fakeEndpoint", accessor2));
    }

    @Test
    public void positiveGetNullSecretVersion() throws StorageClientException, StorageCryptoException {
        CryptoManager manager = new CryptoManager(null, "ENV_ID", null);
        assertNull(manager.getCurrentSecretVersion());
    }
}