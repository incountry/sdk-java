package com.incountry;

import com.incountry.exceptions.FindOptionsException;
import com.incountry.exceptions.StorageException;
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
                        "      \"secret\": \"passwordpasswordpasswordpassword\",\n" +
                        "      \"version\": 0,\n" +
                        "      \"isKey\": \"true\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"currentVersion\": 0\n" +
                        "}";
            }

        });

        store = new Storage(
                "5422b4ba-016d-4a3b-aea5-a832083697b1",
                "nbskjo.4b1ede21dbf7437eb7d8e9ab024dc380",
                "https://us.qa.incountry.io",
                false,
                secretKeyAccessor);
    }

    @Test
    public void test1BatchWrite() throws GeneralSecurityException, StorageException, IOException {
        List<Record> records = new ArrayList<>();
        records.add(new Record(country, batchWriteRecordKey, recordBody, profileKey, batchWriteRangeKey, key2, key3));
        store.batchWrite(country, records);
    }

    @Test
    public void test2Write() throws GeneralSecurityException, IOException, StorageException {
        Record record = new Record(country, writeRecordKey, recordBody, profileKey, writeRangeKey, key2, key3);
        store.write(record);
    }

    @Test
    public void test3Read() throws GeneralSecurityException, IOException, StorageException {
        Record d = store.read(country, writeRecordKey);
        assertEquals(writeRecordKey, d.getKey());
        assertEquals(recordBody, d.getBody());
        assertEquals(profileKey, d.getProfileKey());
        assertEquals(key2, d.getKey2());
        assertEquals(key3, d.getKey3());
    }

    @Test
    public void test4Find() throws FindOptionsException, GeneralSecurityException, StorageException, IOException {
        FindFilter filter = new FindFilter(null, new FilterStringParam(key2), null, null, new FilterRangeParam(writeRangeKey), null);
        FindOptions options = new FindOptions(100, 0);
        BatchRecord d = store.find(country, filter, options);
        assertEquals(1, d.getCount());
        assertEquals(1, d.getRecords().size());
        assertEquals(writeRecordKey, d.getRecords().get(0).getKey());
    }

    @Test
    public void test5FindOne() throws FindOptionsException, GeneralSecurityException, StorageException, IOException {
        FindFilter filter = new FindFilter(null, new FilterStringParam(key2), null, null, new FilterRangeParam(writeRangeKey), null);
        FindOptions options = new FindOptions(100, 0);
        Record d = store.findOne(country, filter, options);
        assertEquals(writeRecordKey, d.getKey());
        assertEquals(recordBody, d.getBody());
    }

    @Test
    public void test6UpdateOne() throws FindOptionsException, GeneralSecurityException, StorageException, IOException {
        FindFilter filter = new FindFilter(null, new FilterStringParam(key2), null, null, new FilterRangeParam(writeRangeKey), null);
        String newBody = "{\"hello\":\"world\"}";
        String newKey2 = "newKey2";
        Record current = store.read(country, writeRecordKey);
        current.setBody(newBody);
        current.setKey2(newKey2);
        store.updateOne(country, filter, current);
        Record updated = store.read(country, writeRecordKey);
        assertEquals(writeRecordKey, updated.getKey());
        assertEquals(newBody, updated.getBody());
        assertEquals(newKey2, updated.getKey2());
    }

    @Test
    public void test7Delete() throws GeneralSecurityException, IOException, StorageException {
        store.delete(country, writeRecordKey);
        store.delete(country, batchWriteRecordKey);
        // Cannot read deleted record
        Record writeMethodRecord = store.read(country, writeRecordKey);
        Record batchWriteMethodRecord = store.read(country, batchWriteRecordKey);
        assertEquals(null, writeMethodRecord);
        assertEquals(null, batchWriteMethodRecord);
    }
}