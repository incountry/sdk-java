package com.incountry;

import com.incountry.exceptions.StorageException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(Parameterized.class)
public class ReadTest extends BaseTest {

    @Test
    public void readFullRecordTest()
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = createFullRecord(country);
        writeRecord(encryption, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
        assertReflectionEquals("Record validation", expectedRecord, actualRecord);
    }

    @Test
    public void readReqRecordTest()
            throws GeneralSecurityException, StorageException, IOException {

        Record expectedRecord = createSimpleRecord(country);
        writeRecord(encryption, expectedRecord);

        Record actualRecord = storage.read(country, expectedRecord.getKey());
        assertReflectionEquals("Record validation", expectedRecord, actualRecord);
    }

    @Test
    public void readNotExistingRecordTest()
            throws IOException, StorageException, GeneralSecurityException {
        Storage storage = createStorage(encryption);
        Record record = storage.read(country, "NotExistingRecord123");
        Assert.assertNull(record);
    }
}
