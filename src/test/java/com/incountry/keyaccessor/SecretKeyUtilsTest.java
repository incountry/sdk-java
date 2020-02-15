package com.incountry.keyaccessor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import com.incountry.keyaccessor.utils.SecretKeyUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SecretKeyUtilsTest {

    private String secret = "user_password";
    private int version = 0;
    private boolean isKey = true;
    private int currentVersion = 0;

    @Test
    public void testConvertStringToSecretKeyData(){

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
