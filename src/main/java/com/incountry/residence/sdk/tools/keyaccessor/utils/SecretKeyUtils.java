package com.incountry.residence.sdk.tools.keyaccessor.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;

import java.util.ArrayList;
import java.util.List;

public class SecretKeyUtils {

    public static final int DEFAULT_VERSION = 0;

    private SecretKeyUtils() {
    }

    /**
     * Convert string to SecretKeyData object
     *
     * @param secretKeyString simple string or json
     * @return SecretKeyData object which contain secret keys and there versions
     */
    public static SecretKeysData convertStringToSecretKeyData(String secretKeyString) {

        if (isJson(secretKeyString)) {
            Gson gson = new Gson();
            return gson.fromJson(secretKeyString, SecretKeysData.class);
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
            new Gson().fromJson(string, JsonObject.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}
