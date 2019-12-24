package com.incountry;

import com.incountry.exceptions.StorageException;
import com.incountry.find.FindBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.security.GeneralSecurityException;


@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class FindRecordsByKeysTest extends FindBaseTest {

    private FindOptions defaultFindOptions = new FindOptions(10, 0);

    @DisplayName("Find records by key")
    @ParameterizedTest(name = "Find records by key" + testName)
    @CsvSource({"false, US"})
    public void findByKeyTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKeyParam(new FilterStringParam(expectedRecord.getKey()));

        findRecords(country, expectedRecord, filter, defaultFindOptions);
    }

    @DisplayName("Find records by key2")
    @ParameterizedTest(name = "Find records by key2" + testName)
    @CsvSource({"false, US"})
    public void findByKey2Test(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey2Param(new FilterStringParam(expectedRecord.getKey2()));

        findRecords(country, expectedRecord, filter, defaultFindOptions);
    }

    @DisplayName("Find records by key3")
    @ParameterizedTest(name = "Find records by key3" + testName)
    @CsvSource({"false, US"})
    public void findByKey3Test(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey3Param(new FilterStringParam(expectedRecord.getKey3()));

        findRecords(country, expectedRecord, filter, defaultFindOptions);
    }

    @DisplayName("Find records by profile key")
    @ParameterizedTest(name = "Find records by profile key" + testName)
    @CsvSource({"false, US"})
    public void findByProfileKeyTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(expectedRecord.getProfileKey()));

        findRecords(country, expectedRecord, filter, defaultFindOptions);
    }

    private void findRecords(String country, Record expectedRecord, FindFilter filter, FindOptions findOptions)
            throws StorageException, IOException, GeneralSecurityException {
        BatchRecord batch = storage.find(country, filter, findOptions);
        Record[] records = batch.getRecords();

        Assertions.assertAll("Validate batch fields",
                validateBatchFields(findOptions, records.length, batch));

        Assertions.assertAll("Validate record",
                validateRecord(expectedRecord, records[0]));
    }

    private FindRecordsByKeysTest() throws FindOptions.FindOptionsException {
    }
}
