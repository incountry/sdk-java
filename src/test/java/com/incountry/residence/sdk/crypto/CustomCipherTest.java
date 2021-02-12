package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.crypto.cipher.CipherStub;
import com.incountry.residence.sdk.crypto.cipher.CipherWithException;
import com.incountry.residence.sdk.crypto.cipher.WrongCipher;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.crypto.EncryptionSecret;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomCipherTest {

    @Test
    void registerCipherTest() throws StorageException {
        CipherStub cipher1 = new CipherStub("cipherStub1");
        CipherStub cipher2 = new CipherStub("cipherStub2");
        CryptoProvider provider = new CryptoProvider(cipher1);
        provider.registerCipher(cipher1)
                .registerCipher(cipher2);

        assertTrue(provider.unregisterCipher(cipher1));
        assertTrue(provider.unregisterCipher(cipher2));
        assertFalse(provider.unregisterCipher(cipher1));
        assertFalse(provider.unregisterCipher(cipher2));
        assertFalse(provider.unregisterCipher(null));
        assertFalse(provider.unregisterCipher(new CipherStub("")));
    }

    @Test
    void registerCipherNegativeTest() throws StorageClientException {
        CipherStub cipher = new CipherStub("");
        CryptoProvider provider = new CryptoProvider();

        StorageClientException ex = assertThrows(StorageClientException.class, () -> provider.registerCipher(cipher));
        assertEquals("Custom cipher has null name", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> provider.registerCipher(null));
        assertEquals("Custom cipher is null", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new CryptoProvider(cipher));
        assertEquals("Custom cipher has null name", ex.getMessage());
    }

    @Test
    void validateCipherTest() throws StorageException {
        CipherStub cipher1 = new CipherStub("cipherStub1");
        CipherStub cipher2 = new CipherStub("cipherStub2");
        CryptoProvider provider = new CryptoProvider(cipher1);
        provider.registerCipher(cipher1)
                .registerCipher(cipher2);

        CustomEncryptionKey customEncryptionKey =
                new CustomEncryptionKey(1, "Custom Encryption Key 32 symbols".getBytes(StandardCharsets.UTF_8));
        List<Secret> secrets = new ArrayList<>();
        secrets.add(customEncryptionKey);
        provider.validateCustomCiphers(new SecretsData(secrets, customEncryptionKey));
        assertNotNull(provider);

        EncryptionSecret encryptionSecret = new EncryptionSecret(2, "EncryptionSecret".getBytes(StandardCharsets.UTF_8));
        secrets.add(encryptionSecret);
        provider.validateCustomCiphers(new SecretsData(secrets, encryptionSecret));
        assertNotNull(provider);
    }

    @Test
    void validateCipherNegativeTest() throws StorageClientException {
        CipherStub cipher1 = new CipherStub("cipherStub1");
        WrongCipher cipher2 = new WrongCipher("WrongCipher");
        CryptoProvider provider = new CryptoProvider(cipher1);
        provider.registerCipher(cipher1)
                .registerCipher(cipher2);

        CustomEncryptionKey customEncryptionKey =
                new CustomEncryptionKey(1, "CustomEncryptionKey".getBytes(StandardCharsets.UTF_8));
        List<Secret> secrets = new ArrayList<>();
        secrets.add(customEncryptionKey);

        SecretsData secretsData = new SecretsData(secrets, customEncryptionKey);
        StorageClientException ex = assertThrows(
                StorageClientException.class, () -> provider.validateCustomCiphers(secretsData));
        assertEquals("Validation failed for custom cipher config with version WrongCipher", ex.getMessage());

        provider.unregisterCipher(cipher2);
        CipherWithException cipher3 = new CipherWithException("code");
        provider.registerCipher(cipher3);

        SecretsData secretsData1 = new SecretsData(secrets, customEncryptionKey);
        ex = assertThrows(StorageClientException.class, () -> provider.validateCustomCiphers(secretsData1));
        assertEquals("Validation failed for custom cipher config with version code", ex.getMessage());
        assertEquals(ex.getCause().getClass(), RuntimeException.class);
        assertTrue(ex.getCause().getMessage().isEmpty());
    }

    @Test
    void encryptWithCustomCipherNegativeTest() throws StorageClientException {
        CipherWithException cipher = new CipherWithException("some cipher");
        String exceptionMessage = "some exception message";
        CipherWithException anotherCipher = new CipherWithException("another cipher", exceptionMessage);
        CryptoProvider provider = new CryptoProvider(cipher);
        provider.registerCipher(cipher)
                .registerCipher(anotherCipher);

        CustomEncryptionKey key = new CustomEncryptionKey(1, "Custom Encryption Key 32 symbols".getBytes(StandardCharsets.UTF_8));
        List<Secret> secrets = new ArrayList<>();
        secrets.add(key);
        SecretsData secretData = new SecretsData(secrets, key);

        StorageCryptoException ex =
                assertThrows(StorageCryptoException.class, () -> provider.decrypt("c" + UUID.randomUUID().toString() + ":123", secretData, 1));
        assertEquals("Unknown cipher format", ex.getMessage());

        ex = assertThrows(StorageCryptoException.class, () -> provider.decrypt("z" + UUID.randomUUID().toString() + ":123", secretData, 1));
        assertEquals("Unknown cipher format", ex.getMessage());

        ex = assertThrows(StorageCryptoException.class, () -> provider.encrypt(UUID.randomUUID().toString(), secretData));
        assertEquals("Unexpected exception during encryption", ex.getMessage());

        ex = assertThrows(StorageCryptoException.class, () -> provider.decrypt("cc29tZSBjaXBoZXI=:c29tZSBjaXBoZXI=", secretData, 1));
        assertEquals("Unexpected exception", ex.getMessage());

        ex = assertThrows(StorageCryptoException.class, () -> provider.decrypt("cYW5vdGhlciBjaXBoZXI=:YW5vdGhlciBjaXBoZXI=", secretData, 1));
        assertEquals(exceptionMessage, ex.getCause().getMessage());

        CryptoProvider anotherProvider = new CryptoProvider(anotherCipher);
        provider.registerCipher(anotherCipher);

        ex = assertThrows(StorageCryptoException.class, () -> anotherProvider.encrypt(UUID.randomUUID().toString(), secretData));
        assertEquals(exceptionMessage, ex.getCause().getMessage());
    }
}
