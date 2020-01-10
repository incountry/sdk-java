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
public class DeleteTest extends BaseTest {

    private String country = "US";

    @DisplayName("Delete record")
    @ParameterizedTest(name = "Delete record" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void deleteRecordTest(boolean encryption) throws GeneralSecurityException, StorageException, IOException {
        Record record = writeRecord(encryption, country);
        storage.delete(country, record.key);
    }

    */
/*@DisplayName("Try to delete not existing record")
    @ParameterizedTest(name = "Try to delete not existing record" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void deleteNotExistingRecordTest(final boolean encryption) {
        Assertions.assertThrows(StorageServerException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                Storage storage = DeleteTest.this.createStorage(encryption);
                storage.delete("US", "NotExistingRecord123");
            }
        });
    }*//*

}
*/
