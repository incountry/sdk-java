package com.incountry;

import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.key_accessor.SecretKeyAccessor;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Random;


public abstract class BaseTest {

    protected Storage storage;
    protected static final String testNameEnc = " [{index}] [enc={0}]";

    protected Storage createStorage(boolean encryption) throws IOException, StorageServerException {

        SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor("mySecretKey");

        return new Storage(
                "env-id",
                "api-key",
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
        return randomGenerator.nextInt(500) + 1;
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

    public void findRecords(String country, Record expectedRecord, FindFilter filter, FindOptions findOptions)
            throws StorageException, IOException, GeneralSecurityException {
        BatchRecord batch = storage.find(country, filter, findOptions);
        Record[] records = batch.getRecords();

//        Assertions.assertAll("Validate batch fields",
//                validateBatchFields(findOptions, records.length, batch));

//        Assertions.assertAll("Validate record",
//                validateRecord(expectedRecord, records[0]));
    }
}
