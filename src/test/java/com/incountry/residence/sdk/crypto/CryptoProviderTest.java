package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.StorageConfig;
import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.cipher.Ciphertext;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptoProviderTest {

    private SecretsData secretsData;
    private byte[] secret;
    private Integer keyVersion;
    private static final String ENV_ID = "ENV_ID";

    private final String[] plainTexts = {"",
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
        Secret secret = new EncryptionSecret(keyVersion, this.secret);
        secretsData = new SecretsData(Collections.singletonList(secret), secret);
    }

    @ParameterizedTest
    @MethodSource("getEncryptedString")
    void testDecryption(String encrypted) throws StorageException {
        CryptoProvider provider = new CryptoProvider(null);
        String decrypted = provider.decrypt(encrypted, secretsData, keyVersion);
        assertEquals("InCountry", decrypted);
    }

    @Test
    void testWithNormalEncryption() throws StorageClientException, StorageCryptoException {
        CryptoProvider provider = new CryptoProvider(null);
        for (String plainText : plainTexts) {
            Ciphertext ciphertext = provider.encrypt(plainText, secretsData);
            String decrypted = provider.decrypt(ciphertext.getData(), secretsData, ciphertext.getKeyVersion());
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, ciphertext.getData());
        }
    }

    @Test
    void testWithPTEncryption() throws StorageClientException, StorageCryptoException {
        CryptoProvider provider = new CryptoProvider(null);
        String expectedVersion = "pt";
        for (String plainText : plainTexts) {
            Ciphertext ciphertext = provider.encrypt(plainText, null);
            String decrypted = provider.decrypt(ciphertext.getData(), null, ciphertext.getKeyVersion());
            String actualVersion = ciphertext.getData().split(":")[0];
            assertEquals(expectedVersion, actualVersion);
            assertEquals(plainText, decrypted);
            assertNotEquals(plainText, ciphertext.getData());
        }
    }

    @Test
    void testVPTDecryptionWithoutEnc() throws StorageCryptoException, StorageClientException {
        CryptoProvider provider = new CryptoProvider(null);
        String encrypted = "pt:SW5Db3VudHJ5";
        String decrypted = provider.decrypt(encrypted, null, 1);
        assertEquals("InCountry", decrypted);
    }

    @Test
    void testDecryptionErrorOnSecretMismatch() throws StorageClientException {
        byte[] anotherSecretBytes = "otherpassword".getBytes(StandardCharsets.UTF_8);
        keyVersion = 0;
        Secret anotherSecret = new EncryptionSecret(keyVersion, anotherSecretBytes);
        SecretsData anotherSecretsData = new SecretsData(Collections.singletonList(anotherSecret), anotherSecret);
        CryptoProvider provider = new CryptoProvider(null);
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> provider.decrypt(encrypted, anotherSecretsData, keyVersion));
        assertEquals("Data decryption error", ex.getMessage());
    }

    @Test
    void testSecretKeyWithNegativeVersion() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new EncryptionSecret(-1, secret));
        assertEquals("Version must be >= 0", ex.getMessage());
    }

    @Test
    void testSecretKeyDataWithNegativeVersion() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new SecretsData(new ArrayList<>(), null));
        assertEquals("Secrets in SecretData are null", ex.getMessage());
    }

    @Test
    void testIncorrectKeyAccessor() {
        SecretKeyAccessor accessor1 = () -> null;
        SecretKeyAccessor accessor2 = () -> SecretsDataGenerator.fromPassword("");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId("envId")
                .setClientId("clientId")
                .setClientSecret("clientSecret")
                .setSecretKeyAccessor(accessor1);
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config));
        assertEquals("SecretKeyAccessor returns null secret", ex1.getMessage());
        config.setSecretKeyAccessor(accessor2);
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config));
        assertEquals("Unexpected error", ex2.getMessage());
        assertEquals("Secret can't be null or empty", ex2.getCause().getMessage());
    }

    @Test
    void positiveDecryptNull() throws StorageClientException, StorageCryptoException {
        CryptoProvider provider = new CryptoProvider(null);
        assertNull(provider.decrypt(null, secretsData, 1));
        assertNull(provider.decrypt("", secretsData, 1));
    }

    @Test
    void negativeSecretNotFound() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider(null);
        String encrypted = "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> provider.decrypt(encrypted, secretsData, keyVersion + 1));
        assertEquals("Secret not found for 'version'=1", ex.getMessage());
    }

    @Test
    void encryptDecryptTest() throws StorageClientException, StorageCryptoException {
        SecretsData secretsData = SecretsDataGenerator.fromPassword("123456789_123456789_123456789_12");
        CryptoProvider provider = new CryptoProvider(null);
        String text = "Some secret text";
        Ciphertext ciphertext = provider.encrypt(text, secretsData);
        String decrypted = provider.decrypt(ciphertext.getData(), secretsData, ciphertext.getKeyVersion());
        assertEquals(text, decrypted);
    }

    @Test
    void illegalVersionTest() throws StorageClientException, StorageCryptoException {
        SecretsData secretsData = SecretsDataGenerator.fromPassword("123456789_123456789_123456789_12");
        CryptoProvider provider = new CryptoProvider(null);
        String text = "Some secret text";
        Ciphertext ciphertext = provider.encrypt(text, secretsData);
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> provider.decrypt(ciphertext.getData(), secretsData, 100500));
        assertEquals("Secret not found for 'version'=100500", ex.getMessage());
    }
}
