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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SecretsTest {

    @Test
    public void testConvertStringToSecretsDataWhenSecretKeyStringIsJson() throws Exception {
        String secret = "password__password__password__32";
        int version = 1;
        boolean isKey = true;
        boolean isForCustomEncryption = false;
        SecretKey secretKey = new SecretKey(secret, version, isKey, isForCustomEncryption);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        int currentVersion = 1;
        SecretsData secretsData = new SecretsData(secretKeyList, currentVersion);
        String secretKeyString = new Gson().toJson(secretsData);

        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromJson(secretKeyString);
        SecretsData resultSecretsData = accessor.getSecretsData();
        assertEquals(currentVersion, resultSecretsData.getCurrentVersion());
        assertEquals(secret, resultSecretsData.getSecrets().get(0).getSecret());
        assertEquals(version, resultSecretsData.getSecrets().get(0).getVersion());
        assertEquals(isKey, resultSecretsData.getSecrets().get(0).isKey());
        assertEquals(isForCustomEncryption, resultSecretsData.getSecrets().get(0).isForCustomEncryption());
    }

    @Test
    public void testConvertStringToSecretsDataWhenSecretKeyStringIsNotJson() throws Exception {
        String secret = "user_password";
        int version = 0;
        int currentVersion = 0;
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("user_password");
        SecretsData resultSecretsData = accessor.getSecretsData();
        assertEquals(currentVersion, resultSecretsData.getCurrentVersion());
        assertEquals(secret, resultSecretsData.getSecrets().get(0).getSecret());
        assertEquals(version, resultSecretsData.getSecrets().get(0).getVersion());
        assertFalse(resultSecretsData.getSecrets().get(0).isForCustomEncryption());
    }

    @Test
    public void testIsJson() throws StorageClientException {
        JsonObject jsonWithoutSecretsDataFields = new JsonObject();
        jsonWithoutSecretsDataFields.addProperty("body", "test");
        jsonWithoutSecretsDataFields.addProperty("key", "write_key");
        jsonWithoutSecretsDataFields.addProperty("key2", "key2");
        jsonWithoutSecretsDataFields.addProperty("profile_key", "profileKey");
        jsonWithoutSecretsDataFields.addProperty("range_key", 1);
        jsonWithoutSecretsDataFields.addProperty("version", 2);
        String jsonString = new Gson().toJson(jsonWithoutSecretsDataFields);

        assertNotNull(JsonUtils.getSecretsDataFromJson(jsonString));
        assertEquals(0, JsonUtils.getSecretsDataFromJson(jsonString).getCurrentVersion());
        assertNull(JsonUtils.getSecretsDataFromJson(jsonString).getSecrets());
        assertThrows(StorageClientException.class, () -> JsonUtils.getSecretsDataFromJson("NotJsonString"));

        JsonObject jsonWithSecret = new JsonObject();
        jsonWithSecret.addProperty("secret", "someSecret");
        jsonWithSecret.addProperty("isKey", "false");
        jsonWithSecret.addProperty("version", "1");
        JsonArray array = new JsonArray();
        array.add(jsonWithSecret);
        JsonObject jsonWithSecretsDataFields = new JsonObject();
        jsonWithSecretsDataFields.addProperty("currentVersion", "1");
        jsonWithSecretsDataFields.add("secrets", array);
        jsonString = new Gson().toJson(jsonWithSecretsDataFields);
        SecretsData data = JsonUtils.getSecretsDataFromJson(jsonString);

        assertNotNull(data);
        assertEquals(1, data.getCurrentVersion());
        assertEquals(1, data.getSecrets().size());
        assertEquals("someSecret", data.getSecrets().get(0).getSecret());
        assertEquals(1, data.getSecrets().get(0).getVersion());
        assertFalse(data.getSecrets().get(0).isForCustomEncryption());
    }

    @Test
    public void testWrongJson() {
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

        assertThrows(StorageClientException.class, () -> SecretsDataGenerator.fromJson(secretDataWrongJson));
    }

    @Test
    public void testValidationOfSecretsData() throws StorageClientException {
        SecretKey secretKey1 = new SecretKey("password1", 0, false);
        SecretKey secretKey2 = new SecretKey("password2", 1, false);
        SecretKey secretKey3 = new SecretKey("password3", 0, false);
        assertThrows(StorageClientException.class, () -> new SecretsData(Arrays.asList(secretKey1, secretKey2, secretKey3), 1));
        assertThrows(StorageClientException.class, () -> new SecretsData(Arrays.asList(secretKey1, secretKey2), 2));
    }

    @Test
    public void secretsDataToStringTest() throws StorageClientException {
        String expected = "SecretsData{secrets=[SecretKey{secret=HASH[1267537070], version=0, isKey=false, isForCustomEncryption=false}], currentVersion=0}";
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("user_password");
        SecretsData secretsData = accessor.getSecretsData();
        assertEquals(expected, secretsData.toString());
    }

    @Test
    public void secretsDataNegativeVersionTest() throws StorageClientException {
        List<SecretKey> secrets = Arrays.asList(new SecretKey("password", 1, false));
        assertThrows(StorageClientException.class, () -> new SecretsData(secrets, -1));
    }

    @Test
    public void secretsDataEmptySecretsTest() {
        assertThrows(StorageClientException.class, () -> new SecretsData(null, 1));
        assertThrows(StorageClientException.class, () -> new SecretsData(new ArrayList<>(), 1));
    }

    @Test
    public void secretsKeyWrongLength() {
        assertThrows(StorageClientException.class, () -> new SecretKey("123", 1, true));
    }

    @Test
    public void testValidationOfSecretKey() throws StorageClientException {
        assertThrows(StorageClientException.class, () -> SecretKey.validateSecretKey("secret", 0, true, true));
        assertThrows(StorageClientException.class, () -> SecretKey.validateSecretKey("secret", 0, true, false));
        SecretKey.validateSecretKey("secret", 0, false, false);
        SecretKey.validateSecretKey("secret", 0, false, true);
    }
}
