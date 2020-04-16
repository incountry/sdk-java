package com.incountry.residence.sdk.keyaccessor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import com.incountry.residence.sdk.tools.keyaccessor.utils.SecretKeyUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SecretKeyUtilsTest {

    @Test
    public void testConvertStringToSecretKeyDataWhenSecretKeyStringIsJson() {
        String secret = "password__password__password__32";
        int version = 1;
        boolean isKey = true;
        SecretKey secretKey = new SecretKey(secret, version, isKey);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        int currentVersion = 1;
        SecretKeysData secretKeysData = new SecretKeysData();
        secretKeysData.setSecrets(secretKeyList);
        secretKeysData.setCurrentVersion(currentVersion);

        String secretKeyString = new Gson().toJson(secretKeysData);

        SecretKeysData resultSecretKeysData = SecretKeyUtils.getSecretKeyDataFromString(secretKeyString);
        assertEquals(currentVersion, resultSecretKeysData.getCurrentVersion());
        assertEquals(secret, resultSecretKeysData.getSecrets().get(0).getSecret());
        assertEquals(version, resultSecretKeysData.getSecrets().get(0).getVersion());
        assertEquals(isKey, resultSecretKeysData.getSecrets().get(0).getIsKey());
    }

    @Test
    public void testConvertStringToSecretKeyDataWhenSecretKeyStringIsNotJson() {
        String secret = "user_password";
        int version = 0;
        int currentVersion = 0;

        SecretKeysData resultSecretKeysData = SecretKeyUtils.getSecretKeyDataFromString("user_password");
        assertEquals(currentVersion, resultSecretKeysData.getCurrentVersion());
        assertEquals(secret, resultSecretKeysData.getSecrets().get(0).getSecret());
        assertEquals(version, resultSecretKeysData.getSecrets().get(0).getVersion());
        assertFalse(resultSecretKeysData.getSecrets().get(0).getIsKey());
    }

    @Test
    public void testIsJson() {
        JsonObject jsonWithoutSecretKeyDataFields = new JsonObject();
        jsonWithoutSecretKeyDataFields.addProperty("body", "test");
        jsonWithoutSecretKeyDataFields.addProperty("key", "write_key");
        jsonWithoutSecretKeyDataFields.addProperty("key2", "key2");
        jsonWithoutSecretKeyDataFields.addProperty("profile_key", "profileKey");
        jsonWithoutSecretKeyDataFields.addProperty("range_key", 1);
        jsonWithoutSecretKeyDataFields.addProperty("version", 2);
        String jsonString = new Gson().toJson(jsonWithoutSecretKeyDataFields);

        assertNotNull(JsonUtils.getSecretKeysDataFromJson(jsonString));
        assertEquals(0, JsonUtils.getSecretKeysDataFromJson(jsonString).getCurrentVersion());
        assertNull(JsonUtils.getSecretKeysDataFromJson(jsonString).getSecrets());
        assertNull(JsonUtils.getSecretKeysDataFromJson("NotJsonString"));

        JsonObject jsonWithSecret = new JsonObject();
        jsonWithSecret.addProperty("secret", "someSecret");
        jsonWithSecret.addProperty("isKey", "false");
        jsonWithSecret.addProperty("version", "1");
        JsonArray array = new JsonArray();
        array.add(jsonWithSecret);
        JsonObject jsonWithSecretKeyDataFields = new JsonObject();
        jsonWithSecretKeyDataFields.addProperty("currentVersion", "1");
        jsonWithSecretKeyDataFields.add("secrets", array);
        jsonString = new Gson().toJson(jsonWithSecretKeyDataFields);
        SecretKeysData data = JsonUtils.getSecretKeysDataFromJson(jsonString);

        assertNotNull(data);
        assertEquals(1, data.getCurrentVersion());
        assertEquals(1, data.getSecrets().size());
        assertEquals("someSecret", data.getSecrets().get(0).getSecret());
        assertEquals(1, data.getSecrets().get(0).getVersion());
        assertFalse(data.getSecrets().get(0).getIsKey());
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

        assertThrows(IllegalArgumentException.class,
                () -> SecretKeyAccessor.getAccessor(() -> secretDataWrongJson));
    }

}
