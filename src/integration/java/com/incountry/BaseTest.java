package com.incountry;

import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.key_accessor.SecretKeyAccessor;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;


public abstract class BaseTest {

    protected Storage storage;

    @Parameterized.Parameter(0)
    public boolean encryption;
    @Parameterized.Parameter(1)
    public String country;

    @Parameterized.Parameters(name = "{index}:enc={0},{1}")
    public static Collection<Object[]> data() {
        String country = "se";
        Object[][] data = new Object[][]{
                {false, country},
                {true, country}
        };
        return Arrays.asList(data);
    }

    protected Storage createStorage(boolean encryption) throws IOException, StorageServerException {

        SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor("mySecretKey");

        return new Storage(
                "env_id",
                "api_key",
                "https://se.qa.incountry.io",
                encryption,
                secretKeyAccessor
        );
    }

    protected Record createFullRecord(String country) {
        return createFullRecord(country, "{\"FirstName\":\"MyFirstName\"}");
    }

    protected Record createFullRecord(String country, String body) {
        return new Record(country,
                "key_" + randomInt(),
                body,
                "profileKey_" + randomInt(),
                randomInt(),
                "key2_" + randomInt(),
                "key3_" + randomInt()
        );
    }

    protected int randomInt() {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(1001234) + 1;
    }

    protected Record createSimpleRecord(String country) {
        return createSimpleRecord(country, "{\"FirstName\":\"MyFirstName\"}");
    }

    protected Record createSimpleRecord(String country, String body) {
        return new Record(country,
                "key_" + randomInt(),
                body
        );
    }

    protected Record writeRecord(boolean encryption, String country)
            throws IOException, StorageException, GeneralSecurityException {
        storage = createStorage(encryption);
        Record record = createFullRecord(country);

        storage.write(record);
        return record;
    }

    protected void writeRecord(boolean encryption, Record record)
            throws IOException, StorageException, GeneralSecurityException {
        storage = createStorage(encryption);
        storage.write(record);
    }

    protected void validateRecord(Record expectedRecord) throws GeneralSecurityException, StorageException, IOException {
        Record actualRecord = storage.read(country, expectedRecord.getKey());
        assertReflectionEquals("Record validation", expectedRecord, actualRecord);
    }
}
