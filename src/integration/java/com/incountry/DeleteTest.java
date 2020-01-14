package com.incountry;

import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

@RunWith(Parameterized.class)
public class DeleteTest extends BaseTest {

    private Record record;

    @Before
    public void setUp() throws Exception {
        record = writeRecord(encryption, country);
    }

    @After
    public void tearDown() {
        try {
            storage.delete(country, record.getKey());
        } catch (StorageException | IOException e) {
            System.out.println("Try to delete record after test");
        }
    }

    @Test
    public void deleteRecordTest() throws StorageException, IOException {
        System.out.println(record.getKey());
        storage.delete(country, record.getKey());
    }

    @Test(expected = StorageServerException.class)
    public void deleteNotExistingRecordTest() throws IOException, StorageException {
        Storage storage = DeleteTest.this.createStorage(encryption);
        storage.delete(country, "NotExistingRecord123");
    }

}
