package com.incountry;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incountry.crypto.Crypto;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

@JsonFilter("nullFilter")
public class Data {
    String country;
    String key;
    String body;
    String profile_key;
    String range_key;
    String key2;
    String key3;

    public Data(String country, String key, String body, String profile_key, String range_key, String key2, String key3) {
        this.country = country;
        this.key = key;
        this.body = body;
        this.profile_key = profile_key;
        this.range_key = range_key;
        this.key2 = key2;
        this.key3 = key3;
    }

    private static String extractKey(JsonNode o, String k){
        if (o.has(k)){
            JsonNode v = o.get(k);
            if (!v.isNull()){
                return v.asText();
            }
        }
        return null;
    }

    public static Data fromString(String s, Crypto mCrypto) throws IOException, GeneralSecurityException {
        if (mCrypto == null) return fromUnencryptedString(s);
        return fromEncryptedString(s, mCrypto);
    }

    private static Data fromUnencryptedString(String s) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode o = mapper.readTree(s);
        String country = extractKey(o, "country");
        String key = extractKey(o, "key");
        String body = extractKey(o, "body");
        String profile_key = extractKey(o, "profile_key");
        String range_key = extractKey(o, "range_key");
        String key2 = extractKey(o, "key2");
        String key3 = extractKey(o, "key3");
        return new Data(country, key, body, profile_key, range_key, key2, key3);
    }

    private static Data fromEncryptedString(String s, Crypto mCrypto) throws IOException, GeneralSecurityException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode o = mapper.readTree(s);
        String country = extractKey(o, "country");
        String key = extractKey(o, "key");
        String body = extractKey(o, "body");
        String profile_key = extractKey(o, "profile_key");
        String range_key = extractKey(o, "range_key");
        String key2 = extractKey(o, "key2");
        String key3 = extractKey(o, "key3");

        if (body != null){
            body = mCrypto.decrypt(body);
            String[] parts = body.split(":");

            if (parts.length != 2){
                key = mCrypto.decrypt(key);
                if (profile_key != null) profile_key = mCrypto.decrypt(profile_key);
                if (key2 != null) key2 = mCrypto.decrypt(key2);
                if (key3 != null) key3 = mCrypto.decrypt(key3);
            } else {
                JsonNode bodyObj = mapper.readTree(body);
                body = extractKey(bodyObj, "payload");
                String meta = extractKey(bodyObj, "meta");
                JsonNode metaObj = mapper.readTree(meta);
                key = extractKey(metaObj, "key");
                if (profile_key != null) profile_key = extractKey(metaObj, "profile_key");
                if (key2 != null) key2 = extractKey(metaObj, "key2");
                if (key3 != null) key3 = extractKey(metaObj, "key3");
            }
        }

        return new Data(country, key, body, profile_key, range_key, key2, key3);
    }


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getProfile_key() {
        return profile_key;
    }

    public void setProfile_key(String profile_key) {
        this.profile_key = profile_key;
    }

    public String getRange_key() {
        return range_key;
    }

    public void setRange_key(String range_key) {
        this.range_key = range_key;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getKey3() {
        return key3;
    }

    public void setKey3(String key3) {
        this.key3 = key3;
    }

    public String toString(Crypto mCrypto) throws GeneralSecurityException, IOException {
        if (mCrypto == null) {
            return new JSONObject()
                .put("key", key)
                .put("key2", key2)
                .put("key3", key3)
                .put("profile_key", profile_key)
                .put("range_key", range_key)
                .put("body", body).toString();
        }

        String bodyJson = new JSONObject()
            .put("payload", body)
            .put("meta", new JSONObject()
                .put("key", key)
                .put("key2", key2)
                .put("key3", key3)
                .put("profile_key", profile_key)
                .put("range_key", range_key).toString()
            ).toString();

        String encryptedBodyJson = mCrypto.encrypt(bodyJson);

        String result = new JSONObject()
            .put("key", mCrypto.createKeyHash(key))
            .put("key2", mCrypto.createKeyHash(key2))
            .put("key3", mCrypto.createKeyHash(key3))
            .put("profile_key", mCrypto.createKeyHash(profile_key))
            .put("range_key", range_key)
            .put("body", encryptedBodyJson).toString();

        return result;
    }
}
