package com.incountry.residence.sdk;

import com.incountry.residence.sdk.crypto.testimpl.FernetCrypto;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StorageIntegrationTest {

    public static final String INTEGR_ENV_KEY_COUNTRY = "INT_INC_COUNTRY";
    public static final String INTEGR_ENV_KEY_ENDPOINT = "INT_INC_ENDPOINT";
    public static final String INT_INC_ENVIRONMENT_ID_OAUTH = "INT_INC_ENVIRONMENT_ID_OAUTH";
    public static final String INT_INC_CLIENT_ID = "INT_INC_CLIENT_ID";
    public static final String INT_INC_CLIENT_SECRET = "INT_INC_CLIENT_SECRET";
    public static final String INT_INC_DEFAULT_AUTH_ENDPOINT = "INT_INC_DEFAULT_AUTH_ENDPOINT";
    public static final String INT_INC_ENPOINT_MASK = "INT_INC_ENPOINT_MASK";
    public static final String INT_COUNTRIES_LIST_ENDPOINT = "INT_COUNTRIES_LIST_ENDPOINT";

    private static final Logger LOG = LogManager.getLogger(StorageIntegrationTest.class);

    private static final String TEMP = "-javasdk-" +
            new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
            "-" +
            UUID.randomUUID().toString().replace("-", "");

    private final Storage storage;
    private final Storage storageIgnoreCase;
    private final SecretKeyAccessor secretKeyAccessor;

    private static final String BATCH_WRITE_KEY = "BatchWriteKey" + TEMP;
    private static final String WRITE_KEY = "Write_Key" + TEMP;
    private static final String WRITE_KEY_IGNORE_CASE = WRITE_KEY + "_IgnorE_CasE";
    private static final String PROFILE_KEY = "ProfileKey" + TEMP;
    private static final String KEY_2 = "Key2" + TEMP;
    private static final String KEY_3 = "Key3" + TEMP;
    private static final Long BATCH_WRITE_RANGE_KEY = 2L;
    private static final Long WRITE_RANGE_KEY = 1L;
    private static final String RECORD_BODY = "test";
    private static final Integer HTTP_POOL_SIZE = 2;

    private static final String COUNTRY = loadFromEnv(INTEGR_ENV_KEY_COUNTRY);
    private static final String ENCRYPTION_SECRET = "123456789_123456789_1234567890Ab";
    private static final String DEFAULT_AUTH_ENDPOINT = loadFromEnv(INT_INC_DEFAULT_AUTH_ENDPOINT);
    private static final String CLIENT_ID = loadFromEnv(INT_INC_CLIENT_ID);
    private static final String SECRET = loadFromEnv(INT_INC_CLIENT_SECRET);
    private static final String ENDPOINT_MASK = loadFromEnv(INT_INC_ENPOINT_MASK);
    private static final String ENV_ID = loadFromEnv(INT_INC_ENVIRONMENT_ID_OAUTH);
    private static final String COUNTRIES_LIST_ENDPOINT = loadFromEnv(INT_COUNTRIES_LIST_ENDPOINT);

    private static final int VERSION = 0;

    public static String loadFromEnv(String key) {
        return System.getenv(key);
    }


    public StorageIntegrationTest() throws StorageServerException, StorageClientException {
        SecretKey secretKey = new SecretKey(ENCRYPTION_SECRET, VERSION, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, VERSION);
        secretKeyAccessor = () -> secretsData;
        storage = StorageImpl.getInstance(
                new StorageConfig()
                        .setClientId(CLIENT_ID)
                        .setClientSecret(SECRET)
                        .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                        .setEndpointMask(ENDPOINT_MASK)
                        .setEnvId(ENV_ID)
                        .setSecretKeyAccessor(secretKeyAccessor)
                        .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT)
        );

        StorageConfig config = new StorageConfig()
                .setClientId(CLIENT_ID)
                .setClientSecret(SECRET)
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setEnvId(ENV_ID)
                .setNormalizeKeys(true)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT);

        storageIgnoreCase = StorageImpl.getInstance(config);
    }

    @Test
    @Order(100)
    public void batchWriteTest() throws StorageException {
        List<Record> records = new ArrayList<>();
        records.add(new Record(BATCH_WRITE_KEY, RECORD_BODY, PROFILE_KEY, BATCH_WRITE_RANGE_KEY, KEY_2, KEY_3));
        storage.batchWrite(COUNTRY, records);
    }

    @Test
    @Order(200)
    public void writeTest() throws StorageException {
        Record record = new Record(WRITE_KEY, RECORD_BODY, PROFILE_KEY, WRITE_RANGE_KEY, KEY_2, KEY_3);
        storage.write(COUNTRY, record);
    }

    @Test
    @Order(300)
    public void readTest() throws StorageException {
        Record incomingRecord = storage.read(COUNTRY, WRITE_KEY);
        assertEquals(WRITE_KEY, incomingRecord.getKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
    }

    @Test
    @Order(301)
    public void readIgnoreCaseTest() throws StorageException {
        Record record = new Record(WRITE_KEY_IGNORE_CASE, RECORD_BODY, PROFILE_KEY, WRITE_RANGE_KEY, KEY_2, KEY_3);
        storageIgnoreCase.write(COUNTRY, record);

        Record incomingRecord = storageIgnoreCase.read(COUNTRY, WRITE_KEY_IGNORE_CASE.toLowerCase());
        assertEquals(WRITE_KEY_IGNORE_CASE, incomingRecord.getKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());

        incomingRecord = storageIgnoreCase.read(COUNTRY, WRITE_KEY_IGNORE_CASE.toUpperCase());
        assertEquals(WRITE_KEY_IGNORE_CASE, incomingRecord.getKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
    }

    @Test
    @Order(400)
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(WRITE_KEY)
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY)
                .rangeKeyEq(WRITE_RANGE_KEY);
        BatchRecord batchRecord = storage.find(COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(WRITE_KEY, batchRecord.getRecords().get(0).getKey());

        builder.clear()
                .keyEq(BATCH_WRITE_KEY)
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY)
                .rangeKeyEq(BATCH_WRITE_RANGE_KEY);
        batchRecord = storage.find(COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(BATCH_WRITE_KEY, batchRecord.getRecords().get(0).getKey());

        builder.clear()
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY);
        batchRecord = storage.find(COUNTRY, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        assertTrue(batchRecord.getRecords().stream().anyMatch(record
                -> record.getKey().equals(BATCH_WRITE_KEY)));
        assertTrue(batchRecord.getRecords().stream().anyMatch(record
                -> record.getKey().equals(WRITE_KEY)));

        builder.clear()
                .keyNotEq(WRITE_KEY)
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY);
        batchRecord = storage.find(COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(BATCH_WRITE_KEY, batchRecord.getRecords().get(0).getKey());
    }

    @Test
    @Order(401)
    public void findAdvancedTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(KEY_2)
                .rangeKeyEq(WRITE_RANGE_KEY, BATCH_WRITE_RANGE_KEY, WRITE_RANGE_KEY + BATCH_WRITE_RANGE_KEY + 1);
        BatchRecord batchRecord = storage.find(COUNTRY, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        List<String> resultIdList = new ArrayList<>();
        resultIdList.add(batchRecord.getRecords().get(0).getKey());
        resultIdList.add(batchRecord.getRecords().get(1).getKey());
        assertTrue(resultIdList.contains(WRITE_KEY));
        assertTrue(resultIdList.contains(BATCH_WRITE_KEY));
    }

    @Test
    @Order(402)
    public void findIgnoreCaseTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(WRITE_KEY_IGNORE_CASE)
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY)
                .rangeKeyEq(WRITE_RANGE_KEY);
        BatchRecord batchRecord = storageIgnoreCase.find(COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(WRITE_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getKey());

        builder = builder.clear()
                .keyEq(WRITE_KEY_IGNORE_CASE.toLowerCase())
                .key2Eq(KEY_2.toLowerCase())
                .key3Eq(KEY_3.toLowerCase())
                .profileKeyEq(PROFILE_KEY.toLowerCase())
                .rangeKeyEq(WRITE_RANGE_KEY);
        batchRecord = storageIgnoreCase.find(COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(WRITE_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getKey());

        builder = builder.clear()
                .keyEq(WRITE_KEY_IGNORE_CASE.toUpperCase())
                .key2Eq(KEY_2.toUpperCase())
                .key3Eq(KEY_3.toUpperCase())
                .profileKeyEq(PROFILE_KEY.toUpperCase())
                .rangeKeyEq(WRITE_RANGE_KEY);
        batchRecord = storageIgnoreCase.find(COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(WRITE_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getKey());
    }

    @Test
    @Order(500)
    public void findOneTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(KEY_2)
                .rangeKeyEq(WRITE_RANGE_KEY);
        Record record = storage.findOne(COUNTRY, builder);
        assertEquals(WRITE_KEY, record.getKey());
        assertEquals(RECORD_BODY, record.getBody());
    }

    @Test
    @Order(600)
    public void customEncryptionTest() throws StorageException {
        SecretKey customSecretKey = new SecretKey(ENCRYPTION_SECRET, VERSION + 1, false, true);
        List<SecretKey> secretKeyList = new ArrayList<>(secretKeyAccessor.getSecretsData().getSecrets());
        secretKeyList.add(customSecretKey);
        SecretsData anotherSecretsData = new SecretsData(secretKeyList, customSecretKey.getVersion());
        SecretKeyAccessor customAccessor = () -> anotherSecretsData;
        List<Crypto> cryptoList = new ArrayList<>();
        cryptoList.add(new FernetCrypto(true));

        StorageConfig config = new StorageConfig()
                .setClientId(CLIENT_ID)
                .setClientSecret(SECRET)
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setEnvId(ENV_ID)
                .setSecretKeyAccessor(customAccessor)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT)
                .setCustomEncryptionConfigsList(cryptoList);

        Storage storage2 = StorageImpl.getInstance(config);
        //write record with custom enc
        String customRecordKey = WRITE_KEY + "_custom";
        Record record = new Record(customRecordKey, RECORD_BODY, PROFILE_KEY, WRITE_RANGE_KEY, KEY_2, KEY_3);
        storage2.write(COUNTRY, record);
        //read record with custom enc
        Record record1 = storage2.read(COUNTRY, customRecordKey);
        assertEquals(record, record1);
        //read recorded record with default encryption
        Record record2 = storage2.read(COUNTRY, WRITE_KEY);
        assertEquals(RECORD_BODY, record2.getBody());
        //find record with custom enc
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(customRecordKey)
                .rangeKeyEq(WRITE_RANGE_KEY);
        Record record3 = storage2.findOne(COUNTRY, builder);
        assertEquals(record, record3);
        //delete record with custom enc
        storage2.delete(COUNTRY, customRecordKey);
        Record record4 = storage2.read(COUNTRY, customRecordKey);
        assertNull(record4);
    }

    @Test
    @Order(700)
    public void deleteTest() throws StorageException {
        storage.delete(COUNTRY, WRITE_KEY);
        storage.delete(COUNTRY, BATCH_WRITE_KEY);
        // Cannot read deleted record
        Record writeMethodRecord = storage.read(COUNTRY, WRITE_KEY);
        Record batchWriteMethodRecord = storage.read(COUNTRY, BATCH_WRITE_KEY);
        assertNull(writeMethodRecord);
        assertNull(batchWriteMethodRecord);
    }

    @Test
    @Order(701)
    public void deleteIgnoreCaseTest() throws StorageException {
        storageIgnoreCase.delete(COUNTRY, WRITE_KEY_IGNORE_CASE.toUpperCase());
        // Cannot read deleted record
        Record record = storageIgnoreCase.read(COUNTRY, WRITE_KEY_IGNORE_CASE);
        assertNull(record);
        record = storageIgnoreCase.read(COUNTRY, WRITE_KEY_IGNORE_CASE.toUpperCase());
        assertNull(record);
        record = storageIgnoreCase.read(COUNTRY, WRITE_KEY_IGNORE_CASE.toLowerCase());
        assertNull(record);
    }

    @Test
    @Order(800)
    public void connectionPoolTest() throws StorageException, InterruptedException, ExecutionException {
        SecretKey secretKey = new SecretKey(ENCRYPTION_SECRET, VERSION, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, VERSION);
        SecretKeyAccessor mySecretKeyAccessor = () -> secretsData;

        StorageConfig config = new StorageConfig()
                .setClientId(CLIENT_ID)
                .setClientSecret(SECRET)
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setEnvId(ENV_ID)
                .setSecretKeyAccessor(mySecretKeyAccessor)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT)
                .setMaxHttpPoolSize(HTTP_POOL_SIZE);

        Storage customStorage = StorageImpl.getInstance(config);
        //http pool size < concurrent threads < count of threads
        ExecutorService executorService = Executors.newFixedThreadPool(HTTP_POOL_SIZE * 2);
        List<Future<StorageException>> futureList = new ArrayList<>();
        for (int i = 0; i < HTTP_POOL_SIZE * 4; i++) {
            futureList.add(executorService.submit(createCallableTask(customStorage, i)));
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        for (Future<StorageException> one : futureList) {
            assertTrue(one.isDone());
            assertNull(one.get());
        }
    }

    private Callable<StorageException> createCallableTask(final Storage storage, final int numb) {
        return () -> {
            try {
                String randomKey = WRITE_KEY + UUID.randomUUID().toString();
                Thread currentThread = Thread.currentThread();
                currentThread.setName("connectionPoolTest #" + numb);
                Record record = new Record(randomKey, RECORD_BODY, PROFILE_KEY, WRITE_RANGE_KEY, KEY_2, KEY_3);
                storage.write(COUNTRY, record);
                Record incomingRecord = storage.read(COUNTRY, randomKey);
                assertEquals(randomKey, incomingRecord.getKey());
                assertEquals(RECORD_BODY, incomingRecord.getBody());
                assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
                assertEquals(KEY_2, incomingRecord.getKey2());
                assertEquals(KEY_3, incomingRecord.getKey3());
                storage.delete(COUNTRY, randomKey);
            } catch (StorageException exception) {
                LOG.error("Exception in connectionPoolTest", exception);
                return exception;
            }
            return null;
        };
    }
}
