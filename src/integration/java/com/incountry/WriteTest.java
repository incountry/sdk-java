package com.incountry;

import com.incountry.exceptions.StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.security.GeneralSecurityException;


@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class WriteTest extends BaseTest {

    @DisplayName("Write record with all field")
    @ParameterizedTest(name = "Write record with all field" + testName)
    @CsvSource({"false, US"})
    public void writeFullRecordTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Record record = createFullRecord(country);
        writeRecord(encryption, record);
    }

    @DisplayName("Write record with required field")
    @ParameterizedTest(name = "Write record with required field" + testName)
    @CsvSource({"false, US"})
    public void writeReqRecordTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Record record = createSimpleRecord(country);
        writeRecord(encryption, record);
    }

    @DisplayName("Write record with empty body")
    @ParameterizedTest(name = "Write record with empty body [{index}] => encryption={0}")
    @CsvSource({"false, US"})
    public void readRecordEmptyBodyTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Record record = createSimpleRecord(country, null);
        writeRecord(encryption, record);
    }
}
