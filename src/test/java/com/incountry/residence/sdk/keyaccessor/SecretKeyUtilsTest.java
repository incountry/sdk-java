package com.incountry.residence.sdk.keyaccessor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import com.incountry.residence.sdk.tools.keyaccessor.utils.SecretKeyUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecretKeyUtilsTest {

    @Test
    public void testConvertStringToSecretKeyDataWhenSecretKeyStringIsJson() {
        String secret = "user_password";
        int version = 1;
        boolean isKey = true;
        int currentVersion = 1;

        SecretKeysData secretKeysData = new SecretKeysData();
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(version);
        secretKey.setIsKey(isKey);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        secretKeysData.setSecrets(secretKeyList);
        secretKeysData.setCurrentVersion(currentVersion);

        String secretKeyString = new Gson().toJson(secretKeysData);

        SecretKeysData resultSecretKeysData = SecretKeyUtils.convertStringToSecretKeyData(secretKeyString);
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

        SecretKeysData resultSecretKeysData = SecretKeyUtils.convertStringToSecretKeyData("user_password");
        assertEquals(currentVersion, resultSecretKeysData.getCurrentVersion());
        assertEquals(secret, resultSecretKeysData.getSecrets().get(0).getSecret());
        assertEquals(version, resultSecretKeysData.getSecrets().get(0).getVersion());
        assertNull(resultSecretKeysData.getSecrets().get(0).getIsKey());
    }

    @Test
    public void testIsJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("body", "test");
        jsonObject.addProperty("key", "write_key");
        jsonObject.addProperty("key2", "key2");
        jsonObject.addProperty("profile_key", "profileKey");
        jsonObject.addProperty("range_key", 1);
        jsonObject.addProperty("version", 2);
        String jsonString = new Gson().toJson(jsonObject);

        assertTrue(SecretKeyUtils.isJson(jsonString));
        assertFalse(SecretKeyUtils.isJson("NotJsonString"));
    }

}
