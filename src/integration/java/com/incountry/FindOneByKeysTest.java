package com.incountry;


import com.incountry.exceptions.StorageException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

@RunWith(Parameterized.class)
public class FindOneByKeysTest extends BaseTest {

    private FindOptions options = new FindOptions(1, 0);

    public FindOneByKeysTest() throws FindOptions.FindOptionsException {
    }


    @Test
    public void findOneByKeyTest() throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKeyParam(new FilterStringParam(expectedRecord.getKey()));

        Record actualRecord = storage.findOne(country, filter, options);
        assertReflectionEquals("Find record validation", actualRecord, expectedRecord, IGNORE_DEFAULTS);  //Ignore country field
    }

    @Test
    public void findOneByKey2Test() throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey2Param(new FilterStringParam(expectedRecord.getKey2()));

        Record actualRecord = storage.findOne(country, filter, options);
        assertReflectionEquals("Find record validation", actualRecord, expectedRecord, IGNORE_DEFAULTS);  //Ignore country field
    }


    @Test
    public void findOneByKey3Test() throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey3Param(new FilterStringParam(expectedRecord.getKey3()));

        Record actualRecord = storage.findOne(country, filter, options);
        assertReflectionEquals("Find record validation", actualRecord, expectedRecord, IGNORE_DEFAULTS);  //Ignore country field
    }

    @Test
    public void findOneByProfileKeyTest() throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(expectedRecord.getProfileKey()));

        Record actualRecord = storage.findOne(country, filter, options);
        assertReflectionEquals("Find record validation", actualRecord, expectedRecord, IGNORE_DEFAULTS);  //Ignore country field
    }


    @Test
    public void findOneByRangeKeyTest() throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);
        FindFilter filter = new FindFilter();
        filter.setRangeKeyParam(new FilterRangeParam(expectedRecord.getRangeKey()));

        Record actualRecord = storage.findOne(country, filter, options);
        assertReflectionEquals("Find record validation", actualRecord, expectedRecord, IGNORE_DEFAULTS);  //Ignore country field
    }
}
