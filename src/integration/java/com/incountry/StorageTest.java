package com.incountry;

import com.incountry.exceptions.StorageCryptoException;
import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.keyaccessor.generator.SecretKeyGenerator;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StorageTest {
    private Storage store;
    private String country = "US";
    private String batchWriteRecordKey = "batch_write_key";
    private String writeRecordKey = "write_key";
    private String profileKey = "profileKey";
    private String key2 = "key2";
    private String key3 = "key3";
    private Integer batchWriteRangeKey = 2;
    private Integer writeRangeKey = 1;
    private String recordBody = "test";


    @Before
    public void beforeTestMethod() throws Exception {
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretKeyGenerator <String>() {
            @Override
            public String generate() {
                return "{\n" +
                        "  \"secrets\": [\n" +
                        "    {\n" +
                        "      \"secret\": \"123\",\n" +
                        "      \"version\": 0\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"currentVersion\": 0\n" +
                        "}";
            }
        });

        store = new Storage(
                "envId",
                "apiKey",
                secretKeyAccessor
        );

    }

    @Test
    public void test1BatchWrite() throws StorageServerException, StorageCryptoException {
        List<Record> records = new ArrayList<>();
        records.add(new Record(country, batchWriteRecordKey, recordBody, profileKey, batchWriteRangeKey, key2, key3));
        store.batchWrite(country, records);
    }

    @Test
    public void test2Write() throws StorageServerException, StorageCryptoException {
        Record record = new Record(country, writeRecordKey, recordBody, profileKey, writeRangeKey, key2, key3);
        store.write(record);
    }

    @Test
    public void test3Read() throws StorageServerException, StorageCryptoException {
        Record incomingRecord = store.read(country, writeRecordKey);
        assertEquals(writeRecordKey, incomingRecord.getKey());
        assertEquals(recordBody, incomingRecord.getBody());
        assertEquals(profileKey, incomingRecord.getProfileKey());
        assertEquals(key2, incomingRecord.getKey2());
        assertEquals(key3, incomingRecord.getKey3());
    }

    @Test
    public void test4Find() throws StorageServerException, StorageCryptoException {
        FindFilter filter = new FindFilter(null, new FilterStringParam(key2), null, null, new FilterRangeParam(writeRangeKey), null);
        FindOptions options = new FindOptions(100, 0);
        BatchRecord batchRecord = store.find(country, filter, options);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(writeRecordKey, batchRecord.getRecords().get(0).getKey());
    }

    @Test
    public void test5FindOne() throws GeneralSecurityException, StorageException, IOException {
        FindFilter filter = new FindFilter(null, new FilterStringParam(key2), null, null, new FilterRangeParam(writeRangeKey), null);
        FindOptions options = new FindOptions(100, 0);
        Record d = store.findOne(country, filter, options);
        assertEquals(writeRecordKey, d.getKey());
        assertEquals(recordBody, d.getBody());
    }

    @Test
    public void test6UpdateOne() throws StorageServerException, StorageCryptoException {
        FindFilter filter = new FindFilter(null, new FilterStringParam(key2), null, null, new FilterRangeParam(writeRangeKey), null);
        String newBody = "{\"hello\":\"world\"}";
        String newKey2 = "newKey2";
        Record incomingRecord = store.read(country, writeRecordKey);
        incomingRecord.setBody(newBody);
        incomingRecord.setKey2(newKey2);
        store.updateOne(country, filter, incomingRecord);
        Record updatedRecord = store.read(country, writeRecordKey);
        assertEquals(writeRecordKey, updatedRecord.getKey());
        assertEquals(newBody, updatedRecord.getBody());
        assertEquals(newKey2, updatedRecord.getKey2());
    }

    @Test
    public void test7Delete() throws StorageServerException, StorageCryptoException {
        store.delete(country, writeRecordKey);
        store.delete(country, batchWriteRecordKey);
        // Cannot read deleted record
        Record writeMethodRecord = store.read(country, writeRecordKey);
        Record batchWriteMethodRecord = store.read(country, batchWriteRecordKey);
        assertEquals(null, writeMethodRecord);
        assertEquals(null, batchWriteMethodRecord);

    }
}