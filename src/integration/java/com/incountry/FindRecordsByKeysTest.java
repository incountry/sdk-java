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
public class FindRecordsByKeysTest extends BaseTest {

    private String country = "US";
    
    @DisplayName("Find records by key")
    @ParameterizedTest(name = "Find records by key" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findByKeyTest(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKeyParam(new FilterStringParam(expectedRecord.getKey()));

        findRecords(country, expectedRecord, filter, new FindOptions());
    }

    @DisplayName("Find records by key2")
    @ParameterizedTest(name = "Find records by key2" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findByKey2Test(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey2Param(new FilterStringParam(expectedRecord.getKey2()));

        findRecords(country, expectedRecord, filter, new FindOptions());
    }

    @DisplayName("Find records by key3")
    @ParameterizedTest(name = "Find records by key3" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findByKey3Test(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setKey3Param(new FilterStringParam(expectedRecord.getKey3()));

        findRecords(country, expectedRecord, filter, new FindOptions());
    }

    @DisplayName("Find records by profile key")
    @ParameterizedTest(name = "Find records by profile key" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void findByProfileKeyTest(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record expectedRecord = writeRecord(encryption, country);

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(expectedRecord.getProfileKey()));

        findRecords(country, expectedRecord, filter, new FindOptions());
    }
}
*/
