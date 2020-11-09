package com.incountry.residence.sdk.keyaccessor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretsTest {
    private static final byte[] PASSWORD_32 = "password__password__password__32".getBytes(StandardCharsets.UTF_8);

    @Test
    void testConvertStringToSecretsDataWhenSecretKeyStringIsJson() throws Exception {
        String secretKeyString = "{\n" +
                "  \"secrets\": [\n" +
                "    {\n" +
                "      \"secret\": \"password__password__password__32\",\n" +
                "      \"version\": 1,\n" +
                "      \"isKey\": true,\n" +
                "      \"isForCustomEncryption\": false\n" +
                "    }\n" +
                "  ],\n" +
                "  \"currentVersion\": 1\n" +
                "}";
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromJson(secretKeyString);
        SecretsData resultSecretsData = accessor.getSecretsData();
        assertEquals(1, resultSecretsData.getCurrentVersion());
        assertTrue(Arrays.equals(PASSWORD_32, resultSecretsData.getSecrets().get(0).getSecret()));
        assertEquals(1, resultSecretsData.getSecrets().get(0).getVersion());
        assertTrue(resultSecretsData.getSecrets().get(0).isKey());
        assertFalse(resultSecretsData.getSecrets().get(0).isForCustomEncryption());
    }

    @Test
    void testConvertStringToSecretsDataWhenSecretKeyStringIsNotJson() throws Exception {
        String secretString = "user_password";
        int version = 0;
        int currentVersion = 0;
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("user_password");
        SecretsData resultSecretsData = accessor.getSecretsData();
        assertEquals(currentVersion, resultSecretsData.getCurrentVersion());
        assertTrue(Arrays.equals(secretString.getBytes(StandardCharsets.UTF_8), resultSecretsData.getSecrets().get(0).getSecret()));
        assertEquals(version, resultSecretsData.getSecrets().get(0).getVersion());
        assertFalse(resultSecretsData.getSecrets().get(0).isForCustomEncryption());
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

        StorageClientException ex = assertThrows(StorageClientException.class, () -> JsonUtils.getSecretsDataFromJson(jsonString));
        assertEquals("Incorrect JSON with SecretsData", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> JsonUtils.getSecretsDataFromJson("NotJsonString"));
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
        SecretsData data = JsonUtils.getSecretsDataFromJson(jsonString2);

        assertNotNull(data);
        assertEquals(1, data.getCurrentVersion());
        assertEquals(1, data.getSecrets().size());
        assertTrue(Arrays.equals("someSecret".getBytes(StandardCharsets.UTF_8), data.getSecrets().get(0).getSecret()));
        assertEquals(1, data.getSecrets().get(0).getVersion());
        assertFalse(data.getSecrets().get(0).isForCustomEncryption());
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
    void testValidationOfSecretsData() throws StorageClientException {
        SecretKey secretKey1 = new SecretKey("password1".getBytes(StandardCharsets.UTF_8), 0, false);
        SecretKey secretKey2 = new SecretKey("password2".getBytes(StandardCharsets.UTF_8), 1, false);
        SecretKey secretKey3 = new SecretKey("password3".getBytes(StandardCharsets.UTF_8), 0, false);
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> new SecretsData(Arrays.asList(secretKey1, secretKey2, secretKey3), 1));
        assertTrue(ex1.getMessage().startsWith("SecretKey versions must be unique. Got duplicates for:"));
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> new SecretsData(Arrays.asList(secretKey1, secretKey2), 2));
        assertEquals(String.format("There is no SecretKey version that matches current version %d", 2), ex2.getMessage());
    }

    @Test
    void secretsDataToStringTest() throws StorageClientException {
        String expectedStart = "SecretsData{secrets=[SecretKey{secret=HASH[";
        String expectedEnd = "], version=0, isKey=false, isForCustomEncryption=false}], currentVersion=0}";
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("user_password");
        SecretsData secretsData = accessor.getSecretsData();
        assertTrue(secretsData.toString().startsWith(expectedStart));
        assertTrue(secretsData.toString().endsWith(expectedEnd));
    }

    @Test
    void secretsDataNegativeVersionTest() throws StorageClientException {
        List<SecretKey> secrets = Collections.singletonList(new SecretKey("password".getBytes(StandardCharsets.UTF_8), 1, false));
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new SecretsData(secrets, -1));
        assertEquals("Current version must be >= 0", ex.getMessage());
    }

    @Test
    void secretsDataEmptySecretsTest() {
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> new SecretsData(null, 1));
        assertEquals("Secrets in SecretData are null", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> new SecretsData(new ArrayList<>(), 1));
        assertEquals("Secrets in SecretData are null", ex2.getMessage());
    }

    @Test
    void secretsKeyWrongLength() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new SecretKey("123".getBytes(StandardCharsets.UTF_8), 1, true));
        assertTrue(ex.getMessage().startsWith("Wrong key length for secret key with 'isKey==true'. Should be"));

    }

    @Test
    void testValidationOfSecretKey() throws StorageClientException {
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> SecretKey.validateSecretKey(PASSWORD_32, 0, true, true));
        assertEquals("SecretKey can have either 'isKey' or 'isForCustomEncryption' set to True, not both", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> SecretKey.validateSecretKey("secret".getBytes(StandardCharsets.UTF_8), 0, true, true));
        assertTrue(ex2.getMessage().startsWith("Wrong key length for secret key with 'isKey==true'. Should be"));
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> SecretKey.validateSecretKey("secret".getBytes(StandardCharsets.UTF_8), 0, true, false));
        assertTrue(ex3.getMessage().startsWith("Wrong key length for secret key with 'isKey==true'. Should be"));
        SecretKey.validateSecretKey("secret".getBytes(StandardCharsets.UTF_8), 0, false, false);
        SecretKey.validateSecretKey("secret".getBytes(StandardCharsets.UTF_8), 0, false, true);
        StorageClientException ex4 = assertThrows(StorageClientException.class, () -> SecretKey.validateSecretKey(new byte[]{}, 0, false, true));
        assertEquals("Secret can't be null", ex4.getMessage());
        StorageClientException ex5 = assertThrows(StorageClientException.class, () -> SecretKey.validateSecretKey("".getBytes(StandardCharsets.UTF_8), 0, false, true));
        assertEquals("Secret can't be null", ex5.getMessage());
        StorageClientException ex6 = assertThrows(StorageClientException.class, () -> SecretKey.validateSecretKey(null, 0, false, true));
        assertEquals("Secret can't be null", ex6.getMessage());
    }
}
