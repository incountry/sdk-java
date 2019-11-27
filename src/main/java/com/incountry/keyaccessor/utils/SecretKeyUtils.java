package com.incountry.keyaccessor.utils;

import com.google.gson.Gson;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SecretKeyUtils {

    /**
     * Convert string to SecretKeyData object
     * @param secretKeyString simple string or json
     * @return SecretKeyData object wich contain secret keys and there versions
     */
    static public SecretKeysData convertStringToSecretKeyData(String secretKeyString) {

        if (isJson(secretKeyString)) {
            Gson g = new Gson();
            SecretKeysData secretKeysData = g.fromJson(secretKeyString, SecretKeysData.class);
            return secretKeysData;
        }
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secretKeyString);
        List<SecretKey> secretKeys = new ArrayList<>();
        secretKeys.add(secretKey);
        SecretKeysData secretKeysData = new SecretKeysData();
        secretKeysData.setSecrets(secretKeys);

        return secretKeysData;
    }

    static public boolean isJson(String string) {
        try {
            new JSONObject(string);
            return true;
        } catch (JSONException e) {
            return false;
        }

    }


}
