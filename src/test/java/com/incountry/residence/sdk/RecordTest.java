package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecordTest {
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

    @BeforeEach
    public void init() {
        key = "key1";
        body = "body";
        key2 = "key2";
        key3 = "key3";
        profileKey = "profileKey";
        rangeKey = 1;
    }

    @Test
    public void testSimpleMerge() {
        String newKey = "newKey";
        String newBody = "newBody";

        Record baseRecord = new Record(key, body);
        Record mergedRecord = new Record(newKey, newBody);
        Record resultRecord = Record.merge(baseRecord, mergedRecord);

        assertEquals(newKey, resultRecord.getKey());
        assertEquals(newBody, resultRecord.getBody());
    }


    @Test
    public void testMerge() {
        String newKey = "newKey";
        String newBody = "newBody";
        String newProfileKey = "newProfileKey";
        String newKey2 = "newKey2";

        Record baseRecord = new Record(key, body, profileKey, rangeKey, key2, key3);
        Record mergedRecord = new Record(newKey, newBody, newProfileKey, null, newKey2, null);
        Record resultRecord = Record.merge(baseRecord, mergedRecord);

        assertEquals(newKey, resultRecord.getKey());
        assertEquals(newBody, resultRecord.getBody());
        assertEquals(newProfileKey, resultRecord.getProfileKey());
        assertEquals((Integer) rangeKey, resultRecord.getRangeKey());
        assertEquals(newKey2, resultRecord.getKey2());
        assertEquals(key3, resultRecord.getKey3());
    }

    @Test
    public void testFromString() throws StorageCryptoException, StorageClientException, StorageServerException {
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
        Record record = JsonUtils.recordFromString(jsonString, null);
        assertEquals(jsonObject.get("key").getAsString(), record.getKey());
        assertEquals(jsonObject.get("body").getAsString(), record.getBody());
        assertEquals(jsonObject.get("profile_key").getAsString(), record.getProfileKey());
        assertEquals(jsonObject.get("range_key").getAsNumber(), record.getRangeKey());
        assertEquals(jsonObject.get("key2").getAsString(), record.getKey2());
        assertEquals(jsonObject.get("key3").getAsString(), record.getKey3());
    }

    @Test
    public void testToJsonObject() throws StorageCryptoException, StorageClientException {
        JsonElement jsonElement = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJsonTree(this);
        JsonObject jsonObject = (JsonObject) jsonElement;
        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        JsonObject recordJsonObject = JsonUtils.toJson(record, null);
        assertEquals(jsonObject.get("key"), recordJsonObject.get("key"));
        assertEquals(jsonObject.get("body"), recordJsonObject.get("body"));
        assertEquals(jsonObject.get("profile_key"), recordJsonObject.get("profile_key"));
        assertEquals(jsonObject.get("range_key"), recordJsonObject.get("range_key"));
        assertEquals(jsonObject.get("key2"), recordJsonObject.get("key2"));
        assertEquals(jsonObject.get("key3"), recordJsonObject.get("key3"));
    }

    @Test
    public void testToJsonObjectWithPTE() throws StorageCryptoException, StorageClientException, StorageServerException {
        String bodyWithJson = "{\"FirstName\":\"<first name>\"}";
        Record record = new Record(key, bodyWithJson, profileKey, rangeKey, key2, key3);
        CryptoManager crypto = new CryptoManager(null, "envId", null, false);
        String recordJson = JsonUtils.toJsonString(record, crypto);
        assertEquals("{\"version\":0,\"key\":\"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\"key2\":\"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\"key3\":\"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\"profile_key\":\"ee597d2e9e8ed19fd1b891af76495586da223cdbd6251fdac201531451b3329d\",\"range_key\":1,\"body\":\"pt:eyJwYXlsb2FkIjoie1wiRmlyc3ROYW1lXCI6XCI8Zmlyc3QgbmFtZT5cIn0iLCJtZXRhIjp7ImtleSI6ImtleTEiLCJrZXkyIjoia2V5MiIsImtleTMiOiJrZXkzIiwicHJvZmlsZV9rZXkiOiJwcm9maWxlS2V5IiwicmFuZ2Vfa2V5IjoxfX0=\"}", recordJson);
        Record record2 = JsonUtils.recordFromString(recordJson, crypto);
        assertEquals(record, record2);
    }

    /**
     * test case: serialize to json string some custom object with the same structure
     * as Record (orders of fields are different). Then test fuction of serialize/deserialize
     * to JSON and compare objects
     *
     * @throws StorageCryptoException when problem with encryption
     */
    @Test
    public void testToJsonString() throws StorageCryptoException, StorageClientException, StorageServerException {
        String quaziJsonString = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
        Record nativeRecord = new Record(key, body, profileKey, rangeKey, key2, key3);
        String nativeRecordJson = JsonUtils.toJsonString(nativeRecord, null);
        Record recordFromQuazy = JsonUtils.recordFromString(quaziJsonString, null);
        Record recordFromNative = JsonUtils.recordFromString(nativeRecordJson, null);
        assertEquals(recordFromQuazy, recordFromNative);
    }

    @Test
    public void testBatchToStringString() {
        Record record1 = new Record(key + 1, body + 1, profileKey + 1, rangeKey + 1, key2 + 1, key3 + 1);
        Record record2 = new Record(key + 2, body + 2, profileKey + 2, rangeKey + 2, key2 + 2, key3 + 2);
        BatchRecord batchRecord = new BatchRecord(Arrays.asList(record1, record2), 2, 2, 0, 2, new ArrayList<>());
        String str = batchRecord.toString();
        assertTrue(str.contains(String.valueOf(record1.hashCode())));
        assertTrue(str.contains(String.valueOf(record2.hashCode())));
    }

    @Test
    public void testEquals() {
        Record record1 = new Record(key, body, profileKey, rangeKey, key2, key3);
        Record record2 = new Record(key, body, profileKey, rangeKey, key2, key3);
        assertEquals(record1, record1);
        assertEquals(record1, record2);
        assertEquals(record2, record1);
        assertNotEquals(null, record1);
        assertNotEquals(record1, UUID.randomUUID());
        record2 = new Record(key + 1, body, profileKey, rangeKey, key2, key3);
        assertNotEquals(record1, record2);
        record2 = new Record(key, body + 1, profileKey, rangeKey, key2, key3);
        assertNotEquals(record1, record2);
        record2 = new Record(key, body, profileKey + 1, rangeKey, key2, key3);
        assertNotEquals(record1, record2);
        record2 = new Record(key, body, profileKey, rangeKey + 1, key2, key3);
        assertNotEquals(record1, record2);
        record2 = new Record(key, body, profileKey, rangeKey, key2 + 1, key3);
        assertNotEquals(record1, record2);
        record2 = new Record(key, body, profileKey, rangeKey, key2, key3 + 1);
        assertNotEquals(record1, record2);
    }
}
