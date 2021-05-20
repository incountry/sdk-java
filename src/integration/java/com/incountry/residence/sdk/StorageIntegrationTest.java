package com.incountry.residence.sdk;

import com.incountry.residence.sdk.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.crypto.EncryptionSecret;
import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.crypto.testimpl.FernetCipher;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.DateField;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.SortField;
import com.incountry.residence.sdk.dto.search.SortOrder;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.crypto.SecretKeyAccessor;
import com.incountry.residence.sdk.crypto.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.incountry.residence.sdk.CredentialsHelper.loadFromEnv;
import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY1;
import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY2;
import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY3;
import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY4;
import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY5;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StorageIntegrationTest {
    public static final String INT_INC_EMEA_AUTH_ENDPOINT = "INT_INC_EMEA_AUTH_ENDPOINT";
    public static final String INT_INC_APAC_AUTH_ENDPOINT = "INT_INC_APAC_AUTH_ENDPOINT";
    private static final Logger LOG = LogManager.getLogger(StorageIntegrationTest.class);
    private static final String COUNTRY = CredentialsHelper.getMidPopCountry(true);
    private static final String TEMP = "-javasdk-" +
            new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
            "-" +
            UUID.randomUUID().toString().replace("-", "");

    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static StorageConfig ignoreCaseConfig;
    private static StorageConfig withoutEncryptionConfig;
    private static StorageConfig ordinaryConfig;
    private static StorageConfig nonHashingConfig;
    private static StorageConfig customCipherConfig;

    private static final String EMEA = "emea";
    private static final String APAC = "apac";

    private static final String RECORD_KEY = "RecordKey_" + TEMP;
    private static final String PARENT_KEY = "ParentKey_" + TEMP;
    private static final String BATCH_RECORD_KEY = "Batch" + RECORD_KEY;
    private static final String ATTACHMENT_RECORD_KEY = "AttachmentRecordKey_" + TEMP;
    private static final String RECORD_KEY_IGNORE_CASE = "_IgnorE_CasE_" + RECORD_KEY;
    private static final String PROFILE_KEY = "ProfileKey" + TEMP;
    private static final String KEY_1 = "Key1_" + TEMP;
    private static final String KEY_2 = "Key2_" + TEMP;
    private static final String KEY_3 = "Key3_" + TEMP;
    private static final String KEY_4 = "Key4_" + TEMP;
    private static final String KEY_5 = "Key5_" + TEMP;
    private static final String KEY_6 = "Key6_" + TEMP;
    private static final String KEY_7 = "Key7_" + TEMP;
    private static final String KEY_8 = "Key8_" + TEMP;
    private static final String KEY_9 = "Key9_" + TEMP;
    private static final String KEY_10 = "Key10_" + TEMP;
    private static final String KEY_11 = "Key11_" + TEMP;
    private static final String KEY_12 = "Key12_" + TEMP;
    private static final String KEY_13 = "Key13_" + TEMP;
    private static final String KEY_14 = "Key14_" + TEMP;
    private static final String KEY_15 = "Key15_" + TEMP;
    private static final String KEY_16 = "Key16_" + TEMP;
    private static final String KEY_17 = "Key17_" + TEMP;
    private static final String KEY_18 = "Key18_" + TEMP;
    private static final String KEY_19 = "Key19_" + TEMP;
    private static final String KEY_20 = "Key20_" + TEMP;
    private static final String SERVICE_KEY_1 = "ServiceKey1" + TEMP;
    private static final String SERVICE_KEY_2 = "ServiceKey2" + TEMP;
    private static final String SERVICE_KEY_3 = "ServiceKey3_" + TEMP;
    private static final String SERVICE_KEY_4 = "ServiceKey4_" + TEMP;
    private static final String SERVICE_KEY_5 = "ServiceKey5_" + TEMP;
    private static final String PRECOMMIT_BODY = "PreсommitBody" + TEMP;
    private static final Date EXPIRES_AT = new Date(System.currentTimeMillis() + 300_000);
    private static final Long BATCH_WRITE_RANGE_KEY_1 = 2L;
    private static final Long WRITE_RANGE_KEY_1 = 1L;
    private static final Long RANGE_KEY_2 = 2L;
    private static final Long RANGE_KEY_3 = 3L;
    private static final Long RANGE_KEY_4 = 4L;
    private static final Long RANGE_KEY_5 = 5L;
    private static final Long RANGE_KEY_6 = 6L;
    private static final Long RANGE_KEY_7 = 7L;
    private static final Long RANGE_KEY_8 = 8L;
    private static final Long RANGE_KEY_9 = 9L;
    private static final String RECORD_BODY = "test";

    private static final byte[] ENCRYPTION_SECRET = "123456789_123456789_1234567890Ab".getBytes(StandardCharsets.UTF_8);

    private static final String EMEA_AUTH_ENDPOINT = loadFromEnv(INT_INC_EMEA_AUTH_ENDPOINT);
    private static final String APAC_AUTH_ENDPOINT = loadFromEnv(INT_INC_APAC_AUTH_ENDPOINT);

    private static final int VERSION = 0;
    private static final String FILE_CONTENT = UUID.randomUUID().toString();
    private static final String DEFAULT_MIME_TYPE = "multipart/form-data";
    private static final String NEW_FILE_NAME = UUID.randomUUID() + ".txt";
    private static final String MIME_TYPE = "text/plain";
    private static final String FILE_NAME = UUID.randomUUID() + ".txt";

    @BeforeAll
    public static void initConfig() throws StorageException {
        Secret secret = new EncryptionSecret(VERSION, ENCRYPTION_SECRET);
        List<Secret> secretList = new ArrayList<>();
        secretList.add(secret);
        SecretsData secretsData = new SecretsData(secretList, secret);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;

        StorageConfig config = CredentialsHelper.getConfigWithOauth()
                .setSecretKeyAccessor(secretKeyAccessor);
        ordinaryConfig = config;

        withoutEncryptionConfig = config
                .copy()
                .setSecretKeyAccessor(null);

        nonHashingConfig = config
                .copy()
                .setSecretKeyAccessor(secretKeyAccessor)
                .setHashSearchKeys(false);

        ignoreCaseConfig = config
                .copy()
                .setHashSearchKeys(true)
                .setNormalizeKeys(true);

        Secret customSecretKey = new CustomEncryptionKey(ENCRYPTION_SECRET, VERSION);
        List<Secret> secretList2 = new ArrayList<>();
        secretList2.add(customSecretKey);
        SecretsData anotherSecretsData = new SecretsData(secretList2, customSecretKey);
        SecretKeyAccessor anotherAccessor = () -> anotherSecretsData;

        customCipherConfig = config
                .copy()
                .setNormalizeKeys(false)
                .setSecretKeyAccessor(anotherAccessor)
                .setCryptoProvider(new CryptoProvider(new FernetCipher("Fernet")));
    }

    private static Stream<Arguments> storageProvider() {
        return Stream.of(
                generateArguments(nonHashingConfig),
                generateArguments(ordinaryConfig),
                generateArguments(withoutEncryptionConfig),
                generateArguments(ignoreCaseConfig),
                generateArguments(customCipherConfig)
        );
    }

    private static Arguments generateArguments(StorageConfig config) {
        int hash = config.hashCode();
        return Arguments.of(config, RECORD_KEY + hash, BATCH_RECORD_KEY + hash, KEY_2 + hash);
    }

    @Test
    @Order(1)
    void testAverage() throws StorageCryptoException, StorageClientException, StorageServerException, IOException {
        Storage storage = StorageImpl.getInstance(ordinaryConfig);
        for (int i = 0; i < 100; i++) {
            LOG.debug("iteration: {}", i);
            Record newRecord = new Record(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            storage.write(COUNTRY, newRecord);
            storage.read(COUNTRY, newRecord.getRecordKey());
            storage.delete(COUNTRY, newRecord.getRecordKey());
        }
        storage.close();
    }

    @ParameterizedTest(name = "commonTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(100)
    public void commonTest(StorageConfig config, String recordKey, String batchRecordKey, String key2) throws StorageException, InterruptedException, IOException {
        Storage storage = StorageImpl.getInstance(config);

        batchWriteTest(storage, batchRecordKey, key2);
        writeTest(storage, recordKey, key2);
        readTest(storage, recordKey, key2);
        findTest(storage, recordKey, batchRecordKey, key2);
        findAdvancedTest(storage, recordKey, batchRecordKey, key2);
        findByVersionTest(storage, key2);
        findByAllFieldsTest(storage, recordKey, key2);
        findOneTest(storage, recordKey, key2);
        deleteTest(storage, recordKey, batchRecordKey);
        expiredRecordTest(storage);
        healthCheckTest(storage);
        String fileId = addAttachmentTest(storage);
        getAttachmentFileTest(storage, fileId);
        getAttachmentMetaTest(storage, fileId);
        updateAttachmentMetaTest(storage, fileId);
        deleteAttachmentTest(storage, fileId);
        addAttachmentMultipleFilesTest(storage);
        deleteOneOfAttachmentMultipleFilesTest(storage);
        getAttachmentFileFromNonExistentRecordTest(storage, fileId);
        getNonExistentAttachmentFileTest(storage);
        getAttachmentMetaFromNonExistentFileTest(storage);
        updateAttachmentMetaForNonExistentFileTest(storage);
        addAttachmentWithUnusualFileNameTest(storage);
        addBinaryFilesTest(storage);
        deleteRecordWithAttachment(storage);

        storage.close();
    }

    public void batchWriteTest(Storage storage, String batchRecordKey, String key2) throws StorageException {
        List<Record> records = new ArrayList<>();
        Record myRecord = new Record(batchRecordKey)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(BATCH_WRITE_RANGE_KEY_1)
                .setKey2(key2)
                .setKey3(KEY_3)
                .setRangeKey10(RANDOM.nextLong());

        records.add(myRecord);
        List<Record> recordedList = storage.batchWrite(COUNTRY, records);
        assertEquals(1, recordedList.size());
        Record recordedRecord = recordedList.get(0);
        assertEquals(batchRecordKey, recordedRecord.getRecordKey());
        assertEquals(RECORD_BODY, recordedRecord.getBody());
        assertEquals(PROFILE_KEY, recordedRecord.getProfileKey());
        assertEquals(BATCH_WRITE_RANGE_KEY_1, recordedRecord.getRangeKey1());
        assertEquals(key2, recordedRecord.getKey2());
        assertEquals(myRecord.getRangeKey10(), recordedRecord.getRangeKey10());
    }

    public void writeTest(Storage storage, String recordKey, String key2) throws StorageException {
        Record newRecord = new Record(recordKey)
                .setBody(RECORD_BODY).setProfileKey(PROFILE_KEY).setRangeKey1(WRITE_RANGE_KEY_1)
                .setRangeKey2(RANGE_KEY_2).setRangeKey3(RANGE_KEY_3).setRangeKey4(RANGE_KEY_4)
                .setRangeKey5(RANGE_KEY_5).setRangeKey6(RANGE_KEY_6).setRangeKey7(RANGE_KEY_7)
                .setRangeKey8(RANGE_KEY_8).setRangeKey9(RANGE_KEY_9).setRangeKey10(RANDOM.nextLong())
                .setKey1(KEY_1).setKey2(key2).setKey3(KEY_3)
                .setKey4(KEY_4).setKey5(KEY_5).setKey6(KEY_6)
                .setKey7(KEY_7).setKey8(KEY_8).setKey9(KEY_9)
                .setKey10(KEY_10).setKey11(KEY_11).setKey12(KEY_12)
                .setKey13(KEY_13).setKey14(KEY_14).setKey15(KEY_15)
                .setKey16(KEY_16).setKey17(KEY_17).setKey18(KEY_18)
                .setKey19(KEY_19).setKey20(KEY_20).setParentKey(PARENT_KEY)
                .setPrecommitBody(PRECOMMIT_BODY)
                .setServiceKey1(SERVICE_KEY_1)
                .setServiceKey2(SERVICE_KEY_2)
                .setServiceKey3(SERVICE_KEY_3)
                .setServiceKey4(SERVICE_KEY_4)
                .setServiceKey5(SERVICE_KEY_5)
                .setExpiresAt(EXPIRES_AT);
        Record recordedRecord = storage.write(COUNTRY, newRecord);
        checkAllFields(recordedRecord, recordKey, key2);
    }


    void expiredRecordTest(Storage storage) throws StorageException, InterruptedException {
        Record newRecord = new Record(UUID.randomUUID().toString())
                .setBody(UUID.randomUUID().toString())
                //record will be expired in 5 seconds
                .setExpiresAt(new Date(System.currentTimeMillis() + 5_000L));
        Record recordedRecord = storage.write(COUNTRY, newRecord);
        assertEquals(recordedRecord.getRecordKey(), recordedRecord.getRecordKey());
        assertEquals(recordedRecord.getBody(), recordedRecord.getBody());

        Record readRecord = storage.read(COUNTRY, newRecord.getRecordKey());
        assertEquals(recordedRecord.getRecordKey(), readRecord.getRecordKey());
        assertEquals(recordedRecord.getBody(), readRecord.getBody());
        //wait 5 seconds
        Thread.sleep(5_000L);
        readRecord = storage.read(COUNTRY, newRecord.getRecordKey());
        assertNull(readRecord);
    }

    public void readTest(Storage storage, String recordKey, String key2) throws StorageException {
        Record incomingRecord = storage.read(COUNTRY, recordKey);
        checkAllFields(incomingRecord, recordKey, key2);
    }

    private void checkAllFields(Record incomingRecord, String recordKey, String key2) {
        assertEquals(recordKey, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_1, incomingRecord.getKey1());
        assertEquals(key2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
        assertEquals(KEY_4, incomingRecord.getKey4());
        assertEquals(KEY_5, incomingRecord.getKey5());
        assertEquals(KEY_6, incomingRecord.getKey6());
        assertEquals(KEY_7, incomingRecord.getKey7());
        assertEquals(KEY_8, incomingRecord.getKey8());
        assertEquals(KEY_9, incomingRecord.getKey9());
        assertEquals(KEY_10, incomingRecord.getKey10());
        assertEquals(KEY_11, incomingRecord.getKey11());
        assertEquals(KEY_12, incomingRecord.getKey12());
        assertEquals(KEY_13, incomingRecord.getKey13());
        assertEquals(KEY_14, incomingRecord.getKey14());
        assertEquals(KEY_15, incomingRecord.getKey15());
        assertEquals(KEY_16, incomingRecord.getKey16());
        assertEquals(KEY_17, incomingRecord.getKey17());
        assertEquals(KEY_18, incomingRecord.getKey18());
        assertEquals(KEY_19, incomingRecord.getKey19());
        assertEquals(KEY_20, incomingRecord.getKey20());
        assertEquals(PARENT_KEY, incomingRecord.getParentKey());
        assertEquals(PRECOMMIT_BODY, incomingRecord.getPrecommitBody());
        assertEquals(SERVICE_KEY_1, incomingRecord.getServiceKey1());
        assertEquals(SERVICE_KEY_2, incomingRecord.getServiceKey2());
        assertEquals(SERVICE_KEY_3, incomingRecord.getServiceKey3());
        assertEquals(SERVICE_KEY_4, incomingRecord.getServiceKey4());
        assertEquals(SERVICE_KEY_5, incomingRecord.getServiceKey5());
        assertEquals(WRITE_RANGE_KEY_1, incomingRecord.getRangeKey1());
        assertEquals(RANGE_KEY_2, incomingRecord.getRangeKey2());
        assertEquals(RANGE_KEY_3, incomingRecord.getRangeKey3());
        assertEquals(RANGE_KEY_4, incomingRecord.getRangeKey4());
        assertEquals(RANGE_KEY_5, incomingRecord.getRangeKey5());
        assertEquals(RANGE_KEY_6, incomingRecord.getRangeKey6());
        assertEquals(RANGE_KEY_7, incomingRecord.getRangeKey7());
        assertEquals(RANGE_KEY_8, incomingRecord.getRangeKey8());
        assertEquals(RANGE_KEY_9, incomingRecord.getRangeKey9());
        assertNotNull(incomingRecord.getRangeKey10());
        assertEquals(EXPIRES_AT, incomingRecord.getExpiresAt());
    }

    public void findTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        FindFilter filter = new FindFilter()
                .keyEq(StringField.RECORD_KEY, recordKey)
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.KEY20, KEY_20)
                .keyEq(StringField.PARENT_KEY, PARENT_KEY)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1)
                .keyEq(DateField.EXPIRES_AT, EXPIRES_AT);
        FindResult findResult = storage.find(COUNTRY, filter);
        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        assertEquals(recordKey, findResult.getRecords().get(0).getRecordKey());
        assertNotNull(findResult.getRecords().get(0).getCreatedAt());
        assertNotNull(findResult.getRecords().get(0).getUpdatedAt());
        assertEquals(EXPIRES_AT, findResult.getRecords().get(0).getExpiresAt());

        filter.clear()
                .keyEq(StringField.RECORD_KEY, batchRecordKey)
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, BATCH_WRITE_RANGE_KEY_1);
        findResult = storage.find(COUNTRY, filter);
        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        assertEquals(batchRecordKey, findResult.getRecords().get(0).getRecordKey());

        filter.clear()
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        findResult = storage.find(COUNTRY, filter);
        assertEquals(2, findResult.getCount());
        assertEquals(2, findResult.getRecords().size());
        assertTrue(findResult.getRecords().stream().anyMatch(currentRecord
                -> currentRecord.getRecordKey().equals(batchRecordKey)));
        assertTrue(findResult.getRecords().stream().anyMatch(currentRecord
                -> currentRecord.getRecordKey().equals(recordKey)));

        filter.clear()
                .keyNotEq(StringField.RECORD_KEY, recordKey)
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        findResult = storage.find(COUNTRY, filter);
        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        assertEquals(batchRecordKey, findResult.getRecords().get(0).getRecordKey());
    }

    public void findAdvancedTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        FindFilter filter = new FindFilter()
                .keyEq(StringField.KEY2, key2)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1, BATCH_WRITE_RANGE_KEY_1, WRITE_RANGE_KEY_1 + BATCH_WRITE_RANGE_KEY_1 + 1);

        FindResult findResult = storage.find(COUNTRY, filter.copy().sortBy(SortField.RANGE_KEY10, SortOrder.ASC));
        assertEquals(2, findResult.getCount());
        assertEquals(2, findResult.getRecords().size());
        Long record1Value = findResult.getRecords().get(0).getRangeKey10();
        Long record2Value = findResult.getRecords().get(1).getRangeKey10();
        assertTrue(record1Value <= record2Value);


        findResult = storage.find(COUNTRY, filter.copy().sortBy(SortField.RANGE_KEY10, SortOrder.DESC));
        assertEquals(2, findResult.getCount());
        assertEquals(2, findResult.getRecords().size());
        record1Value = findResult.getRecords().get(0).getRangeKey10();
        record2Value = findResult.getRecords().get(1).getRangeKey10();
        assertTrue(record1Value >= record2Value);

        findResult = storage.find(COUNTRY, filter.copy().sortBy(SortField.CREATED_AT, SortOrder.ASC));
        assertEquals(2, findResult.getCount());
        assertEquals(2, findResult.getRecords().size());
        Date record1date = findResult.getRecords().get(0).getCreatedAt();
        Date record2date = findResult.getRecords().get(1).getCreatedAt();
        assertTrue(record1date.before(record2date) || record1date.equals(record2date));

        findResult = storage.find(COUNTRY, filter.copy().sortBy(SortField.CREATED_AT, SortOrder.DESC));
        assertEquals(2, findResult.getCount());
        assertEquals(2, findResult.getRecords().size());
        record1date = findResult.getRecords().get(0).getCreatedAt();
        record2date = findResult.getRecords().get(1).getCreatedAt();
        assertTrue(record1date.after(record2date) || record1date.equals(record2date));

        filter = new FindFilter().keyEq(StringField.KEY2, key2);
        findResult = storage.find(COUNTRY, filter
                .copy()
                .keyIsNotNull(StringField.KEY20)
                .keyIsNotNull(DateField.EXPIRES_AT));
        assertEquals(1, findResult.getCount());
        assertEquals(recordKey, findResult.getRecords().get(0).getRecordKey());
        assertNotNull(findResult.getRecords().get(0).getExpiresAt());

        findResult = storage.find(COUNTRY, filter
                .copy()
                .keyIsNull(StringField.KEY20)
                .keyIsNull(DateField.EXPIRES_AT));
        assertEquals(1, findResult.getCount());
        assertEquals(batchRecordKey, findResult.getRecords().get(0).getRecordKey());
        assertNull(findResult.getRecords().get(0).getExpiresAt());
    }

    public void findByVersionTest(Storage storage, String key2) throws StorageException {
        FindFilter filter = new FindFilter()
                .keyEq(StringField.KEY2, key2)
                .keyEq(NumberField.VERSION, Long.valueOf(VERSION));
        FindResult findResult1 = storage.find(COUNTRY, filter);
        assertEquals(2, findResult1.getCount());
        assertEquals(2, findResult1.getRecords().size());

        filter.keyEq(NumberField.VERSION, 10L + VERSION);
        FindResult findResult2 = storage.find(COUNTRY, filter);
        assertEquals(0, findResult2.getCount());
        assertEquals(0, findResult2.getRecords().size());

        filter.keyNotEq(NumberField.VERSION, Long.valueOf(VERSION));
        FindResult findResult3 = storage.find(COUNTRY, filter);
        assertEquals(0, findResult3.getCount());
        assertEquals(0, findResult3.getRecords().size());

        filter.keyNotEq(NumberField.VERSION, 10L + VERSION);
        FindResult findResult4 = storage.find(COUNTRY, filter);
        assertEquals(2, findResult4.getCount());
        assertEquals(2, findResult4.getRecords().size());
    }

    public void findByAllFieldsTest(Storage storage, String recordKey, String key2) throws StorageException {
        FindFilter filter = new FindFilter()
                .keyEq(StringField.RECORD_KEY, recordKey)
                .keyEq(StringField.KEY1, KEY_1)
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.KEY4, KEY_4)
                .keyEq(StringField.KEY5, KEY_5)
                .keyEq(StringField.KEY6, KEY_6)
                .keyEq(StringField.KEY7, KEY_7)
                .keyEq(StringField.KEY8, KEY_8)
                .keyEq(StringField.KEY9, KEY_9)
                .keyEq(StringField.KEY10, KEY_10)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1)
                .keyGreater(NumberField.RANGE_KEY2, RANGE_KEY_2, true)
                .keyEq(NumberField.RANGE_KEY3, RANGE_KEY_3)
                .keyEq(NumberField.RANGE_KEY4, RANGE_KEY_4)
                .keyEq(NumberField.RANGE_KEY5, RANGE_KEY_5)
                .keyEq(NumberField.RANGE_KEY6, RANGE_KEY_6)
                .keyEq(NumberField.RANGE_KEY7, RANGE_KEY_7)
                .keyEq(NumberField.RANGE_KEY8, RANGE_KEY_8)
                .keyEq(NumberField.RANGE_KEY9, RANGE_KEY_9)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(SERVICE_KEY1, SERVICE_KEY_1)
                .keyEq(SERVICE_KEY2, SERVICE_KEY_2)
                .keyEq(SERVICE_KEY3, SERVICE_KEY_3)
                .keyEq(SERVICE_KEY4, SERVICE_KEY_4)
                .keyEq(SERVICE_KEY5, SERVICE_KEY_5);

        FindResult findResult = storage.find(COUNTRY, filter);
        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        Record resultRecord = findResult.getRecords().get(0);
        assertEquals(recordKey, resultRecord.getRecordKey());
        assertEquals(KEY_1, resultRecord.getKey1());
        assertEquals(key2, resultRecord.getKey2());
        assertEquals(KEY_3, resultRecord.getKey3());
        assertEquals(KEY_4, resultRecord.getKey4());
        assertEquals(KEY_5, resultRecord.getKey5());
        assertEquals(KEY_6, resultRecord.getKey6());
        assertEquals(KEY_7, resultRecord.getKey7());
        assertEquals(KEY_8, resultRecord.getKey8());
        assertEquals(KEY_9, resultRecord.getKey9());
        assertEquals(KEY_10, resultRecord.getKey10());
        assertEquals(WRITE_RANGE_KEY_1, resultRecord.getRangeKey1());
        assertEquals(RANGE_KEY_2, resultRecord.getRangeKey2());
        assertEquals(RANGE_KEY_3, resultRecord.getRangeKey3());
        assertEquals(RANGE_KEY_4, resultRecord.getRangeKey4());
        assertEquals(RANGE_KEY_5, resultRecord.getRangeKey5());
        assertEquals(RANGE_KEY_6, resultRecord.getRangeKey6());
        assertEquals(RANGE_KEY_7, resultRecord.getRangeKey7());
        assertEquals(RANGE_KEY_8, resultRecord.getRangeKey8());
        assertEquals(RANGE_KEY_9, resultRecord.getRangeKey9());
        assertEquals(PROFILE_KEY, resultRecord.getProfileKey());
        assertEquals(SERVICE_KEY_1, resultRecord.getServiceKey1());
        assertEquals(SERVICE_KEY_2, resultRecord.getServiceKey2());
        assertEquals(SERVICE_KEY_3, resultRecord.getServiceKey3());
        assertEquals(SERVICE_KEY_4, resultRecord.getServiceKey4());
        assertEquals(SERVICE_KEY_5, resultRecord.getServiceKey5());
    }

    public void findOneTest(Storage storage, String recordKey, String key2) throws StorageException {
        FindFilter filter = new FindFilter()
                .keyEq(StringField.KEY2, key2)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        Record resultRecord = storage.findOne(COUNTRY, filter);
        assertEquals(recordKey, resultRecord.getRecordKey());
        assertEquals(RECORD_BODY, resultRecord.getBody());
    }

    public void deleteTest(Storage storage, String recordKey, String batchRecordKey) throws StorageException {
        storage.delete(COUNTRY, recordKey);
        storage.delete(COUNTRY, batchRecordKey);
        // Cannot read deleted record
        Record writeMethodRecord = storage.read(COUNTRY, recordKey);
        Record batchWriteMethodRecord = storage.read(COUNTRY, batchRecordKey);
        assertNull(writeMethodRecord);
        assertNull(batchWriteMethodRecord);
    }

    void healthCheckTest(Storage storage) throws StorageServerException, StorageClientException {
        assertTrue(storage.healthCheck(COUNTRY));
        assertTrue(storage.healthCheck(CredentialsHelper.getMiniPopCountry()));
    }

    @Test
    @Order(200)
    void ignoreCaseTest() throws StorageException, IOException {
        Storage storage = StorageImpl.getInstance(ignoreCaseConfig);
        readIgnoreCaseTest(storage);
        findIgnoreCaseTest(storage);
        deleteIgnoreCaseTest(storage);
        storage.close();
    }


    void readIgnoreCaseTest(Storage storage) throws StorageException {
        Record newRecord = new Record(RECORD_KEY_IGNORE_CASE)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storage.write(COUNTRY, newRecord);

        Record incomingRecord = storage.read(COUNTRY, RECORD_KEY_IGNORE_CASE.toLowerCase());
        assertEquals(RECORD_KEY_IGNORE_CASE, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());

        incomingRecord = storage.read(COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        assertEquals(RECORD_KEY_IGNORE_CASE, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
    }

    void findIgnoreCaseTest(Storage storage) throws StorageException {
        FindFilter filter = new FindFilter()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE)
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        FindResult findResult = storage.find(COUNTRY, filter);
        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, findResult.getRecords().get(0).getRecordKey());

        filter = filter.clear()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE.toLowerCase())
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY.toLowerCase())
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        findResult = storage.find(COUNTRY, filter);
        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, findResult.getRecords().get(0).getRecordKey());

        filter = filter.clear()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE.toUpperCase())
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY.toUpperCase())
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        findResult = storage.find(COUNTRY, filter);
        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, findResult.getRecords().get(0).getRecordKey());
    }


    void deleteIgnoreCaseTest(Storage storage) throws StorageException {
        storage.delete(COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        // Cannot read deleted record
        Record readRecord = storage.read(COUNTRY, RECORD_KEY_IGNORE_CASE);
        assertNull(readRecord);
        readRecord = storage.read(COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        assertNull(readRecord);
        readRecord = storage.read(COUNTRY, RECORD_KEY_IGNORE_CASE.toLowerCase());
        assertNull(readRecord);
    }

    String addAttachmentTest(Storage storage) throws StorageException, IOException {
        Record newRecord = new Record(ATTACHMENT_RECORD_KEY)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1);
        storage.write(COUNTRY, newRecord);
        Path tempFile = Files.createTempFile(FILE_NAME.split("\\.")[0], FILE_NAME.split("\\.")[1]);
        Files.write(tempFile, FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        InputStream fileInputStream = Files.newInputStream(tempFile);
        AttachmentMeta attachmentMeta = storage.addAttachment(COUNTRY, ATTACHMENT_RECORD_KEY, fileInputStream, FILE_NAME, false, DEFAULT_MIME_TYPE);
        String fileId = attachmentMeta.getFileId();
        assertEquals(FILE_NAME, attachmentMeta.getFilename());
        Files.delete(tempFile);
        return fileId;
    }

    void getAttachmentFileTest(Storage storage, String fileId) throws StorageException, IOException {
        AttachedFile file = storage.getAttachmentFile(COUNTRY, ATTACHMENT_RECORD_KEY, fileId);
        String incomingFileContent = IOUtils.toString(file.getFileContent(), StandardCharsets.UTF_8.name());
        assertEquals(FILE_CONTENT, incomingFileContent);
        assertEquals(FILE_NAME, file.getFileName());
    }

    void getAttachmentMetaTest(Storage storage, String fileId) throws StorageException {
        AttachmentMeta meta = storage.getAttachmentMeta(COUNTRY, ATTACHMENT_RECORD_KEY, fileId);
        assertEquals(fileId, meta.getFileId());
        assertEquals(FILE_NAME, meta.getFilename());
        assertTrue(meta.getMimeType().contains(DEFAULT_MIME_TYPE));
    }

    void updateAttachmentMetaTest(Storage storage, String fileId) throws StorageException {
        AttachmentMeta meta = storage.updateAttachmentMeta(COUNTRY, ATTACHMENT_RECORD_KEY, fileId, NEW_FILE_NAME, MIME_TYPE);
        assertEquals(fileId, meta.getFileId());
        assertEquals(NEW_FILE_NAME, meta.getFilename());
        assertEquals(MIME_TYPE, meta.getMimeType());
    }

    void deleteAttachmentTest(Storage storage, String fileId) throws StorageException {
        storage.deleteAttachment(COUNTRY, ATTACHMENT_RECORD_KEY, fileId);
        AttachedFile file = storage.getAttachmentFile(COUNTRY, ATTACHMENT_RECORD_KEY, fileId);
        assertNull(file);
    }

    void addAttachmentMultipleFilesTest(Storage storage) throws StorageException {
        Map<String, String> attachmentFiles = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            String fileName = UUID.randomUUID().toString();
            String fileContent = UUID.randomUUID().toString();
            InputStream fileInputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
            AttachmentMeta attachmentMeta = storage.addAttachment(COUNTRY, ATTACHMENT_RECORD_KEY, fileInputStream, fileName, false);
            attachmentFiles.put(attachmentMeta.getFileId(), fileContent);
        }
        attachmentFiles.forEach((idFile, fileContent) -> {
            Exception ex = null;
            try {
                AttachedFile file = storage.getAttachmentFile(COUNTRY, ATTACHMENT_RECORD_KEY, idFile);
                String incomingFileContent = IOUtils.toString(file.getFileContent(), StandardCharsets.UTF_8.name());
                assertEquals(fileContent, incomingFileContent);
            } catch (StorageException | IOException exception) {
                LOG.error("Exception during attached files reading", exception);
                ex = exception;
            }
            assertNull(ex);
        });
    }

    void deleteOneOfAttachmentMultipleFilesTest(Storage storage) throws StorageException {
        Record recordWithFiles = storage.read(COUNTRY, ATTACHMENT_RECORD_KEY);
        storage.deleteAttachment(COUNTRY, ATTACHMENT_RECORD_KEY, recordWithFiles.getAttachments().get(0).getFileId());
        Record incomingRecord = storage.read(COUNTRY, ATTACHMENT_RECORD_KEY);
        assertEquals(2, incomingRecord.getAttachments().size());
    }

    void getAttachmentFileFromNonExistentRecordTest(Storage storage, String fileId) throws StorageException {
        AttachedFile file = storage.getAttachmentFile(COUNTRY, UUID.randomUUID().toString(), fileId);
        assertNull(file);
    }

    void getNonExistentAttachmentFileTest(Storage storage) throws StorageException {
        AttachedFile file = storage.getAttachmentFile(COUNTRY, ATTACHMENT_RECORD_KEY, UUID.randomUUID().toString());
        assertNull(file);
    }

    void getAttachmentMetaFromNonExistentFileTest(Storage storage) throws StorageException {
        AttachmentMeta meta = storage.getAttachmentMeta(COUNTRY, ATTACHMENT_RECORD_KEY, UUID.randomUUID().toString());
        assertNull(meta);
    }

    void updateAttachmentMetaForNonExistentFileTest(Storage storage) {
        String nonExistentFileId = UUID.randomUUID().toString();
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storage.updateAttachmentMeta(COUNTRY, ATTACHMENT_RECORD_KEY, nonExistentFileId, NEW_FILE_NAME, MIME_TYPE));
        assertTrue(ex.getMessage().contains("Code=404"));
        assertTrue(ex.getMessage().contains(nonExistentFileId));
    }

    void addAttachmentWithUnusualFileNameTest(Storage storage) throws StorageException, IOException {
        String fileName = "Naïve file.txt";
        Path tempFile = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        Files.write(tempFile, FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        InputStream fileInputStream = Files.newInputStream(tempFile);
        AttachmentMeta attachmentMeta = storage.addAttachment(COUNTRY, ATTACHMENT_RECORD_KEY, fileInputStream, fileName, false, DEFAULT_MIME_TYPE);
        assertEquals(fileName, attachmentMeta.getFilename());
        Files.delete(tempFile);
    }

    void addBinaryFilesTest(Storage storage) throws StorageException, IOException {
        String filePath = "./gradle/wrapper/gradle-wrapper.jar";
        String fileName = "gradle-wrapper.jar";
        try (InputStream inputStream = new FileInputStream(filePath)) {
            AttachmentMeta meta = storage.addAttachment(COUNTRY, ATTACHMENT_RECORD_KEY, inputStream, fileName, false);
            assertNotNull(meta);
            assertNotNull(meta.getFilename());
            assertNotNull(meta.getFileId());
            AttachedFile file = storage.getAttachmentFile(COUNTRY, ATTACHMENT_RECORD_KEY, meta.getFileId());
            assertArrayEquals(IOUtils.toByteArray(new FileInputStream(filePath)), IOUtils.toByteArray(file.getFileContent()));
            assertTrue(storage.deleteAttachment(COUNTRY, ATTACHMENT_RECORD_KEY, meta.getFileId()));
        }
    }

    void deleteRecordWithAttachment(Storage storage) throws StorageException {
        assertTrue(storage.delete(COUNTRY, ATTACHMENT_RECORD_KEY));
    }

    @Test
    @Order(300)
    public void findWithSearchKeys() throws StorageException, IOException {
        Storage storage = StorageImpl.getInstance(nonHashingConfig);
        String recordKey = "Non hashing " + RECORD_KEY;
        Record newRecord = new Record(recordKey)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setRangeKey2(RANGE_KEY_2)
                .setRangeKey3(RANGE_KEY_3)
                .setRangeKey4(RANGE_KEY_4)
                .setRangeKey5(RANGE_KEY_5)
                .setRangeKey6(RANGE_KEY_6)
                .setRangeKey7(RANGE_KEY_7)
                .setRangeKey8(RANGE_KEY_8)
                .setRangeKey9(RANGE_KEY_9)
                .setRangeKey10(RANDOM.nextLong())
                .setKey1(KEY_1)
                .setPrecommitBody(PRECOMMIT_BODY)
                .setServiceKey1(SERVICE_KEY_1)
                .setServiceKey2(SERVICE_KEY_2);
        String country = CredentialsHelper.getMidPopCountry(false);
        storage.write(country, newRecord);

        FindFilter filter = new FindFilter()
                .searchKeysLike(KEY_1.split("-")[2]);
        FindResult findResult = storage.find(country, filter);

        assertEquals(1, findResult.getCount());
        assertEquals(recordKey, findResult.getRecords().get(0).getRecordKey());
        assertEquals(RECORD_BODY, findResult.getRecords().get(0).getBody());

        storage.delete(country, recordKey);
        storage.close();
    }

    @Test
    @Order(400)
    public void utf8EncodingTest() throws StorageException, IOException {
        Storage storage = StorageImpl.getInstance(nonHashingConfig);
        String recordKey = "utf8" + RECORD_KEY;
        String key1 = "Louis César de La Baume Le Blanc" + TEMP;
        Record newRecord = new Record(recordKey)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey1(key1)
                .setPrecommitBody(PRECOMMIT_BODY);
        storage.write(COUNTRY, newRecord);

        FindFilter filter = new FindFilter()
                .keyEq(StringField.KEY1, key1);
        FindResult findResult = storage.find(COUNTRY, filter);

        assertEquals(1, findResult.getCount());
        assertEquals(recordKey, findResult.getRecords().get(0).getRecordKey());
        assertEquals(RECORD_BODY, findResult.getRecords().get(0).getBody());
        assertEquals(key1, findResult.getRecords().get(0).getKey1());

        storage.delete(COUNTRY, recordKey);
        storage.close();
    }

    @Test
    @Order(1100)
    public void connectionPoolTest() throws StorageException, InterruptedException, ExecutionException, IOException {
        Secret secret = new EncryptionSecret(VERSION, ENCRYPTION_SECRET);
        List<Secret> secretList = new ArrayList<>();
        secretList.add(secret);
        SecretsData secretsData = new SecretsData(secretList, secret);
        SecretKeyAccessor mySecretKeyAccessor = () -> secretsData;

        Map<String, String> authMap = new HashMap<>();
        if (EMEA_AUTH_ENDPOINT != null && !EMEA_AUTH_ENDPOINT.isEmpty()) {
            authMap.put(EMEA, EMEA_AUTH_ENDPOINT);
        }
        if (APAC_AUTH_ENDPOINT != null && !APAC_AUTH_ENDPOINT.isEmpty()) {
            authMap.put(APAC, APAC_AUTH_ENDPOINT);
        }
        StorageConfig config = CredentialsHelper.getConfigWithOauth()
                .setSecretKeyAccessor(mySecretKeyAccessor);
        if (!authMap.isEmpty()) {
            config.setAuthEndpoints(authMap);
        }
        Storage customStorage = StorageImpl.getInstance(config);
        //http pool size < concurrent threads < count of threads
        ExecutorService executorService = Executors.newFixedThreadPool(config.getMaxHttpPoolSize() / 2);
        List<Future<StorageException>> futureList = new ArrayList<>();
        Long startTime = System.currentTimeMillis();
        int taskCount = config.getMaxHttpPoolSize() * 2;
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
        customStorage.close();
    }

    private Callable<StorageException> createCallableTask(final Storage storage, final int numb) {
        return () -> {
            try {
                String randomKey = "RecordKey" + TEMP + UUID.randomUUID();
                Thread currentThread = Thread.currentThread();
                currentThread.setName("connectionPoolTest #" + numb);
                Record newRecord = new Record(randomKey)
                        .setBody(RECORD_BODY)
                        .setProfileKey(PROFILE_KEY)
                        .setRangeKey1(WRITE_RANGE_KEY_1)
                        .setKey2(KEY_2)
                        .setKey3(KEY_3);
                String country = (numb % 2 == 0 ? COUNTRY : CredentialsHelper.getMidPopCountry(false));
                storage.write(country, newRecord);
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
