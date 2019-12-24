package com.incountry;

import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.key_accessor.SecretKeyAccessor;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public abstract class BaseTest {

    protected Storage storage;

    @NotNull
    protected Executable[] validateRecord(final Record expectedRecord, final Record actualRecord) {
        return new Executable[]{
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(expectedRecord.key, actualRecord.key, "Validate key value");
                    }
                },
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(expectedRecord.body, actualRecord.body, "Validate body value");
                    }
                },
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(expectedRecord.key2, actualRecord.key2, "Validate key2");
                    }
                },
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(expectedRecord.key3, actualRecord.key3, "Validate key3");
                    }
                },
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(expectedRecord.profileKey, actualRecord.profileKey, "Validate profile_key");
                    }
                },
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(expectedRecord.rangeKey, actualRecord.rangeKey, "Validate range_key");
                    }
                },
//                () -> assertEquals(expectedRecord.country, actualRecord.country, "Validate country value")
        };
    }

    @NotNull
    protected Executable[] validateBatchFields(final FindOptions findOptions, final int expectedCount, final BatchRecord actualBatch) {
        return new Executable[]{
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(expectedCount, actualBatch.getTotal(), "Validate total count");
                    }
                },
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(expectedCount, actualBatch.getCount(), "Validate count");
                    }
                },
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(findOptions.getLimit(), actualBatch.getLimit(), "Validate limit");
                    }
                },
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        assertEquals(findOptions.getOffset(), actualBatch.getOffset(), "Validate offset");
                    }
                },
        };
    }

    protected static final String testName = " [{index}] ==> encryption={0}, country={1}";
    protected static final String testNameEnc = " [{index}] ==> encryption={0}";

    protected Storage createStorage(boolean encryption) throws IOException, StorageServerException {

        Dotenv dotenv = Dotenv.load();

        SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor(dotenv.get("INC_SECRET_KEY"));

        return new Storage(
                dotenv.get("INC_ENVIRONMENT_ID"),
                dotenv.get("INC_API_KEY"),
                dotenv.get("INC_URL"),
                encryption,
                secretKeyAccessor
        );
    }

    protected Record createFullRecord(String country) {
        return createFullRecord(country, "{\"FirstName\":\"MyFirstName\"}");
    }

    protected Record createFullRecord(String country, String body) {
        return new Record(country,
                "key_" + RandomStringUtils.randomAlphabetic(5),
                body,
                "profileKey_" + RandomStringUtils.randomAlphabetic(5),
                RandomUtils.nextInt(1, 99),
                "key2_" + RandomStringUtils.randomAlphabetic(5),
                "key3_" + RandomStringUtils.randomAlphabetic(5)
        );
    }

    protected Record createSimpleRecord(String country) {
        return createSimpleRecord(country, "{\"FirstName\":\"MyFirstName\"}");
    }

    protected Record createSimpleRecord(String country, String body) {
        return new Record(country,
                "key_" + RandomStringUtils.randomAlphabetic(5),
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
}
