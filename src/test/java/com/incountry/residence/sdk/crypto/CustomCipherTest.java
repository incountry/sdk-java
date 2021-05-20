package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.Storage;
import com.incountry.residence.sdk.StorageConfig;
import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.crypto.testimpl.CipherStub;
import com.incountry.residence.sdk.crypto.testimpl.FernetCipher;
import com.incountry.residence.sdk.crypto.testimpl.InvalidCipher;
import com.incountry.residence.sdk.crypto.testimpl.PseudoCustomCipher;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.cipher.Ciphertext;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomCipherTest {
    private static final byte[] CUSTOM_PASSWORD_1 = "123456789_123456789_123456789_12".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CUSTOM_PASSWORD_2 = "123456789!123456789_123456789%Ab".getBytes(StandardCharsets.UTF_8);
    private static final String ENV_ID = "envId";
    private static final String FAKE_ENDPOINT = "http://localhost";
    private static final String TOKEN = "token";
    private static final String BODY_FOR_ENCRYPTION = "SomeSecretBody!234567=!@#$%^&**()_+|";

    @Test
    void storageWithCustomCiphersPositive() throws StorageClientException, StorageCryptoException {
        CryptoProvider provider = new CryptoProvider(new CipherStub());
        AbstractCipher cipher = new PseudoCustomCipher();
        provider.registerCipher(cipher);
        Secret key1 = new CustomEncryptionKey(CUSTOM_PASSWORD_1, 1);
        Secret key2 = new CustomEncryptionKey(CUSTOM_PASSWORD_2, 2);
        SecretsData data = new SecretsData(Arrays.asList(key1, key2), key1);
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENV_ID)
                .setOauthToken(TOKEN)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(() -> data)
                .setCryptoProvider(provider);
        Storage storage = StorageImpl.getInstance(config);
        assertNotNull(storage);
        storage = StorageImpl.getInstance(config.copy().setSecretKeyAccessor(null).setCryptoProvider(null));
        assertNotNull(storage);
        storage = StorageImpl.getInstance(config.copy().setSecretKeyAccessor(null).setCryptoProvider(new CryptoProvider(null)));
        assertNotNull(storage);
        assertTrue(provider.unregisterCipher(cipher));
        assertFalse(provider.unregisterCipher(cipher));
        assertFalse(provider.unregisterCipher(null));
    }

    @Test
    void storageWithCustomCiphersNegative() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider(new InvalidCipher());
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, 1);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENV_ID)
                .setOauthToken(TOKEN)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(() -> secretsData)
                .setCryptoProvider(provider);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertTrue(ex.getMessage().startsWith("Validation failed for custom cipher with name"));
    }

    @Test
    void validateCustomCiphersPositive() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider(new CipherStub());
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, 1);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        assertDoesNotThrow(() -> provider.validateCustomCiphers(secretsData));
        Secret key2 = new EncryptionSecret(CUSTOM_PASSWORD_1, 2);
        SecretsData secretsData2 = new SecretsData(Arrays.asList(key, key2), key2);
        assertDoesNotThrow(() -> provider.validateCustomCiphers(secretsData2));
    }

    @Test
    void validateCustomCiphersNegative() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider(new PseudoCustomCipher(0, 0, false));
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, 1);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> provider.validateCustomCiphers(secretsData));
        assertEquals("Validation failed for custom cipher with name 'PseudoCustomCipher'", ex.getMessage());
    }

    @Test
    void customCiphersNullNameNegative() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new CipherStub(null));
        assertEquals("Cipher has null name", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> new CipherStub(""));
        assertEquals("Cipher has null name", ex.getMessage());
    }

    @Test
    void registerCipherNegative() throws StorageClientException {
        AbstractCipher cipher = new CipherStub();
        CryptoProvider provider = new CryptoProvider(cipher);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> provider.registerCipher(null));
        assertEquals("Custom cipher can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> provider.registerCipher(cipher));
        assertEquals("Custom cipher with name com.incountry.residence.sdk.crypto.testimpl.CipherStub is already registered", ex.getMessage());
    }

    @Test
    void customCipherWithoutSecretsNegative() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider(new CipherStub());
        Secret key = new EncryptionSecret(CUSTOM_PASSWORD_1, 1);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> provider.validateCustomCiphers(secretsData));
        assertEquals("There is no custom encryption key for the custom ciphers", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> provider.validateCustomCiphers(null));
        assertEquals("There is no custom encryption key for the custom ciphers", ex.getMessage());
    }

    @Test
    void customCipherSameVersionsNegative() throws StorageClientException {
        String cipherName = "CipherName";
        AbstractCipher cipher = new CipherStub(cipherName);
        CryptoProvider provider = new CryptoProvider(cipher);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> provider.registerCipher(cipher));
        assertEquals("Custom cipher with name CipherName is already registered", ex.getMessage());
    }

    @Test
    void customCipherEncryptDecryptPositive() throws StorageClientException, StorageCryptoException {
        Integer keyVersion = 100500;
        CryptoProvider provider = new CryptoProvider(new FernetCipher("fernet"));
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, keyVersion);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        String text = BODY_FOR_ENCRYPTION;
        Ciphertext ciphertext = provider.encrypt(text, secretsData);
        assertEquals(keyVersion, ciphertext.getKeyVersion());
        String text2 = provider.decrypt(ciphertext.getData(), secretsData, ciphertext.getKeyVersion());
        assertEquals(text, text2);
        assertNull(provider.encrypt(null, secretsData).getData());
        assertNull(provider.decrypt(null, secretsData, keyVersion));
    }

    @Test
    void customCipherDecryptNegative() throws StorageClientException, StorageCryptoException {
        Integer keyVersion = 2;
        CryptoProvider provider = new CryptoProvider(new FernetCipher("fernet"));
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, keyVersion);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);

        String wrongCipherText1 = "qqaazz" + UUID.randomUUID() + ":" + UUID.randomUUID();
        StorageCryptoException ex1 = assertThrows(StorageCryptoException.class, () -> provider.decrypt(wrongCipherText1, secretsData, keyVersion));
        assertEquals("Unknown cipher format", ex1.getMessage());

        CryptoProvider anotherProvider = new CryptoProvider(new CipherStub());
        String encryptedAnotherCipher = anotherProvider.encrypt(BODY_FOR_ENCRYPTION, secretsData).getData();
        StorageCryptoException ex2 = assertThrows(StorageCryptoException.class, () -> provider.decrypt(encryptedAnotherCipher, secretsData, keyVersion));
        assertEquals("Unknown cipher format", ex2.getMessage());
    }

    @Test
    void customCipherWithExceptionNegative() throws StorageClientException, StorageCryptoException {
        int keyVersion = 3;
        CryptoProvider provider = new CryptoProvider(new PseudoCustomCipher(1, 0, true));
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, keyVersion);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        String text = BODY_FOR_ENCRYPTION;
        Ciphertext ciphertext = provider.encrypt(text, secretsData);
        StorageCryptoException ex1 = assertThrows(StorageCryptoException.class, () -> provider.encrypt(text, secretsData));
        assertTrue(ex1.getMessage().isEmpty());
        StorageCryptoException ex2 = assertThrows(StorageCryptoException.class, () -> provider.decrypt(ciphertext.getData(), secretsData, keyVersion));
        assertTrue(ex2.getMessage().isEmpty());
    }

    @Test
    void negativeTestWithCryptoExceptionsInInit() throws StorageClientException {
        int keyVersion = 3;
        CryptoProvider provider = new CryptoProvider(new PseudoCustomCipher(0, 0, true));
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, keyVersion);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> provider.validateCustomCiphers(secretsData));
        assertEquals("", ex.getMessage());
    }

    @Test
    void customCipherWithUnexpectedExceptionNegative() throws StorageClientException, StorageCryptoException {
        int keyVersion = 3;
        CryptoProvider provider = new CryptoProvider(new PseudoCustomCipher(1, 0, false));
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, keyVersion);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        String text = BODY_FOR_ENCRYPTION;
        Ciphertext ciphertext = provider.encrypt(text, secretsData);
        StorageCryptoException ex1 = assertThrows(StorageCryptoException.class, () -> provider.encrypt(text, secretsData));
        assertEquals("Unexpected exception", ex1.getMessage());
        assertEquals(NullPointerException.class, ex1.getCause().getClass());
        StorageCryptoException ex2 = assertThrows(StorageCryptoException.class, () -> provider.decrypt(ciphertext.getData(), secretsData, keyVersion));
        assertEquals("Unexpected exception", ex2.getMessage());
        assertEquals(NullPointerException.class, ex2.getCause().getClass());
    }

    @Test
    void negativeDecryptWrongVersion() throws StorageClientException, StorageCryptoException {
        Integer keyVersion = 3;
        CryptoProvider provider = new CryptoProvider(new FernetCipher("fernet"));
        Secret key = new CustomEncryptionKey(CUSTOM_PASSWORD_1, keyVersion);
        SecretsData secretsData = new SecretsData(Collections.singletonList(key), key);
        Ciphertext ciphertext = provider.encrypt(BODY_FOR_ENCRYPTION, secretsData);
        assertEquals(keyVersion, ciphertext.getKeyVersion());
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () ->
                provider.decrypt(ciphertext.getData(), secretsData, ciphertext.getKeyVersion() + 1));
        assertEquals("Secret not found for 'version'=4", ex.getMessage());
    }

    @Test
    void invalidSecretNegative() throws StorageClientException {
        FernetCipher cipher = new FernetCipher("fernet");
        EncryptionSecret secret = new EncryptionSecret("secret".getBytes(StandardCharsets.UTF_8), 1);
        String text = "text";
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> cipher.encrypt(text, secret));
        assertEquals("Used key from secrets data is not instance of CustomEncryptionKey", ex.getMessage());
        ex = assertThrows(StorageCryptoException.class, () -> cipher.decrypt(text, secret));
        assertEquals("Used key from secrets data is not instance of CustomEncryptionKey", ex.getMessage());
    }
}
