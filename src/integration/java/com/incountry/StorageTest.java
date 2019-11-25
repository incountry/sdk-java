package com.incountry;

import com.incountry.exceptions.StorageException;
import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.keyaccessor.model.SecretKeysData;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StorageTest {
    private Storage store;
    private String country = "US";
    private String recordKey = "some_key";
    private String profileKey = "profileKey";
    private String key2 = "key2";
    private String key3 = "key3";
    private Integer rangeKey = 1;
    private String recordBody = "{\"name\":\"last\"}";


    @Before
    public void beforeTestMethod() throws Exception {
//        SecretKeyAccessorImpl secretKeyAccessorImpl = new SecretKeyAccessorImpl(System.getenv("INC_SECRET_KEY"));
//        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(System.getenv("INC_SECRET_KEY"));
//        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretKeyGenerator <SecretKeysData>() {
//            @Override
//            public SecretKeysData generate() {
//                return null;
//            }
//        });


//        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretKeyGenerator <String>() {
//            @Override
//            public String generate() {
//                return "1234";
//            }
//        });
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretKeyGenerator <String>() {
            @Override
            public String generate() {
                return "{\n" +
                        "  \"secrets\": [\n" +
                        "    {\n" +
                        "      \"secret\": \"123\",\n" +
                        "      \"version\": 1\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"currentVersion\": 1\n" +
                        "}";
            }
        });
        this.store = new Storage(secretKeyAccessor);
    }

    @Test
    public void test1Write() throws GeneralSecurityException, IOException, StorageException {
        Record record = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
        store.write(record);
    }

//    @Test
//    public void test2Read() throws GeneralSecurityException, IOException, StorageException {
//        Record d = store.read(country, recordKey);
//        assertEquals(recordKey, d.getKey());
//        assertEquals(recordBody, d.getBody());
//        assertEquals(profileKey, d.getProfileKey());
//        assertEquals(key2, d.getKey2());
//        assertEquals(key3, d.getKey3());
//    }
//
//    @Test
//    public void test3Find() throws FindOptions.FindOptionsException, GeneralSecurityException, StorageException, IOException {
//        FindFilter filter = new FindFilter(null, null, new FilterRangeParam(rangeKey), new FilterStringParam(key2), null);
//        FindOptions options = new FindOptions(100, 0);
//        BatchRecord d = store.find(country, filter, options);
//        assertEquals(1, d.getCount());
//        assertEquals(1, d.getRecords().length);
//        assertEquals(recordKey, d.getRecords()[0].getKey());
//    }
//
//    @Test
//    public void test4FindOne() throws FindOptions.FindOptionsException, GeneralSecurityException, StorageException, IOException {
//        FindFilter filter = new FindFilter(null, null, new FilterRangeParam(rangeKey), new FilterStringParam(key2), null);
//        FindOptions options = new FindOptions(100, 0);
//        Record d = store.findOne(country, filter, options);
//        assertEquals(recordKey, d.getKey());
//        assertEquals(recordBody, d.getBody());
//    }
//
//    @Test
//    public void test5UpdateOne() throws FindOptions.FindOptionsException, GeneralSecurityException, StorageException, IOException {
//        FindFilter filter = new FindFilter(null, null, new FilterRangeParam(rangeKey), new FilterStringParam(key2), null);
//        String newBody = "{\"hello\":\"world\"}";
//        String newKey2 = "newKey2";
//        Record current = store.read(country, recordKey);
//        current.setBody(newBody);
//        current.setKey2(newKey2);
//        Record d = store.updateOne(country, filter, current);
//        Record updated = store.read(country, recordKey);
//        assertEquals(recordKey, updated.getKey());
//        assertEquals(newBody, updated.getBody());
//        assertEquals(newKey2, updated.getKey2());
//    }
//
//    @Test
//    public void test6Delete() throws GeneralSecurityException, IOException, StorageException {
//        store.delete(country, recordKey);
//        // Cannot read deleted record
//        Record d = store.read(country, recordKey);
//        assertEquals(null, d);
//    }
}