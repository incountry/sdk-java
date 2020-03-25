package com.incountry;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.incountry.crypto.Crypto;
import com.incountry.exceptions.StorageCryptoException;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.Map;


@JsonFilter("nullFilter")
public class Record {
    private static final String P_COUNTRY = "country";
    private static final String P_BODY = "body";
    private static final String P_KEY = "key";
    private static final String P_KEY_2 = "key2";
    private static final String P_KEY_3 = "key3";
    private static final String P_PROFILE_KEY = "profile_key";
    private static final String P_RANGE_KEY = "range_key";
    private static final String P_PAYLOAD = "payload";
    private static final String P_META = "meta";
    private static final String VERSION = "version";

    private String country;
    private String key;
    private String body;
    @SerializedName("profile_key")
    private String profileKey;
    @SerializedName("range_key")
    private Integer rangeKey;
    private String key2;
    private String key3;

    public Record() {
    }

    public Record(String country, String key, String body) {
        this.country = country;
        this.key = key;
        this.body = body;
    }

    public Record(String country, String key, String body, String profileKey, Integer rangeKey, String key2, String key3) {
        this.country = country;
        this.key = key;
        this.body = body;
        this.profileKey = profileKey;
        this.rangeKey = rangeKey;
        this.key2 = key2;
        this.key3 = key3;
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

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public Integer getRangeKey() {
        return rangeKey;
    }

    public void setRangeKey(Integer rangeKey) {
        this.rangeKey = rangeKey;
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

    private static <T> T mergeKeys(T a, T b) {
        return b != null ? b : a;
    }

    public static Record merge(Record base, Record merged) {
        String country = mergeKeys(base.getCountry(), merged.getCountry());
        String mergedKey = mergeKeys(base.getKey(), merged.getKey());
        String mergedBody = mergeKeys(base.getBody(), merged.getBody());
        String mergedProfileKey = mergeKeys(base.getProfileKey(), merged.getProfileKey());
        Integer mergedRangeKey = mergeKeys(base.getRangeKey(), merged.getRangeKey());
        String mergedKey2 = mergeKeys(base.getKey2(), merged.getKey2());
        String mergedKey3 = mergeKeys(base.getKey3(), merged.getKey3());

        return new Record(country, mergedKey, mergedBody, mergedProfileKey, mergedRangeKey, mergedKey2, mergedKey3);
    }

    /**
     * Get property value from json
     *
     * @param jsonObject json object
     * @param property   property name
     * @return property value
     */
    private static String getPropertyFromJson(JsonObject jsonObject, String property) {
        if (!jsonObject.has(property)) {
            return null;
        }
        return jsonObject.get(property).isJsonNull() ? null : jsonObject.get(property).getAsString();
    }

    /**
     * Create record object from json string
     *
     * @param jsonString json string
     * @param mCrypto    crypto object
     * @return record objects with data from json
     * @throws StorageCryptoException if decryption failed
     */
    public static Record fromString(String jsonString, Crypto mCrypto) throws StorageCryptoException {

        JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);

        String country = getPropertyFromJson(jsonObject, P_COUNTRY);
        String key = getPropertyFromJson(jsonObject, P_KEY);
        String body = getPropertyFromJson(jsonObject, P_BODY);
        String profileKey = getPropertyFromJson(jsonObject, P_PROFILE_KEY);
        Integer rangeKey = getPropertyFromJson(jsonObject, P_RANGE_KEY) != null ? Integer.parseInt(getPropertyFromJson(jsonObject, P_RANGE_KEY)) : null;
        String key2 = getPropertyFromJson(jsonObject, P_KEY_2);
        String key3 = getPropertyFromJson(jsonObject, P_KEY_3);

        Integer version = Integer.parseInt(getPropertyFromJson(jsonObject, VERSION) != null ? getPropertyFromJson(jsonObject, VERSION) : "0");

        if (body != null && mCrypto != null) {
            String[] parts = body.split(":");

            body = mCrypto.decrypt(body, version);

            if (parts.length != 2) {
                key = mCrypto.decrypt(key, version);
                profileKey = mCrypto.decrypt(profileKey, version);
                key2 = mCrypto.decrypt(key2, version);
                key3 = mCrypto.decrypt(key3, version);
            } else {
                JsonObject bodyObj = new Gson().fromJson(body, JsonObject.class);
                body = getPropertyFromJson(bodyObj, P_PAYLOAD);
                String meta = getPropertyFromJson(bodyObj, P_META);
                JsonObject metaObj = new Gson().fromJson(meta, JsonObject.class);
                key = getPropertyFromJson(metaObj, P_KEY);
                profileKey = getPropertyFromJson(metaObj, P_PROFILE_KEY);
                key2 = getPropertyFromJson(metaObj, P_KEY_2);
                key3 = getPropertyFromJson(metaObj, P_KEY_3);
            }
        }
        return new Record(country, key, body, profileKey, rangeKey, key2, key3);
    }

    /**
     * Converts a Record object to JsonObject
     *
     * @param mCrypto object which is using to encrypt data
     * @return JsonObject with Record data
     * @throws StorageCryptoException if encryption failed
     */
    public JsonObject toJsonObject(Crypto mCrypto) throws StorageCryptoException {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        if (mCrypto == null) {
            JsonObject recordJson = (JsonObject) gson.toJsonTree(this);
            recordJson.remove(P_COUNTRY);
            return recordJson;
        }

        JsonElement nodesElement = gson.toJsonTree(this);
        ((JsonObject) nodesElement).remove(P_COUNTRY);
        ((JsonObject) nodesElement).remove(P_BODY);

        Map<String, String> bodyJson = new HashMap<>();
        bodyJson.put(P_PAYLOAD, body);
        bodyJson.put(P_META, nodesElement.toString());
        String bodyJsonString = gson.toJson(bodyJson);

        Pair<String, Integer> encryptedBodyAndVersion = mCrypto.encrypt(bodyJsonString);

        JsonObject recordJson = new JsonObject();
        recordJson.addProperty(P_KEY, mCrypto.createKeyHash(key));
        recordJson.addProperty(P_KEY_2, mCrypto.createKeyHash(key2));
        recordJson.addProperty(P_KEY_3, mCrypto.createKeyHash(key3));
        recordJson.addProperty(P_PROFILE_KEY, mCrypto.createKeyHash(profileKey));
        recordJson.addProperty(P_RANGE_KEY, rangeKey);
        recordJson.addProperty(P_BODY, encryptedBodyAndVersion.getValue0());
        recordJson.addProperty(VERSION, encryptedBodyAndVersion.getValue1() != null ? encryptedBodyAndVersion.getValue1() : 0);

        return recordJson;
    }

    /**
     * @param mCrypto object which is using to encrypt data
     * @return Json string with Record data
     * @throws StorageCryptoException if encryption failed
     */
    public String toJsonString(Crypto mCrypto) throws StorageCryptoException {
        return toJsonObject(mCrypto).toString();
    }
}
