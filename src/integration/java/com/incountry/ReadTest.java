package com.incountry;

import com.incountry.exceptions.RecordException;
import com.incountry.exceptions.StorageException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(Parameterized.class)
public class ReadTest extends BaseTest {

    Record expectedRecord;

    @After
    public void tearDown() {
        String key = expectedRecord.getKey();
        try {
            storage.delete(country, key);
        } catch (NullPointerException | StorageException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readFullRecordTest()
            throws GeneralSecurityException, StorageException, IOException, RecordException {

        expectedRecord = createFullRecord(country);
        writeRecord(encryption, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
        assertReflectionEquals("Record validation", expectedRecord, actualRecord);
    }

    @Test
    public void readReqRecordTest()
            throws GeneralSecurityException, StorageException, IOException, RecordException {

        expectedRecord = createSimpleRecord(country);
        writeRecord(encryption, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
        assertReflectionEquals("Record validation", expectedRecord, actualRecord);
    }

    @Test
    public void readNotExistingRecordTest()
            throws IOException, StorageException, GeneralSecurityException, RecordException {
        Storage storage = createStorage(encryption);

        expectedRecord = new Record();
        expectedRecord.setKey("NotExistingRecord123");

        Record record = storage.read(country, expectedRecord.getKey());
        Assert.assertNull(record);
    }
}
