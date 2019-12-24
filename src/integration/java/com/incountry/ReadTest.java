package com.incountry;

import com.incountry.exceptions.StorageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.security.GeneralSecurityException;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class ReadTest extends BaseTest {

    @DisplayName("Read record with all field")
    @ParameterizedTest(name = "Read record with all field" + testName)
    @CsvSource({"false, US"})
    public void readFullRecordTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = createFullRecord(country);
        writeRecord(encryption, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    @DisplayName("Read record with required fields")
    @ParameterizedTest(name = "Read record with required fields" + testName)
    @CsvSource({"false, US"})
    public void readReqRecordTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = createSimpleRecord(country);
        writeRecord(encryption, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    @DisplayName("Read record with empty body")
    @ParameterizedTest(name = "Read record with empty body" + testName)
    @CsvSource({"false, US"})
    public void readRecordEmptyBodyTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = createSimpleRecord(country, null);
        writeRecord(encryption, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
        Assertions.assertAll(validateRecord(expectedRecord, actualRecord));
    }

    @DisplayName("Try to read not existing record")
    @ParameterizedTest(name = "Try to read not existing record" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void readNotExistingRecordTest(boolean encryption)
            throws IOException, StorageException, GeneralSecurityException {
        Storage storage = createStorage(encryption);
        Record record = storage.read("US", "NotExistingRecord123");
        Assertions.assertNull(record);
    }
}
