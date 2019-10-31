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
    private static final String P_COUNTRY = "country";
    private static final String P_BODY = "body";
    private static final String P_KEY = "key";
    private static final String P_KEY_2 = "key2";
    private static final String P_KEY_3 = "key3";
    private static final String P_PROFILE_KEY = "profile_key";
    private static final String P_RANGE_KEY = "range_key";
    private static final String P_PAYLOAD = "payload";
    private static final String P_META = "meta";

    String country;
    String key;
    String body;
    String profile_key;
    String range_key;
    String key2;
    String key3;

    public Data(String country, String key, String body, String profileKey, String rangeKey, String key2, String key3) {
        this.country = country;
        this.key = key;
        this.body = body;
        this.profile_key = profileKey;
        this.range_key = rangeKey;
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
        ObjectMapper mapper = new ObjectMapper();
        JsonNode o = mapper.readTree(s);
        String country = extractKey(o, P_COUNTRY);
        String key = extractKey(o, P_KEY);
        String body = extractKey(o, P_BODY);
        String profileKey = extractKey(o, P_PROFILE_KEY);
        String rangeKey = extractKey(o, P_RANGE_KEY);
        String key2 = extractKey(o, P_KEY_2);
        String key3 = extractKey(o, P_KEY_3);

        if (body != null && mCrypto != null){
            String[] parts = body.split(":");

            body = mCrypto.decrypt(body);

            if (parts.length != 2){
                key = mCrypto.decrypt(key);
                profileKey = mCrypto.decrypt(profileKey);
                key2 = mCrypto.decrypt(key2);
                key3 = mCrypto.decrypt(key3);
            } else {
                JsonNode bodyObj = mapper.readTree(body);
                body = extractKey(bodyObj, P_PAYLOAD);
                String meta = extractKey(bodyObj, P_META);
                JsonNode metaObj = mapper.readTree(meta);
                key = extractKey(metaObj, P_KEY);
                profileKey = extractKey(metaObj, P_PROFILE_KEY);
                key2 = extractKey(metaObj, P_KEY_2);
                key3 = extractKey(metaObj, P_KEY_3);
            }
        }
        return new Data(country, key, body, profileKey, rangeKey, key2, key3);
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
                .put(P_KEY, key)
                .put(P_KEY_2, key2)
                .put(P_KEY_3, key3)
                .put(P_PROFILE_KEY, profile_key)
                .put(P_RANGE_KEY, range_key)
                .put(P_BODY, body).toString();
        }

        String bodyJson = new JSONObject()
            .put(P_PAYLOAD, body)
            .put(P_META, new JSONObject()
                .put(P_KEY, key)
                .put(P_KEY_2, key2)
                .put(P_KEY_3, key3)
                .put(P_PROFILE_KEY, profile_key)
                .put(P_RANGE_KEY, range_key).toString()
            ).toString();

        String encryptedBodyJson = mCrypto.encrypt(bodyJson);

        return new JSONObject()
            .put(P_KEY, mCrypto.createKeyHash(key))
            .put(P_KEY_2, mCrypto.createKeyHash(key2))
            .put(P_KEY_3, mCrypto.createKeyHash(key3))
            .put(P_PROFILE_KEY, mCrypto.createKeyHash(profile_key))
            .put(P_RANGE_KEY, range_key)
            .put(P_BODY, encryptedBodyJson).toString();
    }
}
