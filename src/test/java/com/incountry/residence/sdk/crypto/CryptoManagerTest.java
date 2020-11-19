package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptoManagerTest {

    private SecretsData secretsData;
    private byte[] secret;
    private Integer keyVersion;
    private static final String ENV_ID = "ENV_ID";

    private String[] plainTexts = {"",
            "Hello", // <-- English
            "Добрый день", // <-- Russian
            "مرحبا", // <-- Arabic
            "हाय", // <-- Hindi
            "안녕", // <-- Korean
            "こんにちは", // Japanese
            "你好", // <- Chinese
    };

    private static Stream<Arguments> getEncryptedString() {
        return Stream.of(
                Arguments.of("1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29"),
                Arguments.of("2:MyAeMDU3wnlWiqooUM4aStpDvW7JKU0oKBQN4WI0Wyl2vSuSmTIu8TY7Z9ljYeaLfg8ti3mhIJhbLSBNu/AmvMPBZsl6CmSC1KcbZ4kATJQtmZolidyXUGBlXC52xvAnFFGnk2s="),
                Arguments.of("pt:SW5Db3VudHJ5")
        );
    }

    @BeforeEach
    public void init() throws StorageClientException {
        secret = "password".getBytes(StandardCharsets.UTF_8);
        keyVersion = 0;
        SecretKey secretKey = new SecretKey(secret, keyVersion, false);
        secretsData = new SecretsData(Collections.singletonList(secretKey), keyVersion);
    }

    @ParameterizedTest
    @MethodSource("getEncryptedString")
    void testDecryption(String encrypted) throws StorageException {
        CryptoManager crypto = new CryptoManager(() -> secretsData, null, null, false, true);
        String decrypted = crypto.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    void testWithNormalEncryption() throws StorageClientException, StorageCryptoException {
        CryptoManager cryptoManager = new CryptoManager(() -> secretsData, null, null, false, true);
        for (String plainText : plainTexts) {
            Map.Entry<String, Integer> encrypted = cryptoManager.encrypt(plainText);
            String decrypted = cryptoManager.decrypt(encrypted.getKey(), encrypted.getValue());
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, encrypted.getKey());
        }
    }

    @Test
    void testWithPTEncryption() throws StorageClientException, StorageCryptoException {
        CryptoManager crypto = new CryptoManager(null, "", null, false, true);
        String expectedVersion = "pt";
        for (String plainText : plainTexts) {
            Map.Entry<String, Integer> encrypted = crypto.encrypt(plainText);
            String decrypted = crypto.decrypt(encrypted.getKey(), encrypted.getValue());
            String actualVersion = encrypted.getKey().split(":")[0];
            assertEquals(expectedVersion, actualVersion);
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, encrypted.getKey());
        }
    }

    @Test
    void testVPTDecryptionWithoutEnc() throws StorageCryptoException, StorageClientException {
        CryptoManager cryptoManager = new CryptoManager(null, "", null, false, true);
        String encrypted = "pt:SW5Db3VudHJ5";
        String decrypted = cryptoManager.decrypt(encrypted, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    void testDecryptionErrorOnSecretMismatch() throws StorageClientException {
        secret = "otherpassword".getBytes(StandardCharsets.UTF_8);
        keyVersion = 0;
        SecretKey secretKey = new SecretKey(secret, keyVersion, false);
        secretsData = new SecretsData(Collections.singletonList(secretKey), keyVersion);
        CryptoManager crypto = new CryptoManager(() -> secretsData, null, null, false, true);
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> crypto.decrypt(encrypted, keyVersion));
        assertEquals("Data encryption error", ex.getMessage());
    }

    @Test
    void testSecretKeyWithNegativeVersion() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new SecretKey(secret, -1, false));
        assertEquals("Version must be >= 0", ex.getMessage());
    }

    @Test
    void testSecretKeyDataWithNegativeVersion() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new SecretsData(new ArrayList<>(), -2));
        assertEquals("Secrets in SecretData are null", ex.getMessage());
    }

    @Test
    void testIncorrectKeyAccessor() {
        SecretKeyAccessor accessor1 = () -> null;
        SecretKeyAccessor accessor2 = () -> SecretsDataGenerator.fromPassword("");
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance("envId", "apiKey", "Http://fakeEndpoint", accessor1));
        assertEquals("SecretKeyAccessor returns null secret", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance("envId", "apiKey", "Http://fakeEndpoint", accessor2));
        assertEquals("Secret can't be null", ex2.getMessage());
    }

    @Test
    void positiveGetNullSecretVersion() throws StorageClientException {
        CryptoManager manager = new CryptoManager(null, ENV_ID, null, false, true);
        assertNull(manager.getCurrentSecretVersion());
    }

    @Test
    void positiveDecryptNull() throws StorageClientException, StorageCryptoException {
        CryptoManager manager = new CryptoManager(null, ENV_ID, null, false, true);
        assertNull(manager.decrypt(null, 1));
        assertNull(manager.decrypt("", 1));
    }

    @Test
    void negativeNoSecretProvided() throws StorageClientException {
        CryptoManager manager = new CryptoManager(null, ENV_ID, null, false, true);
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> manager.decrypt(encrypted, keyVersion));
        assertEquals(String.format("No secret provided. Cannot decrypt record: %s", encrypted), ex.getMessage());
    }

    @Test
    void negativeTestWrongKeyType() throws StorageClientException {
        int keyVersion = 1;
        SecretKey secretKey = new SecretKey("123456789_123456789_123456789_12".getBytes(StandardCharsets.UTF_8), keyVersion, false, true);
        SecretsData secretsData = new SecretsData(Collections.singletonList(secretKey), keyVersion);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new CryptoManager(secretKeyAccessor, "ENV_ID", null, false, true));
        assertEquals("Secret not found for 'version'=1 with 'isForCustomEncryption'=false", ex.getMessage());
    }

    @Test
    void positiveTestConstructor2WithoutEncryption() throws StorageClientException {
        CryptoManager manager = new CryptoManager(null, ENV_ID, null, false, true);
        assertNotNull(manager);
    }

    @Test
    void negativeTestBadSecretAccessor() {
        SecretKeyAccessor accessor = () -> {
            throw new NullPointerException();
        };
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new CryptoManager(accessor, "ENV_ID", null, false, true));
        assertEquals("Unexpected exception", ex.getMessage());
        assertEquals(NullPointerException.class, ex.getCause().getClass());
    }

    @Test
    void testLowerCasingForKeys() throws StorageClientException {
        String someKey = "FilterValue123~!@#$%^&*()_+";
        CryptoManager crypto = new CryptoManager(null, ENV_ID, null, true, true);
        assertEquals(crypto.createKeyHash(someKey), crypto.createKeyHash(someKey.toLowerCase()));
        assertEquals(crypto.createKeyHash(someKey), crypto.createKeyHash(someKey.toUpperCase()));
    }

    @Test
    void testCreateSearchKeyHash() throws StorageClientException {
        String someKey = "FilterValue123~!@#$%^&*()_+";
        CryptoManager crypto = new CryptoManager(null, ENV_ID, null, true, false);
        assertEquals(someKey, crypto.createSearchKeyHash(someKey));
        crypto = new CryptoManager(null, ENV_ID, null, true, true);
        assertEquals(crypto.createKeyHash(someKey), crypto.createSearchKeyHash(someKey));
    }

    @Test
    void nullVersionTest() throws StorageClientException, StorageCryptoException {
        SecretsData secretsData = SecretsDataGenerator.fromPassword("123456789_123456789_123456789_12");
        CryptoManager manager = new CryptoManager(() -> secretsData, ENV_ID, null, false, true);
        String text = "Some secret text";
        Map.Entry<String, Integer> encrypted = manager.encrypt(text);
        String decrypted = manager.decrypt(encrypted.getKey(), null);
        assertEquals(text, decrypted);
    }

    @Test
    void illegalVersionTest() throws StorageClientException, StorageCryptoException {
        SecretsData secretsData = SecretsDataGenerator.fromPassword("123456789_123456789_123456789_12");
        CryptoManager manager = new CryptoManager(() -> secretsData, ENV_ID, null, false, true);
        String text = "Some secret text";
        Map.Entry<String, Integer> encrypted = manager.encrypt(text);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> manager.decrypt(encrypted.getKey(), 100500));
        assertEquals("Secret not found for 'version'=100500 with 'isForCustomEncryption'=false", ex.getMessage());
    }
}
