package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StorageTest {
    private Storage storage;
    private String country = "US";
    private String batchWriteRecordKey = "batch_write_key";
    private String writeRecordKey = "write_key";
    private String profileKey = "profileKey";
    private String key2 = "key2";
    private String key3 = "key3";
    private Integer batchWriteRangeKey = 2;
    private Integer writeRangeKey = 1;
    private String recordBody = "test";

    private String secret = "passwordpasswordpasswordpassword";
    private int version = 0;
    private boolean isKey = true;
    private int currentVersion = 0;

    @BeforeEach
    public void init() throws Exception {
        SecretKeysData secretKeysData = new SecretKeysData();
        SecretKey secretKey = new SecretKey();
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKey.setSecret(secret);
        secretKey.setVersion(version);
        secretKey.setIsKey(isKey);
        secretKeyList.add(secretKey);
        secretKeysData.setSecrets(secretKeyList);
        secretKeysData.setCurrentVersion(currentVersion);
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(() -> secretKeysData);

        storage = new StorageImpl(
                "envId",
                "apiKey",
                secretKeyAccessor
        );
    }

    @Test
    @Order(1)
    public void batchWriteTest() throws StorageException {
        List<Record> records = new ArrayList<>();
        records.add(new Record(country, batchWriteRecordKey, recordBody, profileKey, batchWriteRangeKey, key2, key3));
        storage.createBatch(country, records);
    }

    @Test
    @Order(2)
    public void writeTest() throws StorageException {
        Record record = new Record(country, writeRecordKey, recordBody, profileKey, writeRangeKey, key2, key3);
        storage.create(record);
    }

    @Test
    @Order(3)
    public void readTest() throws StorageException {
        Record incomingRecord = storage.read(country, writeRecordKey);
        assertEquals(writeRecordKey, incomingRecord.getKey());
        assertEquals(recordBody, incomingRecord.getBody());
        assertEquals(profileKey, incomingRecord.getProfileKey());
        assertEquals(key2, incomingRecord.getKey2());
        assertEquals(key3, incomingRecord.getKey3());
    }

    @Test
    @Order(4)
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(key2)
                .rangeKeyEq(writeRangeKey);
        BatchRecord batchRecord = storage.find(country, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(writeRecordKey, batchRecord.getRecords().get(0).getKey());
    }

    @Test
    @Order(5)
    public void findOneTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(key2)
                .rangeKeyEq(writeRangeKey);
        Record d = storage.findOne(country, builder);
        assertEquals(writeRecordKey, d.getKey());
        assertEquals(recordBody, d.getBody());
    }

    @Test
    @Order(6)
    public void updateOneTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(key2)
                .rangeKeyEq(writeRangeKey);
        String newBody = "{\"hello\":\"world\"}";
        String newKey2 = "newKey2";
        Record incomingRecord = storage.read(country, writeRecordKey);
        incomingRecord.setBody(newBody);
        incomingRecord.setKey2(newKey2);
        storage.updateOne(country, builder, incomingRecord);
        Record updatedRecord = storage.read(country, writeRecordKey);
        assertEquals(writeRecordKey, updatedRecord.getKey());
        assertEquals(newBody, updatedRecord.getBody());
        assertEquals(newKey2, updatedRecord.getKey2());
    }

    @Test
    @Order(7)
    public void deleteTest() throws StorageException {
        storage.delete(country, writeRecordKey);
        storage.delete(country, batchWriteRecordKey);
        // Cannot read deleted record
        Record writeMethodRecord = storage.read(country, writeRecordKey);
        Record batchWriteMethodRecord = storage.read(country, batchWriteRecordKey);
        assertEquals(null, writeMethodRecord);
        assertEquals(null, batchWriteMethodRecord);

    }
}