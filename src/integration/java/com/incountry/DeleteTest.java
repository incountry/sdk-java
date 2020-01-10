package com.incountry;

import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RunWith(Parameterized.class)
public class DeleteTest extends BaseTest {

    @Test
    public void deleteRecordTest() throws GeneralSecurityException, StorageException, IOException {
        Record record = writeRecord(encryption, country);
        storage.delete(country, record.key);
    }


    @Test(expected = StorageServerException.class)
    public void deleteNotExistingRecordTest() throws IOException, StorageException {
        Storage storage = DeleteTest.this.createStorage(encryption);
        storage.delete(country, "NotExistingRecord123");
    }

}
