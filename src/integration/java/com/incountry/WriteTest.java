package com.incountry;

import com.incountry.exceptions.StorageException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RunWith(Parameterized.class)
public class WriteTest extends BaseTest {

    @Test
    public void writeFullRecordTest() throws GeneralSecurityException, StorageException, IOException {
        Record record = createFullRecord(country);
        writeRecord(encryption, record);

        validateRecord(record);
    }

    @Test
    public void writeReqRecordTest() throws GeneralSecurityException, StorageException, IOException {
        Record record = createSimpleRecord(country);
        writeRecord(encryption, record);
        validateRecord(record);
    }
}
