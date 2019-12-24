package com.incountry;


import com.incountry.exceptions.StorageException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.security.GeneralSecurityException;


@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class FindOneByKeysTest extends BaseTest {

    private FindOptions options = new FindOptions(1, 0);

    @DisplayName("Find one record by key")
    @ParameterizedTest(name = "Find one record by key" + testName)
    @CsvSource({"false, US"})
    public void findOneByKeyTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKeyParam(new FilterStringParam(expectedRecord.getKey()));

        Record actualRecord = storage.findOne(country, filter, options);
        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    @DisplayName("Find one record by key2")
    @ParameterizedTest(name = "Find one record by key2" + testName)
    @CsvSource({"false, US"})
    public void findOneByKey2Test(boolean encryption, String country) throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey2Param(new FilterStringParam(expectedRecord.getKey2()));

        Record actualRecord = storage.findOne(country, filter, options);
        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }


    @DisplayName("Find one record by key3")
    @ParameterizedTest(name = "Find one record by key3" + testName)
    @CsvSource({"false, US"})
    public void findOneByKey3Test(boolean encryption, String country) throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey3Param(new FilterStringParam(expectedRecord.getKey3()));

        Record actualRecord = storage.findOne(country, filter, options);
        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    @DisplayName("Find one record by profile key")
    @ParameterizedTest(name = "Find one record by profile key" + testName)
    @CsvSource({"false, US"})
    public void findOneByProfileKeyTest(boolean encryption, String country) throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(expectedRecord.getProfileKey()));

        Record actualRecord = storage.findOne(country, filter, options);
        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }


    @Disabled
    @DisplayName("Find one record by range key")
    @ParameterizedTest(name = "Find one record by range key" + testName)
    @CsvSource({"false, US"})
    public void findOneByRangeKeyTest(boolean encryption, String country) throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);
        FindFilter filter = new FindFilter();
        filter.setRangeKeyParam(new FilterRangeParam(expectedRecord.getRangeKey()));

        Record actualRecord = storage.findOne(country, filter, options);
        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    private FindOneByKeysTest() throws FindOptions.FindOptionsException {
    }
}
