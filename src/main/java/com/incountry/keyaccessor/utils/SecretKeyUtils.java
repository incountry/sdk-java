package com.incountry.keyaccessor.utils;

import com.google.gson.Gson;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SecretKeyUtils {

    public static final int DEFAULT_VERSION = 0;

    private SecretKeyUtils() {
    }

    /**
     * Convert string to SecretKeyData object
     * @param secretKeyString simple string or json
     * @return SecretKeyData object wich contain secret keys and there versions
     */
    public static SecretKeysData convertStringToSecretKeyData(String secretKeyString) {

        if (isJson(secretKeyString)) {
            Gson g = new Gson();
            return g.fromJson(secretKeyString, SecretKeysData.class);
        }
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secretKeyString);
        List<SecretKey> secretKeys = new ArrayList<>();
        secretKeys.add(secretKey);
        SecretKeysData secretKeysData = new SecretKeysData();
        secretKeysData.setSecrets(secretKeys);
        secretKeysData.setCurrentVersion(DEFAULT_VERSION);

        return secretKeysData;
    }

    public static boolean isJson(String string) {
        try {
            new JSONObject(string);
            return true;
        } catch (JSONException e) {
            return false;
        }

    }


}
