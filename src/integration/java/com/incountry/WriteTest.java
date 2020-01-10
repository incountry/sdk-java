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
public class WriteTest extends BaseTest {

    private String country = "US";

    @DisplayName("Write record with all field")
    @ParameterizedTest(name = "Write record with all field" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void writeFullRecordTest(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException {

        Record record = createFullRecord(country);
        writeRecord(encryption, record);
    }

    @DisplayName("Write record with required field")
    @ParameterizedTest(name = "Write record with required field" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void writeReqRecordTest(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException {

        Record record = createSimpleRecord(country);
        writeRecord(encryption, record);
    }
}
*/
