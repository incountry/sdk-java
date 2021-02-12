package com.incountry.residence.sdk.keyaccessor;

import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.crypto.EncryptionKey;
import com.incountry.residence.sdk.tools.crypto.EncryptionSecret;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretTest {

    @Test
    void wrongSecretVersionTest() {
        StorageClientException ex = assertThrows(
                StorageClientException.class, () -> new EncryptionSecret(
                        -1, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));
        assertEquals("Version must be >= 0", ex.getMessage());
    }

    @Test
    void wrongSecretTest() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new EncryptionSecret(1, null));
        assertEquals("Secret can't be null", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new EncryptionSecret(1, new byte[] {}));
        assertEquals("Secret can't be null", ex.getMessage());
    }

    @Test
    void wrongSecretIsKeyTest() {
        StorageClientException ex = assertThrows(
                StorageClientException.class, () -> new EncryptionKey(
                        1, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));
        assertEquals("Wrong key length for secret key with 'isKey==true'. Should be 32-byte array", ex.getMessage());
    }

    @Test
    void secretsToStringTest() throws StorageClientException {
        byte[] secretBytes = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        Secret encryptionSecret = new EncryptionSecret(1, secretBytes);
        assertTrue(encryptionSecret.toString().contains("EncryptionSecret"));
        Secret customEncryptionKey = new CustomEncryptionKey(1, secretBytes);
        assertTrue(customEncryptionKey.toString().contains("CustomEncryptionKey"));
        Secret encryptionKey = new EncryptionKey(1, "123456789012345678901234567890Ab".getBytes(StandardCharsets.UTF_8));
        assertTrue(encryptionKey.toString().contains("EncryptionKey"));
    }
}
