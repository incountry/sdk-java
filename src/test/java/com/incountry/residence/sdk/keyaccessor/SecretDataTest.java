package com.incountry.residence.sdk.keyaccessor;

import com.incountry.residence.sdk.tools.crypto.EncryptionSecret;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretDataTest {

    @Test
    void emptySecretsTest() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new SecretsData(null, null));
        assertEquals("Secrets in SecretData are null", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new SecretsData(new ArrayList<>(), null));
        assertEquals("Secrets in SecretData are null", ex.getMessage());
    }

    @Test
    void wrongCurrentSecretTest() throws StorageClientException {
        Secret secret1 = new EncryptionSecret(1, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        Secret secret2 = new EncryptionSecret(2, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        List<Secret> secrets = new ArrayList<>();
        secrets.add(secret1);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new SecretsData(secrets, null));
        assertEquals("There is no SecretKey version that matches current version ", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new SecretsData(secrets, secret2));
        assertEquals("There is no SecretKey version that matches current version 2", ex.getMessage());
    }

    @Test
    void duplicationSecretsVersionTest() throws StorageClientException {
        Secret secret1 = new EncryptionSecret(1, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        Secret secret2 = new EncryptionSecret(1, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        List<Secret> secrets = new ArrayList<>();
        secrets.add(secret1);
        secrets.add(secret2);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new SecretsData(secrets, secret2));
        assertEquals("SecretKey versions must be unique. Got duplicates for: 1", ex.getMessage());
    }

    @Test
    void toStringTest() throws StorageClientException {
        Secret secret1 = new EncryptionSecret(1, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        Secret secret2 = new EncryptionSecret(2, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        List<Secret> secrets = new ArrayList<>();
        secrets.add(secret1);
        secrets.add(secret2);
        SecretsData secretsData = new SecretsData(secrets, secret1);
        assertTrue(secretsData.toString().contains("version=1"));
        assertTrue(secretsData.toString().contains("version=2"));
    }

    @Test
    void getSecretTest() throws StorageClientException {
        Secret secret1 = new EncryptionSecret(1, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        Secret secret2 = new EncryptionSecret(2, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        List<Secret> secrets = new ArrayList<>();
        secrets.add(secret1);
        secrets.add(secret2);
        SecretsData secretsData = new SecretsData(secrets, secret1);
        Secret secretOne = secretsData.getSecret(1);
        Secret secretTwo = secretsData.getSecret(2);
        Secret defaultSecret = secretsData.getSecret(null);
        assertEquals(secret1, secretOne);
        assertEquals(secret2, secretTwo);
        assertEquals(secret1, defaultSecret);
    }
}
