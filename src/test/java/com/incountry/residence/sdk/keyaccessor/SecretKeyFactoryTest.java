package com.incountry.residence.sdk.keyaccessor;

import com.incountry.residence.sdk.tools.crypto.EncryptionSecret;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.crypto.SecretsFactory;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretKeyFactoryTest {

    @Test
    void getKeyTest() throws StorageException {
        SecretsData secretData = SecretsDataGenerator.fromPassword("secret");
        byte[] salt = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        byte[] key = SecretsFactory.getKey(salt, secretData.getCurrentSecret(), 1000);
        assertNotNull(key);
        assertTrue(key.length > 0);
    }

    @Test
    void getKeyNegativeTest() throws StorageClientException {
        Secret secret = new EncryptionSecret(1, "password".getBytes(StandardCharsets.UTF_8));
        byte[] salt = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);

        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> SecretsFactory.getKey(salt, null, 1000));
        assertEquals("Secret is null", ex.getMessage());

        ex = assertThrows(StorageCryptoException.class, () -> SecretsFactory.getKey(null, secret, -1));
        assertEquals("Secret generation exception", ex.getMessage());
    }

}
