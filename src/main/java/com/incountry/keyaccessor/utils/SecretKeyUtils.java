package com.incountry.keyaccessor.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.incountry.keyaccessor.model.SecretKey;
import com.incountry.keyaccessor.model.SecretKeysData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SecretKeyUtils {

    static public SecretKeysData convertStringToSecretKeyData(String secretKeyString) {


        if (isJson(secretKeyString)) {
            Gson g = new Gson();
            SecretKeysData p = g.fromJson(secretKeyString, SecretKeysData.class);
        }
//        SecretKey secretKey = new SecretKey();
//        secretKey.setSecret(secretKeyString);
//        List<SecretKey> secretKeys = new ArrayList<>();
//        secretKeys.add(secretKey);
//        SecretKeysData secretKeysData = new SecretKeysData();
//        secretKeysData.setSecretKeys(secretKeys);

//        return secretKeysData;
        return new SecretKeysData();
    }

    static public boolean isJson(String string) {

        try {
            new JSONObject(string);
            return true;
        } catch (JSONException e) {
            return false;
        }


//        try {
//            new Gson().fromJson(string, Object.class);
//            return true;
//        } catch(com.google.gson.JsonSyntaxException ex) {
//            return false;
//        }
    }


}
