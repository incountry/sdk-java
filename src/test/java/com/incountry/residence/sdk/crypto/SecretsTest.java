package com.incountry.residence.sdk.crypto;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretsTest {
    private static final byte[] PASSWORD_32 = "password__password__password__32".getBytes(StandardCharsets.UTF_8);

    @Test
    void testConvertStringToSecretsDataWhenSecretKeyStringIsJson() throws Exception {
        String key = Base64.getEncoder().encodeToString("123456789012345678901234567890AB".getBytes(StandardCharsets.UTF_8));
        String secretKeyStringIsKey = "{\n" +
                "  \"secrets\": [\n" +
                "    {\n" +
                "      \"secret\": \"" + key + "\",\n" +
                "      \"version\": 1,\n" +
                "      \"isKey\": %s,\n" +
                "      \"isForCustomEncryption\": %s\n" +
                "    }\n" +
                "  ],\n" +
                "  \"currentVersion\": 1\n" +
                "}";
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromJson(String.format(secretKeyStringIsKey, true, false));
        SecretsData resultSecretsData = accessor.getSecretsData();
        assertEquals(1, resultSecretsData.getCurrentSecret().getVersion());
        assertArrayEquals(DatatypeConverter.parseBase64Binary(key), resultSecretsData.getSecrets().get(0).getSecretBytes());
        assertEquals(1, resultSecretsData.getSecrets().get(0).getVersion());
        assertTrue(resultSecretsData.getSecrets().get(0) instanceof EncryptionKey);

        accessor = () -> SecretsDataGenerator.fromJson(String.format(secretKeyStringIsKey, false, true));
        resultSecretsData = accessor.getSecretsData();
        assertEquals(1, resultSecretsData.getCurrentSecret().getVersion());
        assertArrayEquals(DatatypeConverter.parseBase64Binary(key), resultSecretsData.getSecrets().get(0).getSecretBytes());
        assertEquals(1, resultSecretsData.getSecrets().get(0).getVersion());
        assertTrue(resultSecretsData.getSecrets().get(0) instanceof CustomEncryptionKey);
    }

    @Test
    void testConvertStringToSecretsDataWhenSecretKeyStringIsNotJson() throws Exception {
        String secretString = "user_password";
        int version = 0;
        int currentVersion = 0;
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("user_password");
        SecretsData resultSecretsData = accessor.getSecretsData();
        assertEquals(currentVersion, resultSecretsData.getCurrentSecret().getVersion());
        assertArrayEquals(secretString.getBytes(StandardCharsets.UTF_8), resultSecretsData.getSecrets().get(0).getSecretBytes());
        assertEquals(version, resultSecretsData.getSecrets().get(0).getVersion());
        assertTrue(resultSecretsData.getSecrets().get(0) instanceof EncryptionSecret);
    }

    @Test
    void secretsFromJsonTest() throws StorageClientException {
        JsonObject jsonWithoutSecretsDataFields = new JsonObject();
        jsonWithoutSecretsDataFields.addProperty("body", "<body>");
        jsonWithoutSecretsDataFields.addProperty("record_key", "<recordKey>");
        jsonWithoutSecretsDataFields.addProperty("key2", "<key2>");
        jsonWithoutSecretsDataFields.addProperty("profile_key", "<profileKey>");
        jsonWithoutSecretsDataFields.addProperty("range_key1", 1);
        jsonWithoutSecretsDataFields.addProperty("version", 2);
        final String jsonString = new Gson().toJson(jsonWithoutSecretsDataFields);

        StorageClientException ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromJson(jsonString));
        assertEquals("Secrets in SecretData are null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromJson("NotJsonString"));
        assertEquals("Incorrect JSON with SecretsData", ex.getMessage());

        JsonObject jsonWithSecret = new JsonObject();
        jsonWithSecret.addProperty("secret", "someSecret");
        jsonWithSecret.addProperty("isKey", "false");
        jsonWithSecret.addProperty("version", "1");
        JsonArray array = new JsonArray();
        array.add(jsonWithSecret);
        JsonObject jsonWithSecretsDataFields = new JsonObject();
        jsonWithSecretsDataFields.addProperty("currentVersion", "1");
        jsonWithSecretsDataFields.add("secrets", array);
        String jsonString2 = new Gson().toJson(jsonWithSecretsDataFields);
        SecretsData data = SecretsDataGenerator.fromJson(jsonString2);

        assertNotNull(data);
        assertEquals(1, data.getCurrentSecret().getVersion());
        assertEquals(1, data.getSecrets().size());
        assertArrayEquals("someSecret".getBytes(StandardCharsets.UTF_8), data.getSecrets().get(0).getSecretBytes());
        assertEquals(1, data.getSecrets().get(0).getVersion());
        assertTrue(data.getSecrets().get(0) instanceof EncryptionSecret);

        String secretsDataInJson = "{\n" +
                "    \"currentVersion\": 1,\n" +
                "    \"secrets\": [\n" +
                "        {\"secret\": \"password0\", \"version\": 0},\n" +
                "        {\"secret\": \"password1\", \"version\": 1}\n" +
                "    ]\n" +
                "}";
        SecretsData data1 = SecretsDataGenerator.fromJson(secretsDataInJson);
        assertEquals(1, data1.getCurrentSecret().getVersion());
        assertTrue(data1.getCurrentSecret() instanceof EncryptionSecret);
        assertEquals(2, data1.getSecrets().size());
    }

    @Test
    void testWrongJson() {
        String secretDataWrongJson = "{\n" +
                "  \"secrets\": [\n" +
                "    {\n" +
                //special typo
                "      \"secrettt\": \"mySuperSecret1\",\n" +
                "      \"version_secret\": 1\n" +
                "    }\n" +
                "  ],\n" +
                "  \"currentVersion\": 1\n" +
                "}";

        StorageClientException ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromJson(secretDataWrongJson));
        assertEquals("Incorrect JSON with SecretsData", ex.getMessage());
    }

    @Test
    void generateSecretFromNullNegative() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromJson(null));
        assertEquals("Incorrect JSON with SecretsData", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromJson(""));
        assertEquals("Incorrect JSON with SecretsData", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromPassword(""));
        assertEquals("Secret can't be null or empty", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromPassword(null));
        assertEquals("Secret can't be null or empty", ex.getMessage());
    }

    @Test
    void testValidationOfSecretsData() throws StorageClientException {
        Secret secretKey1 = new EncryptionSecret("password1".getBytes(StandardCharsets.UTF_8), 0);
        Secret secretKey2 = new EncryptionSecret("password2".getBytes(StandardCharsets.UTF_8), 1);
        Secret secretKey3 = new EncryptionSecret("password3".getBytes(StandardCharsets.UTF_8), 0);
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> new SecretsData(Arrays.asList(secretKey1, secretKey2, secretKey3), secretKey1));
        assertEquals("Secret versions must be unique. Got duplicates for: [0]", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> new SecretsData(Arrays.asList(secretKey1, secretKey2), secretKey3));
        assertEquals("There is no current secret at the secrets list", ex2.getMessage());
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> new SecretsData(Arrays.asList(secretKey1, secretKey2), null));
        assertEquals("There is no current secret at the secrets list", ex3.getMessage());
    }

    @Test
    void secretsDataToStringTest() throws StorageClientException {
        String expected = "SecretsData{secrets=[EncryptionSecret{version=0, secretBytes=HASH[761978414]}], currentSecret=EncryptionSecret{version=0, secretBytes=HASH[761978414]}}";
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("user_password");
        SecretsData secretsData = accessor.getSecretsData();
        assertEquals(expected, secretsData.toString());
    }

    @Test
    void secretsDataNegativeVersionTest() {
        StorageClientException ex = assertThrows(StorageClientException.class, () ->
                new EncryptionSecret("password".getBytes(StandardCharsets.UTF_8), -1));
        assertEquals("Version must be >= 0", ex.getMessage());
    }

    @Test
    void secretsDataEmptySecretsTest() {
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> new SecretsData(null, null));
        assertEquals("Secrets in SecretData are null", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> new SecretsData(new ArrayList<>(), null));
        assertEquals("Secrets in SecretData are null", ex2.getMessage());
    }

    @Test
    void secretsKeyWrongLength() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new EncryptionKey("123".getBytes(StandardCharsets.UTF_8), 1));
        assertEquals("Wrong key length for encryption key . Should be 32-byte array", ex.getMessage());
    }

    @Test
    void testWithSecretNotBase64() {
        String secretKeyString = "{\n" +
                "  \"secrets\": [\n" +
                "    {\n" +
                "      \"secret\": \"passwordpasswordpasswordpasswor*\",\n" +
                "      \"version\": 1,\n" +
                "      \"isKey\": true,\n" +
                "      \"isForCustomEncryption\": false\n" +
                "    }\n" +
                "  ],\n" +
                "  \"currentVersion\": 1\n" +
                "}";
        StorageClientException ex = assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromJson(secretKeyString));
        assertEquals("Secret key must be base64-encoded string", ex.getMessage());
    }

    @Test
    void toStringPositive() throws StorageClientException {
        Secret secret1 = new EncryptionSecret("123456789012345678901234567890AB".getBytes(StandardCharsets.UTF_8), 1);
        assertEquals("EncryptionSecret{version=1, secretBytes=HASH[-847010189]}", secret1.toString());
        Secret secret2 = new EncryptionKey("123456789012345678901234567890AB".getBytes(StandardCharsets.UTF_8), 1);
        assertEquals("EncryptionKey{version=1, secretBytes=HASH[-847010189]}", secret2.toString());
        Secret secret3 = new CustomEncryptionKey("123456789012345678901234567890AB".getBytes(StandardCharsets.UTF_8), 1);
        assertEquals("CustomEncryptionKey{version=1, secretBytes=HASH[-847010189]}", secret3.toString());
    }

    @Test
    void createSecretInvalid() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new EncryptionSecret(null, 1));
        assertEquals("Secret can't be null or empty", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> new EncryptionSecret("".getBytes(StandardCharsets.UTF_8), 1));
        assertEquals("Secret can't be null or empty", ex.getMessage());
    }

    @Test
    void secretsDataGetSecretPositive() throws StorageClientException, StorageCryptoException {
        String secretsDataInJson = "{\n" +
                "    \"currentVersion\": 1,\n" +
                "    \"secrets\": [\n" +
                "        {\"secret\": \"password0\", \"version\": 0},\n" +
                "        {\"secret\": \"password1\", \"version\": 1}\n" +
                "    ]\n" +
                "}";
        SecretsData secretsData = SecretsDataGenerator.fromJson(secretsDataInJson);
        Secret secret = secretsData.getSecret(null);
        assertEquals(1, secret.getVersion());
        secret = secretsData.getSecret(1);
        assertEquals(1, secret.getVersion());
        secret = secretsData.getSecret(0);
        assertEquals(0, secret.getVersion());
    }
}
