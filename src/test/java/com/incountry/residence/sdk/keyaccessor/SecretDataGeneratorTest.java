package com.incountry.residence.sdk.keyaccessor;

import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.crypto.EncryptionKey;
import com.incountry.residence.sdk.tools.crypto.EncryptionSecret;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretDataGeneratorTest {

    @Test
    void fromPasswordTest() throws StorageClientException {
        String secret = "password";
        SecretsData secretData = SecretsDataGenerator.fromPassword(secret);
        assertEquals(0, secretData.getCurrentSecret().getVersion());
        assertEquals(1, secretData.getSecrets().size());
        assertEquals(0, secretData.getSecrets().get(0).getVersion());
        assertArrayEquals(secret.getBytes(StandardCharsets.UTF_8), secretData.getSecrets().get(0).getSecretBytes());
        assertEquals(EncryptionSecret.class, secretData.getSecrets().get(0).getClass());
    }

    @Test
    void fromJsonTest() throws StorageClientException {
        String key = new String(Base64.getEncoder().encode("123456789012345678901234567890AB".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        String password = "password1";
        String secretsDataInJson = "{\n" +
                "    \"currentVersion\": 1,\n" +
                "    \"secrets\": [\n" +
                "        {\"secret\": \"" + key +
                "\", \"version\": 0, \"isForCustomEncryption\": true},\n" +
                "        {\"secret\": \"" + key +
                "\", \"version\": 1, \"isKey\": true},\n" +
                "        {\"secret\": \"" + password +
                "\", \"version\": 2, \"isKey\": false}\n" +
                "    ]\n" +
                "}";
        SecretsData secretData = SecretsDataGenerator.fromJson(secretsDataInJson);
        assertEquals(1, secretData.getCurrentSecret().getVersion());
        assertEquals(3, secretData.getSecrets().size());

        Optional<Secret> secretKey0 = secretData.getSecrets().stream()
                .filter(secretKey -> secretKey.getVersion() == 0)
                .findAny();
        assertTrue(secretKey0.isPresent());
        assertEquals(0, secretKey0.get().getVersion());
        assertArrayEquals(Base64.getDecoder().decode(key), secretKey0.get().getSecretBytes());
        assertEquals(CustomEncryptionKey.class, secretKey0.get().getClass());

        Optional<Secret> secretKey1 = secretData.getSecrets().stream()
                .filter(secretKey -> secretKey.getVersion() == 1)
                .findAny();
        assertTrue(secretKey1.isPresent());
        assertEquals(1, secretKey1.get().getVersion());
        assertArrayEquals(Base64.getDecoder().decode(key), secretKey1.get().getSecretBytes());
        assertEquals(EncryptionKey.class, secretKey1.get().getClass());

        Optional<Secret> secretKey2 = secretData.getSecrets().stream()
                .filter(secretKey -> secretKey.getVersion() == 2)
                .findAny();
        assertTrue(secretKey2.isPresent());
        assertEquals(2, secretKey2.get().getVersion());
        assertArrayEquals(password.getBytes(StandardCharsets.UTF_8), secretKey2.get().getSecretBytes());
        assertEquals(EncryptionSecret.class, secretKey2.get().getClass());

        StorageClientException ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromJson("{illegalJsonString:"));
        assertEquals("Incorrect JSON with SecretsData", ex.getMessage());
    }
}
