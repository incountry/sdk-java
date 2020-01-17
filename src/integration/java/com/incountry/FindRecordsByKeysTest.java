package com.incountry;

import com.incountry.exceptions.FindOptionsException;
import com.incountry.exceptions.StorageException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

@RunWith(Parameterized.class)
public class FindRecordsByKeysTest extends BaseTest {

    private Record expectedRecord;

    @Before
    public void setUp() throws Exception {
        expectedRecord = writeRecord(encryption, country);
    }

    @After
    public void tearDown() throws Exception {
        storage.delete(country, expectedRecord.getKey());
    }

    @Test
    public void findByKeyTest()
            throws StorageException, IOException, FindOptionsException {

        FindFilter filter = new FindFilter();
        filter.setKeyParam(new FilterStringParam(expectedRecord.getKey()));

        BatchRecord batch = storage.find(country, filter, new FindOptions());
        validateBatch(batch, expectedRecord);
    }

    @Test
    public void findByKey2Test()
            throws StorageException, IOException, FindOptionsException {

        FindFilter filter = new FindFilter();
        filter.setKey2Param(new FilterStringParam(expectedRecord.getKey2()));

        BatchRecord batch = storage.find(country, filter, new FindOptions());
        validateBatch(batch, expectedRecord);
    }

    @Test
    public void findByKey3Test()
            throws StorageException, IOException, FindOptionsException {

        FindFilter filter = new FindFilter();
        filter.setKey3Param(new FilterStringParam(expectedRecord.getKey3()));

        BatchRecord batch = storage.find(country, filter, new FindOptions());
        validateBatch(batch, expectedRecord);
    }

    @Test
    public void findByProfileKeyTest()
            throws StorageException, IOException, FindOptionsException {

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(expectedRecord.getProfileKey()));

        BatchRecord batch = storage.find(country, filter, new FindOptions());
        validateBatch(batch, expectedRecord);
    }

    @Test
    @Ignore
    public void findByCountryTest()
            throws StorageException, IOException, FindOptionsException {

        BatchRecord batch = storage.find(country, new FindFilter(), new FindOptions());
        validateBatch(batch, expectedRecord);
    }

    private void validateBatch(BatchRecord batch, Record expectedRecord) {
        List<Record> records = Arrays.asList(batch.getRecords());

        assertEquals("Validate records number", 1, records.size());
        assertEquals("Validate offset value", 0, batch.getOffset());
        assertEquals("Validate limit value", 100, batch.getLimit());
        assertEquals("Validate batch count value", records.size(), batch.getCount());
        assertEquals("Validate batch count value", records.size(), batch.getTotal());

        assertReflectionEquals("Find record validation",
                records.get(0), expectedRecord, IGNORE_DEFAULTS);  //Ignore country field
    }

}
