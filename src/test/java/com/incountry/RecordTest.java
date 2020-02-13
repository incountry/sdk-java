package com.incountry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.incountry.exceptions.StorageCryptoException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecordTest {

    @Expose (serialize = false, deserialize = false)
    public String country;
    @Expose
    public String key;
    @Expose
    public String body;
    @Expose
    @SerializedName("profile_key")
    public String profileKey;
    @Expose
    @SerializedName("range_key")
    public int rangeKey;
    @Expose
    public String key2;
    @Expose
    public String key3;

    @Before
    public void init() {
        country = "US";
        key = "key1";
        body = "body";
        key2 = "key2";
        key3 = "key3";
        profileKey = "profileKey";
        rangeKey = 1;
    }


    @Test
    public void testToJsonString() throws StorageCryptoException {
        String comparisonString = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
        Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String recordJsonString = record.toJsonString(null);
        assertEquals(comparisonString, recordJsonString);
    }

    @Test
    public void testToJsonObject() throws StorageCryptoException {
        JsonElement jsonElement = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJsonTree(this);
        JsonObject jsonObject = (JsonObject) jsonElement;
        Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        JsonObject recordJsonObject = record.toJsonObject(null);

        assertEquals(jsonObject.get("key"), recordJsonObject.get("key"));
        assertEquals(jsonObject.get("body"), recordJsonObject.get("body"));
        assertEquals(jsonObject.get("profile_key"), recordJsonObject.get("profile_key"));
        assertEquals(jsonObject.get("range_key"), recordJsonObject.get("range_key"));
        assertEquals(jsonObject.get("key2"), recordJsonObject.get("key2"));
        assertEquals(jsonObject.get("key3"), recordJsonObject.get("key3"));

    }

    @Test
    public void testFromString() throws StorageCryptoException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("body", "test");
        jsonObject.addProperty("env_id", "5422b4ba-016d-4a3b-aea5-a832083697b1");
        jsonObject.addProperty("key", "write_key");
        jsonObject.addProperty("key2", "key2");
        jsonObject.addProperty("key3", "key3");
        jsonObject.addProperty("profile_key", "profileKey");
        jsonObject.addProperty("range_key", 1);
        jsonObject.addProperty("version", 2);
        String jsonString = new Gson().toJson(jsonObject);
        System.out.println();
        Record record = Record.fromString(jsonString, null);

        assertEquals(jsonObject.get("key").getAsString(), record.getKey());
        assertEquals(jsonObject.get("body").getAsString(), record.getBody());
        assertEquals(jsonObject.get("profile_key").getAsString(), record.getProfileKey());
        assertEquals(jsonObject.get("range_key").getAsNumber(), record.getRangeKey());
        assertEquals(jsonObject.get("key2").getAsString(), record.getKey2());
        assertEquals(jsonObject.get("key3").getAsString(), record.getKey3());
    }


}
