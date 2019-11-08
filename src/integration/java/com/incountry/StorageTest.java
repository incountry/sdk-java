package com.incountry;

import com.incountry.exceptions.StorageException;
import com.incountry.key_accessor.SecretKeyAccessor;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
        SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor(System.getenv("INC_SECRET_KEY"));
        this.store = new Storage(secretKeyAccessor);
    }

    @Test
    public void test1Write() throws GeneralSecurityException, IOException, StorageException {
        store.write(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
    }

    @Test
    public void test2Read() throws GeneralSecurityException, IOException, StorageException {
        Data d = store.read(country, recordKey);
        assertEquals(recordKey, d.getKey());
        assertEquals(recordBody, d.getBody());
        assertEquals(profileKey, d.getProfileKey());
        assertEquals(key2, d.getKey2());
        assertEquals(key3, d.getKey3());
    }

    @Test
    public void test3Find() throws FindOptions.FindOptionsException, GeneralSecurityException, StorageException, IOException {
        FindFilter filter = new FindFilter(null, null, new FilterRangeParam(rangeKey), new FilterStringParam(key2), null);
        FindOptions options = new FindOptions(100, 0);
        BatchData d = store.find(country, filter, options);
        assertEquals(1, d.getCount());
        assertEquals(1, d.getRecords().length);
        assertEquals(recordKey, d.getRecords()[0].getKey());
    }

    @Test
    public void test4FindOne() throws FindOptions.FindOptionsException, GeneralSecurityException, StorageException, IOException {
        FindFilter filter = new FindFilter(null, null, new FilterRangeParam(rangeKey), new FilterStringParam(key2), null);
        FindOptions options = new FindOptions(100, 0);
        Data d = store.findOne(country, filter, options);
        assertEquals(recordKey, d.getKey());
        assertEquals(recordBody, d.getBody());
    }

    @Test
    public void test5UpdateOne() throws FindOptions.FindOptionsException, GeneralSecurityException, StorageException, IOException {
        FindFilter filter = new FindFilter(null, null, new FilterRangeParam(rangeKey), new FilterStringParam(key2), null);
        String newBody = "{\"hello\":\"world\"}";
        String newKey2 = "newKey2";
        Data d = store.updateOne(country, filter, null, newBody, null, null, newKey2, null);
        Data updated = store.read(country, recordKey);
        assertEquals(recordKey, d.getKey());
        assertEquals(newBody, d.getBody());
        assertEquals(newKey2, d.getKey2());
    }

    @Test
    public void test6Delete() throws GeneralSecurityException, IOException, StorageException {
        String response = store.delete(country, recordKey);
        assertNotEquals(null, response);
        // Cannot read deleted record
        Data d = store.read(country, recordKey);
        assertEquals(null, d);
    }
}