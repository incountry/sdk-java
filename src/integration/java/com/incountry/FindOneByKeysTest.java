/*
package com.incountry;


import com.incountry.exceptions.StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.security.GeneralSecurityException;


@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class FindOneByKeysTest extends BaseTest {

    private FindOptions options = new FindOptions(1, 0);
    private String country = "US";

    @DisplayName("Find one record by key")
    @ParameterizedTest(name = "Find one record by key" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findOneByKeyTest(boolean encryption) throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKeyParam(new FilterStringParam(expectedRecord.getKey()));

        Record actualRecord = storage.findOne(country, filter, options);
//        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    @DisplayName("Find one record by key2")
    @ParameterizedTest(name = "Find one record by key2" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findOneByKey2Test(boolean encryption) throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey2Param(new FilterStringParam(expectedRecord.getKey2()));

        Record actualRecord = storage.findOne(country, filter, options);
//        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }


    @DisplayName("Find one record by key3")
    @ParameterizedTest(name = "Find one record by key3" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findOneByKey3Test(boolean encryption) throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey3Param(new FilterStringParam(expectedRecord.getKey3()));

        Record actualRecord = storage.findOne(country, filter, options);
//        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    @DisplayName("Find one record by profile key")
    @ParameterizedTest(name = "Find one record by profile key" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findOneByProfileKeyTest(boolean encryption) throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(expectedRecord.getProfileKey()));

        Record actualRecord = storage.findOne(country, filter, options);
//        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }


    @DisplayName("Find one record by range key")
    @ParameterizedTest(name = "Find one record by range key" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findOneByRangeKeyTest(boolean encryption) throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = writeRecord(encryption, country);
        FindFilter filter = new FindFilter();
        filter.setRangeKeyParam(new FilterRangeParam(expectedRecord.getRangeKey()));

        Record actualRecord = storage.findOne(country, filter, options);
//        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    private FindOneByKeysTest() throws FindOptions.FindOptionsException {
    }
}
*/
