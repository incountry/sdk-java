package com.incountry;

import com.incountry.exceptions.StorageException;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ReadTest extends BaseTest {

    private String country = "US";

//    @DisplayName("Read record with all field")
//    @ParameterizedTest(name = "Read record with all field" + testNameEnc)
//    @ValueSource(booleans = {false, true})
    @Test
    public void readFullRecordTest()
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = createFullRecord(country);
        writeRecord(false, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
        System.out.println(actualRecord.body);
//        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    /*@DisplayName("Read record with required fields")
    @ParameterizedTest(name = "Read record with required fields" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void readReqRecordTest(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = createSimpleRecord(country);
        writeRecord(encryption, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
//        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }*/

    /*@DisplayName("Try to read not existing record")
    @ParameterizedTest(name = "Try to read not existing record" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void readNotExistingRecordTest(boolean encryption)
            throws IOException, StorageException, GeneralSecurityException {
        Storage storage = createStorage(encryption);
        Record record = storage.read("US", "NotExistingRecord123");
        Assertions.assertNull(record);
    }*/
}
