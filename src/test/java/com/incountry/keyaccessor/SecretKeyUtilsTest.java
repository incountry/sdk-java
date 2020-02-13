package com.incountry.keyaccessor;

import com.google.gson.Gson;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import com.incountry.keyaccessor.utils.SecretKeyUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SecretKeyUtilsTest {

    private String secretKeyString;

    private String secret = "user_password";
    private int version = 0;
    private boolean isKey = true;
    private int currentVersion = 0;

    @Before
    public void init() {
        SecretKeysData secretKeysData = new SecretKeysData();
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(version);
        secretKey.setIsKey(isKey);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        secretKeysData.setSecrets(secretKeyList);
        secretKeysData.setCurrentVersion(0);

        secretKeyString = new Gson().toJson(secretKeysData);
    }

    @Test
    public void testConvertStringToSecretKeyData(){
        SecretKeysData secretKeysData = SecretKeyUtils.convertStringToSecretKeyData(secretKeyString);
        assertEquals(currentVersion, secretKeysData.getCurrentVersion());
        assertEquals(secret, secretKeysData.getSecrets().get(0).getSecret());
        assertEquals(version, secretKeysData.getSecrets().get(0).getVersion());
        assertEquals(isKey, secretKeysData.getSecrets().get(0).getIsKey());
    }

}
