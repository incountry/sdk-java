package com.incountry;

import com.incountry.exceptions.RecordException;
import com.incountry.exceptions.StorageException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RunWith(Parameterized.class)
public class WriteTest extends BaseTest {

    Record record;

    @After
    public void tearDown() throws Exception {
        storage.delete(country, record.getKey());
    }

    @Test
    public void writeFullRecordTest() throws GeneralSecurityException, StorageException, IOException, RecordException {
        record = createFullRecord(country);
        writeRecord(encryption, record);
        validateRecord(record);
    }

    @Test
    public void writeReqRecordTest() throws GeneralSecurityException, StorageException, IOException, RecordException {
        record = createSimpleRecord(country);
        writeRecord(encryption, record);
        validateRecord(record);
    }
}
