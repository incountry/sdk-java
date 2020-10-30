package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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

class RecordTest {
    @Expose
    @SerializedName("record_key")
    public String recordKey;
    @Expose
    public String body;
    @Expose
    @SerializedName("profile_key")
    public String profileKey;
    @Expose
    @SerializedName("range_key1")
    public long rangeKey1;
    public long rangeKey2;
    public long rangeKey3;
    public long rangeKey4;
    public long rangeKey5;
    public long rangeKey6;
    public long rangeKey7;
    public long rangeKey8;
    public long rangeKey9;
    public long rangeKey10;
    public String key1;
    @Expose
    public String key2;
    @Expose
    public String key3;
    public String key4;
    public String key5;
    public String key6;
    public String key7;
    public String key8;
    public String key9;
    public String key10;
    public String errorCorrectionKey1;
    public String errorCorrectionKey2;
    public String precommit;

    @BeforeEach
    public void init() {
        body = "body";
        recordKey = "recordKey1";
        key1 = "key1";
        key2 = "key2";
        key3 = "key3";
        key4 = "key4";
        key5 = "key5";
        key6 = "key6";
        key7 = "key7";
        key8 = "key8";
        key9 = "key9";
        key10 = "key10";
        profileKey = "profileKey";
        rangeKey1 = 1;
        rangeKey2 = 2;
        rangeKey3 = 3;
        rangeKey4 = 4;
        rangeKey5 = 5;
        rangeKey6 = 6;
        rangeKey7 = 7;
        rangeKey8 = 8;
        rangeKey9 = 9;
        rangeKey10 = 10;
        precommit = "precommit";
        errorCorrectionKey1 = "errorCorrectionKey1";
        errorCorrectionKey2 = "errorCorrectionKey2";
    }

    @Test
    void testFromString() throws StorageCryptoException, StorageClientException, StorageServerException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("body", "test");
        jsonObject.addProperty("env_id", "5422b4ba-016d-4a3b-aea5-a832083697b1");
        jsonObject.addProperty("record_key", "write_record_key");
        jsonObject.addProperty("key2", "key2");
        jsonObject.addProperty("key3", "key3");
        jsonObject.addProperty("profile_key", "profileKey");
        jsonObject.addProperty("range_key1", 1);
        jsonObject.addProperty("version", 2);
        String jsonString = new Gson().toJson(jsonObject);
        Record record = JsonUtils.recordFromString(jsonString, null);
        assertEquals(jsonObject.get("record_key").getAsString(), record.getRecordKey());
        assertEquals(jsonObject.get("body").getAsString(), record.getBody());
        assertEquals(jsonObject.get("profile_key").getAsString(), record.getProfileKey());
        assertEquals(jsonObject.get("range_key1").getAsLong(), record.getRangeKey1());
        assertEquals(jsonObject.get("key2").getAsString(), record.getKey2());
        assertEquals(jsonObject.get("key3").getAsString(), record.getKey3());
    }

    @Test
    void testToJsonObject() throws StorageCryptoException, StorageClientException {
        JsonElement jsonElement = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJsonTree(this);
        JsonObject jsonObject = (JsonObject) jsonElement;
        Record record = new Record(recordKey, body)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        JsonObject recordJsonObject = JsonUtils.toJson(record, null);
        assertEquals(jsonObject.get("record_key"), recordJsonObject.get("record_key"));
        assertEquals(jsonObject.get("body"), recordJsonObject.get("body"));
        assertEquals(jsonObject.get("profile_key"), recordJsonObject.get("profile_key"));
        assertEquals(jsonObject.get("range_key1"), recordJsonObject.get("range_key1"));
        assertEquals(jsonObject.get("key2"), recordJsonObject.get("key2"));
        assertEquals(jsonObject.get("key3"), recordJsonObject.get("key3"));
    }

    @Test
    void testToJsonObjectWithPTE() throws StorageCryptoException, StorageClientException, StorageServerException {
        String bodyWithJson = "{\"FirstName\":\"<first name>\"}";
        Record record = new Record(recordKey, bodyWithJson)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        CryptoManager crypto = new CryptoManager(null, "envId", null, false, true);
        String recordJson = JsonUtils.toJsonString(record, crypto);
        assertEquals("{\"version\":0," +
                        "\"is_encrypted\":false," +
                        "\"record_key\":\"b246499acb9e5c3161f9fb40184324f9d2ac384530ec583fcc0f8a8b12090c71\"," +
                        "\"key2\":\"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\"," +
                        "\"key3\":\"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\"," +
                        "\"profile_key\":\"ee597d2e9e8ed19fd1b891af76495586da223cdbd6251fdac201531451b3329d\"," +
                        "\"range_key1\":1," +
                        "\"body\":\"pt:eyJwYXlsb2FkIjoie1wiRmlyc3ROYW1lXCI6XCI8Zmlyc3QgbmFtZT5cIn0iLCJtZXRhIjp7InJlY29yZF9rZXkiOiJyZWNvcmRLZXkxIiwia2V5MiI6ImtleTIiLCJrZXkzIjoia2V5MyIsInByb2ZpbGVfa2V5IjoicHJvZmlsZUtleSJ9fQ==\"," +
                        "\"attachments\":[]}",
                recordJson);
        Record record2 = JsonUtils.recordFromString(recordJson, crypto);
        assertEquals(record, record2);

        //{"version":0,"is_encrypted":false,"record_key":"b246499acb9e5c3161f9fb40184324f9d2ac384530ec583fcc0f8a8b12090c71","key2":"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8","key3":"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6","profile_key":"ee597d2e9e8ed19fd1b891af76495586da223cdbd6251fdac201531451b3329d","range_key1":1,"body":"pt:eyJwYXlsb2FkIjoie1wiRmlyc3ROYW1lXCI6XCI8Zmlyc3QgbmFtZT5cIn0iLCJtZXRhIjp7InJlY29yZF9rZXkiOiJyZWNvcmRLZXkxIiwia2V5MiI6ImtleTIiLCJrZXkzIjoia2V5MyIsInByb2ZpbGVfa2V5IjoicHJvZmlsZUtleSJ9fQ=="}
    }

    /**
     * test case: serialize to json string some custom object with the same structure
     * as Record (orders of fields are different). Then test fuction of serialize/deserialize
     * to JSON and compare objects
     *
     * @throws StorageCryptoException when problem with encryption
     */
    @Test
    void testToJsonString() throws StorageCryptoException, StorageClientException, StorageServerException {
        String quaziJsonString = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
        Record nativeRecord = new Record(recordKey, body)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        String nativeRecordJson = JsonUtils.toJsonString(nativeRecord, null);
        Record recordFromQuazy = JsonUtils.recordFromString(quaziJsonString, null);
        Record recordFromNative = JsonUtils.recordFromString(nativeRecordJson, null);
        assertEquals(recordFromQuazy, recordFromNative);
    }

    @Test
    void testBatchToStringString() {
        Record record1 = new Record(recordKey + 1, body + 1)
                .setProfileKey(profileKey + 1)
                .setRangeKey1(rangeKey1 + 1)
                .setKey2(key2 + 1)
                .setKey3(key3 + 1);
        Record record2 = new Record(recordKey + 2, body + 2)
                .setProfileKey(profileKey + 2)
                .setRangeKey1(rangeKey1 + 2)
                .setKey2(key2 + 2)
                .setKey3(key3 + 2);
        BatchRecord batchRecord = new BatchRecord(Arrays.asList(record1, record2), 2, 2, 0, 2, new ArrayList<>());
        String str = batchRecord.toString();
        assertTrue(str.contains(String.valueOf(record1.hashCode())));
        assertTrue(str.contains(String.valueOf(record2.hashCode())));
    }

    @SuppressWarnings("java:S3415")
    @Test
    void testEquals() throws StorageClientException, StorageCryptoException, StorageServerException {
        Record record1 = new Record(recordKey, body)
                .setProfileKey(body)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        Record record2 = new Record(recordKey, body)
                .setProfileKey(body)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        assertEquals(record1, record1);
        assertEquals(record1, record2);
        assertEquals(record2, record1);
        assertNotEquals(null, record1);
        assertNotEquals(record1, null);
        assertNotEquals(record1, UUID.randomUUID());

        Record record3 = new Record(recordKey)
                .setKey1(key1)
                .setKey2(key2)
                .setKey3(key3)
                .setKey4(key4)
                .setKey5(key5)
                .setKey6(key6)
                .setKey7(key7)
                .setKey8(key8)
                .setKey9(key9)
                .setKey10(key10)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setRangeKey2(rangeKey2)
                .setRangeKey3(rangeKey3)
                .setRangeKey4(rangeKey4)
                .setRangeKey5(rangeKey5)
                .setRangeKey6(rangeKey6)
                .setRangeKey7(rangeKey7)
                .setRangeKey8(rangeKey8)
                .setRangeKey9(rangeKey9)
                .setRangeKey10(rangeKey10)
                .setBody(body)
                .setServiceKey1(errorCorrectionKey1)
                .setServiceKey2(errorCorrectionKey2)
                .setPrecommitBody(precommit);

        CryptoManager cryptoManager = new CryptoManager(null, "envId", null, false, true);
        String recordString = JsonUtils.toJsonString(record3, cryptoManager);
        Record record4 = JsonUtils.recordFromString(recordString, cryptoManager);
        assertEquals(record3, record4);

        record4.setPrecommitBody(record4.getPrecommitBody() + UUID.randomUUID());
        assertNotEquals(record3, record4);

        record4 = JsonUtils.recordFromString(recordString, cryptoManager)
                .setServiceKey1(record4.getServiceKey1() + UUID.randomUUID());
        assertNotEquals(record3, record4);

        record4 = JsonUtils.recordFromString(recordString, cryptoManager)
                .setServiceKey2(record4.getServiceKey2() + UUID.randomUUID());
        assertNotEquals(record3, record4);

        record4 = JsonUtils.recordFromString(recordString, cryptoManager)
                .setBody(record4.getBody() + UUID.randomUUID());
        assertNotEquals(record3, record4);

        record4 = JsonUtils.recordFromString(recordString, cryptoManager)
                .setProfileKey(record4.getProfileKey() + UUID.randomUUID());
        assertNotEquals(record3, record4);

        checkKeys(record3, recordString, cryptoManager);
        checkRangeKeys(record3, recordString, cryptoManager);

        String attachmentMetaJson = "{\n" +
                "   \"downloadLink\":\"123456\",\n" +
                "   \"fileId\":\"some_link\",\n" +
                "   \"fileName\":\"test_file\",\n" +
                "   \"hash\":\"1234567890\",\n" +
                "   \"mimeType\":\"text/plain\",\n" +
                "   \"size\":1000\n" +
                "}";
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new Gson().fromJson(attachmentMetaJson, JsonObject.class));

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("body", "test");
        jsonObject.addProperty("env_id", "5422b4ba-016d-4a3b-aea5-a832083697b1");
        jsonObject.addProperty("record_key", "write_record_key");
        jsonObject.addProperty("key2", "key2");
        jsonObject.addProperty("key3", "key3");
        jsonObject.addProperty("profile_key", "profileKey");
        jsonObject.addProperty("range_key1", 1);
        jsonObject.addProperty("version", 2);

        Record recordWithoutAttachmentMeta = JsonUtils.recordFromString(jsonObject.toString(), null);

        jsonObject.add("attachments", jsonArray);

        Record recordWithAttachmentMeta = JsonUtils.recordFromString(jsonObject.toString(), null);
        assertNotEquals(recordWithoutAttachmentMeta, recordWithAttachmentMeta);
    }

    private void checkRangeKeys(Record expectedRecord, String recordString, CryptoManager cryptoManager) throws StorageServerException, StorageClientException, StorageCryptoException {
        Record newRecord = JsonUtils.recordFromString(recordString, cryptoManager);
        newRecord.setRangeKey1(newRecord.getRangeKey1() + 1);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey2(newRecord.getRangeKey2() + 2);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey3(newRecord.getRangeKey3() + 3);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey4(newRecord.getRangeKey4() + 4);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey5(newRecord.getRangeKey5() + 5);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey6(newRecord.getRangeKey6() + 6);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey7(newRecord.getRangeKey7() + 7);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey8(newRecord.getRangeKey8() + 8);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey9(newRecord.getRangeKey9() + 9);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setRangeKey10(newRecord.getRangeKey10() + 10);
        assertNotEquals(expectedRecord, newRecord);
    }

    private void checkKeys(Record expectedRecord, String recordString, CryptoManager cryptoManager) throws StorageServerException, StorageClientException, StorageCryptoException {
        Record newRecord = JsonUtils.recordFromString(recordString, cryptoManager);
        newRecord.setRecordKey(newRecord.getRecordKey() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey1(newRecord.getKey1() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey2(newRecord.getKey2() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey3(newRecord.getKey3() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey4(newRecord.getKey4() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey5(newRecord.getKey5() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey6(newRecord.getKey6() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey7(newRecord.getKey7() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey8(newRecord.getKey8() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey9(newRecord.getKey9() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = JsonUtils.recordFromString(recordString, cryptoManager)
                .setKey10(newRecord.getKey10() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);
    }
}
