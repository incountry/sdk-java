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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static final String INT_INC_COUNTRY = "INT_INC_COUNTRY";
    public static final String INT_INC_COUNTRY_2 = "INT_INC_COUNTRY_2";
    public static final String INT_INC_ENDPOINT = "INT_INC_ENDPOINT";
    private static final String INT_INC_ENVIRONMENT_ID = "INT_INC_ENVIRONMENT_ID";
    private static final String INT_INC_API_KEY = "INT_INC_API_KEY";
    public static final String INT_INC_ENVIRONMENT_ID_OAUTH = "INT_INC_ENVIRONMENT_ID_OAUTH";
    public static final String INT_INC_CLIENT_ID = "INT_INC_CLIENT_ID";
    public static final String INT_INC_CLIENT_SECRET = "INT_INC_CLIENT_SECRET";
    public static final String INT_INC_DEFAULT_AUTH_ENDPOINT = "INT_INC_DEFAULT_AUTH_ENDPOINT";
    public static final String INT_INC_EMEA_AUTH_ENDPOINT = "INT_INC_EMEA_AUTH_ENDPOINT";
    public static final String INT_INC_APAC_AUTH_ENDPOINT = "INT_INC_APAC_AUTH_ENDPOINT";
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

    private static final String EMEA = "emea";
    private static final String APAC = "apac";
    private static final String BATCH_WRITE_KEY_1 = "BatchWriteKey" + TEMP;
    private static final String WRITE_KEY_1 = "Write_Key" + TEMP;
    private static final String WRITE_KEY_IGNORE_CASE = WRITE_KEY_1 + "_IgnorE_CasE";
    private static final String PROFILE_KEY = "ProfileKey" + TEMP;
    private static final String KEY_2 = "Key2" + TEMP;
    private static final String KEY_3 = "Key3" + TEMP;
    private static final Long BATCH_WRITE_RANGE_KEY_1 = 2L;
    private static final Long WRITE_RANGE_KEY_1 = 1L;
    private static final String RECORD_BODY = "test";
    private static final Integer HTTP_POOL_SIZE = 4;

    private static final String MIDIPOP_COUNTRY = loadFromEnv(INT_INC_COUNTRY);
    private static final String MIDIPOP_COUNTRY_2 = loadFromEnv(INT_INC_COUNTRY_2);
    private static final String ENCRYPTION_SECRET = "123456789_123456789_1234567890Ab";
    private static final String DEFAULT_AUTH_ENDPOINT = loadFromEnv(INT_INC_DEFAULT_AUTH_ENDPOINT);
    private static final String EMEA_AUTH_ENDPOINT = loadFromEnv(INT_INC_EMEA_AUTH_ENDPOINT);
    private static final String APAC_AUTH_ENDPOINT = loadFromEnv(INT_INC_APAC_AUTH_ENDPOINT);
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
        storage = StorageImpl.getInstance(loadFromEnv(INT_INC_ENVIRONMENT_ID),
                loadFromEnv(INT_INC_API_KEY),
                loadFromEnv(INT_INC_ENDPOINT),
                secretKeyAccessor);

        StorageConfig config = new StorageConfig()
                .setEnvId(loadFromEnv(INT_INC_ENVIRONMENT_ID))
                .setApiKey(loadFromEnv(INT_INC_API_KEY))
                .setEndPoint(loadFromEnv(INT_INC_ENDPOINT))
                .setSecretKeyAccessor(secretKeyAccessor)
                .setNormalizeKeys(true);
        storageIgnoreCase = StorageImpl.getInstance(config);
    }

    @Test
    @Order(100)
    public void batchWriteTest() throws StorageException {
        List<Record> records = new ArrayList<>();
        Record record = new Record()
                .setRecordKey(BATCH_WRITE_KEY_1)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(BATCH_WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        records.add(record);
        storage.batchWrite(MIDIPOP_COUNTRY, records);
    }

    @Test
    @Order(200)
    public void writeTest() throws StorageException {
        Record record = new Record()
                .setRecordKey(WRITE_KEY_1)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storage.write(MIDIPOP_COUNTRY, record);
    }

    @Test
    @Order(300)
    public void readTest() throws StorageException {
        Record incomingRecord = storage.read(MIDIPOP_COUNTRY, WRITE_KEY_1);
        assertEquals(WRITE_KEY_1, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
    }

    @Test
    @Order(301)
    public void readIgnoreCaseTest() throws StorageException {
        Record record = new Record()
                .setRecordKey(WRITE_KEY_IGNORE_CASE)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storageIgnoreCase.write(MIDIPOP_COUNTRY, record);

        Record incomingRecord = storageIgnoreCase.read(MIDIPOP_COUNTRY, WRITE_KEY_IGNORE_CASE.toLowerCase());
        assertEquals(WRITE_KEY_IGNORE_CASE, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());

        incomingRecord = storageIgnoreCase.read(MIDIPOP_COUNTRY, WRITE_KEY_IGNORE_CASE.toUpperCase());
        assertEquals(WRITE_KEY_IGNORE_CASE, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
    }

    @Test
    @Order(400)
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .recordKeyEq(WRITE_KEY_1)
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY)
                .rangeKey1Eq(WRITE_RANGE_KEY_1);
        BatchRecord batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(WRITE_KEY_1, batchRecord.getRecords().get(0).getRecordKey());

        builder.clear()
                .recordKeyEq(BATCH_WRITE_KEY_1)
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY)
                .rangeKey1Eq(BATCH_WRITE_RANGE_KEY_1);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(BATCH_WRITE_KEY_1, batchRecord.getRecords().get(0).getRecordKey());

        builder.clear()
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        assertTrue(batchRecord.getRecords().stream().anyMatch(record
                -> record.getRecordKey().equals(BATCH_WRITE_KEY_1)));
        assertTrue(batchRecord.getRecords().stream().anyMatch(record
                -> record.getRecordKey().equals(WRITE_KEY_1)));

        builder.clear()
                .recordKeyNotEq(WRITE_KEY_1)
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(BATCH_WRITE_KEY_1, batchRecord.getRecords().get(0).getRecordKey());
    }

    @Test
    @Order(401)
    public void findAdvancedTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(KEY_2)
                .rangeKey1Eq(WRITE_RANGE_KEY_1, BATCH_WRITE_RANGE_KEY_1, WRITE_RANGE_KEY_1 + BATCH_WRITE_RANGE_KEY_1 + 1);
        BatchRecord batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        List<String> resultIdList = new ArrayList<>();
        resultIdList.add(batchRecord.getRecords().get(0).getRecordKey());
        resultIdList.add(batchRecord.getRecords().get(1).getRecordKey());
        assertTrue(resultIdList.contains(WRITE_KEY_1));
        assertTrue(resultIdList.contains(BATCH_WRITE_KEY_1));
    }

    @Test
    @Order(402)
    public void findIgnoreCaseTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .recordKeyEq(WRITE_KEY_IGNORE_CASE)
                .key2Eq(KEY_2)
                .key3Eq(KEY_3)
                .profileKeyEq(PROFILE_KEY)
                .rangeKey1Eq(WRITE_RANGE_KEY_1);
        BatchRecord batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(WRITE_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());

        builder = builder.clear()
                .recordKeyEq(WRITE_KEY_IGNORE_CASE.toLowerCase())
                .key2Eq(KEY_2.toLowerCase())
                .key3Eq(KEY_3.toLowerCase())
                .profileKeyEq(PROFILE_KEY.toLowerCase())
                .rangeKey1Eq(WRITE_RANGE_KEY_1);
        batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(WRITE_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());

        builder = builder.clear()
                .recordKeyEq(WRITE_KEY_IGNORE_CASE.toUpperCase())
                .key2Eq(KEY_2.toUpperCase())
                .key3Eq(KEY_3.toUpperCase())
                .profileKeyEq(PROFILE_KEY.toUpperCase())
                .rangeKey1Eq(WRITE_RANGE_KEY_1);
        batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(WRITE_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());
    }

    @Test
    @Order(500)
    public void findOneTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(KEY_2)
                .rangeKey1Eq(WRITE_RANGE_KEY_1);
        Record record = storage.findOne(MIDIPOP_COUNTRY, builder);
        assertEquals(WRITE_KEY_1, record.getRecordKey());
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
                .setEnvId(loadFromEnv(INT_INC_ENVIRONMENT_ID))
                .setApiKey(loadFromEnv(INT_INC_API_KEY))
                .setEndPoint(loadFromEnv(INT_INC_ENDPOINT))
                .setSecretKeyAccessor(customAccessor)
                .setCustomEncryptionConfigsList(cryptoList);

        Storage storage2 = StorageImpl.getInstance(config);
        //write record with custom enc
        String customRecordKey = WRITE_KEY_1 + "_custom";
        Record record = new Record()
                .setRecordKey(customRecordKey)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storage2.write(MIDIPOP_COUNTRY, record);
        //read record with custom enc
        Record record1 = storage2.read(MIDIPOP_COUNTRY, customRecordKey);
        assertEquals(record, record1);
        //read recorded record with default encryption
        Record record2 = storage2.read(MIDIPOP_COUNTRY, WRITE_KEY_1);
        assertEquals(RECORD_BODY, record2.getBody());
        //find record with custom enc
        FindFilterBuilder builder = FindFilterBuilder.create()
                .recordKeyEq(customRecordKey)
                .rangeKey1Eq(WRITE_RANGE_KEY_1);
        Record record3 = storage2.findOne(MIDIPOP_COUNTRY, builder);
        assertEquals(record, record3);
        //delete record with custom enc
        storage2.delete(MIDIPOP_COUNTRY, customRecordKey);
        Record record4 = storage2.read(MIDIPOP_COUNTRY, customRecordKey);
        assertNull(record4);
    }

    @Test
    @Order(700)
    public void deleteTest() throws StorageException {
        storage.delete(MIDIPOP_COUNTRY, WRITE_KEY_1);
        storage.delete(MIDIPOP_COUNTRY, BATCH_WRITE_KEY_1);
        // Cannot read deleted record
        Record writeMethodRecord = storage.read(MIDIPOP_COUNTRY, WRITE_KEY_1);
        Record batchWriteMethodRecord = storage.read(MIDIPOP_COUNTRY, BATCH_WRITE_KEY_1);
        assertNull(writeMethodRecord);
        assertNull(batchWriteMethodRecord);
    }

    @Test
    @Order(701)
    public void deleteIgnoreCaseTest() throws StorageException {
        storageIgnoreCase.delete(MIDIPOP_COUNTRY, WRITE_KEY_IGNORE_CASE.toUpperCase());
        // Cannot read deleted record
        Record record = storageIgnoreCase.read(MIDIPOP_COUNTRY, WRITE_KEY_IGNORE_CASE);
        assertNull(record);
        record = storageIgnoreCase.read(MIDIPOP_COUNTRY, WRITE_KEY_IGNORE_CASE.toUpperCase());
        assertNull(record);
        record = storageIgnoreCase.read(MIDIPOP_COUNTRY, WRITE_KEY_IGNORE_CASE.toLowerCase());
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

        Map<String, String> authMap = new HashMap<>();
        if (EMEA_AUTH_ENDPOINT != null && !EMEA_AUTH_ENDPOINT.isEmpty()) {
            authMap.put(EMEA, EMEA_AUTH_ENDPOINT);
        }
        if (APAC_AUTH_ENDPOINT != null && !APAC_AUTH_ENDPOINT.isEmpty()) {
            authMap.put(APAC, APAC_AUTH_ENDPOINT);
        }
        StorageConfig config = new StorageConfig()
                .setClientId(CLIENT_ID)
                .setClientSecret(SECRET)
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setEnvId(ENV_ID)
                .setSecretKeyAccessor(mySecretKeyAccessor)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT)
                .setMaxHttpPoolSize(HTTP_POOL_SIZE)
                .setMaxHttpConnectionsPerRoute(HTTP_POOL_SIZE / 2);
        if (!authMap.isEmpty()) {
            config.setAuthEndpoints(authMap);
        }
        Storage customStorage = StorageImpl.getInstance(config);
        //http pool size < concurrent threads < count of threads
        ExecutorService executorService = Executors.newFixedThreadPool(HTTP_POOL_SIZE / 2);
        List<Future<StorageException>> futureList = new ArrayList<>();
        Long startTime = System.currentTimeMillis();
        int taskCount = HTTP_POOL_SIZE * 2;
        for (int i = 0; i < taskCount; i++) {
            futureList.add(executorService.submit(createCallableTask(customStorage, i)));
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        int successfulTaskCount = 0;
        for (Future<StorageException> one : futureList) {
            assertTrue(one.isDone());
            if (one.get() == null) {
                successfulTaskCount += 1;
            }
        }
        Long finishTime = System.currentTimeMillis();
        LOG.debug("connectionPoolTest duration time = {} ms, average speed = {} ms per 1 task", finishTime - startTime, (finishTime - startTime) / taskCount);
        assertEquals(taskCount, successfulTaskCount);
    }

    private Callable<StorageException> createCallableTask(final Storage storage, final int numb) {
        return () -> {
            try {
                String randomKey = WRITE_KEY_1 + UUID.randomUUID().toString();
                Thread currentThread = Thread.currentThread();
                currentThread.setName("connectionPoolTest #" + numb);
                Record record = new Record()
                        .setRecordKey(randomKey)
                        .setBody(RECORD_BODY)
                        .setProfileKey(PROFILE_KEY)
                        .setRangeKey1(WRITE_RANGE_KEY_1)
                        .setKey2(KEY_2)
                        .setKey3(KEY_3);
                String country = (numb % 2 == 0 ? MIDIPOP_COUNTRY : MIDIPOP_COUNTRY_2);
                storage.write(country, record);
                Record incomingRecord = storage.read(country, randomKey);
                assertEquals(randomKey, incomingRecord.getRecordKey());
                assertEquals(RECORD_BODY, incomingRecord.getBody());
                assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
                assertEquals(KEY_2, incomingRecord.getKey2());
                assertEquals(KEY_3, incomingRecord.getKey3());
                storage.delete(country, randomKey);
            } catch (StorageException exception) {
                LOG.error("Exception in connectionPoolTest", exception);
                return exception;
            }
            return null;
        };
    }
}
