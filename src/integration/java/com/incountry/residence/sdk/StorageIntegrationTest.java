package com.incountry.residence.sdk;

import com.incountry.residence.sdk.crypto.testimpl.FernetCrypto;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY1;
import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
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
    public static final String INT_INC_HTTP_POOL_SIZE = "INT_INC_HTTP_POOL_SIZE";
    public static final String INT_INC_EMEA_AUTH_ENDPOINT = "INT_INC_EMEA_AUTH_ENDPOINT";
    public static final String INT_INC_APAC_AUTH_ENDPOINT = "INT_INC_APAC_AUTH_ENDPOINT";
    public static final String INT_INC_ENDPOINT_MASK = "INT_INC_ENDPOINT_MASK";
    public static final String INT_COUNTRIES_LIST_ENDPOINT = "INT_COUNTRIES_LIST_ENDPOINT";

    private static final Logger LOG = LogManager.getLogger(StorageIntegrationTest.class);

    public static final String TEMP = "-javasdk-" +
            new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
            "-" +
            UUID.randomUUID().toString().replace("-", "");

    private Storage storageIgnoreCase;
    private Storage storageWithApiKey;
    private Storage storageWithoutEncryption;
    private Storage storageOrdinary;
    private Storage storageNonHashing;
    private Storage storageWithCustomCipher;

    private static final String EMEA = "emea";
    private static final String APAC = "apac";

    private static final String RECORD_KEY = "Record Key" + TEMP;
    private static final String BATCH_RECORD_KEY = "Batch " + RECORD_KEY;
    private static final String ATTACHMENT_RECORD_KEY = "Attachment Record Key" + TEMP;
    private static final String RECORD_KEY_IGNORE_CASE = "_IgnorE_CasE_" + RECORD_KEY;
    private static final String PROFILE_KEY = "ProfileKey" + TEMP;
    private static final String KEY_1 = "Key1" + TEMP;
    private static final String KEY_2 = "Key2" + TEMP;

    private static final String KEY_3 = "Key3" + TEMP;
    private static final String KEY_4 = "Key4" + TEMP;
    private static final String KEY_5 = "Key5" + TEMP;
    private static final String KEY_6 = "Key6" + TEMP;
    private static final String KEY_7 = "Key7" + TEMP;
    private static final String KEY_8 = "Key8" + TEMP;
    private static final String KEY_9 = "Key9" + TEMP;
    private static final String KEY_10 = "Key10" + TEMP;
    private static final String SERVICE_KEY_1 = "ServiceKey1" + TEMP;
    private static final String SERVICE_KEY_2 = "ServiceKey2" + TEMP;
    private static final String PRECOMMIT_BODY = "PreсommitBody" + TEMP;
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
    private static final Long RANGE_KEY_10 = 10L;
    private static final String RECORD_BODY = "test";
    private static final Integer HTTP_POOL_SIZE = Integer.valueOf(loadFromEnv(INT_INC_HTTP_POOL_SIZE, "4"));

    private static final String MIDIPOP_COUNTRY = loadFromEnv(INT_INC_COUNTRY);
    private static final String MIDIPOP_COUNTRY_2 = loadFromEnv(INT_INC_COUNTRY_2);
    private static final byte[] ENCRYPTION_SECRET = "123456789_123456789_1234567890Ab".getBytes(StandardCharsets.UTF_8);
    private static final String DEFAULT_AUTH_ENDPOINT = loadFromEnv(INT_INC_DEFAULT_AUTH_ENDPOINT);
    private static final String EMEA_AUTH_ENDPOINT = loadFromEnv(INT_INC_EMEA_AUTH_ENDPOINT);
    private static final String APAC_AUTH_ENDPOINT = loadFromEnv(INT_INC_APAC_AUTH_ENDPOINT);
    private static final String CLIENT_ID = loadFromEnv(INT_INC_CLIENT_ID);
    private static final String SECRET = loadFromEnv(INT_INC_CLIENT_SECRET);
    private static final String ENDPOINT_MASK = loadFromEnv(INT_INC_ENDPOINT_MASK);
    private static final String ENV_ID = loadFromEnv(INT_INC_ENVIRONMENT_ID_OAUTH);
    private static final String COUNTRIES_LIST_ENDPOINT = loadFromEnv(INT_COUNTRIES_LIST_ENDPOINT);

    private static final int VERSION = 0;
    private static final String FILE_CONTENT = UUID.randomUUID().toString();
    private static final String DEFAULT_MIME_TYPE = "multipart/form-data";
    private static final String NEW_FILE_NAME = UUID.randomUUID().toString() + ".txt";
    private static final String MIME_TYPE = "text/plain";
    private static final String FILE_NAME = UUID.randomUUID().toString() + ".txt";
    private String fileId;
    private static Map<String, String> attachmentFiles = new HashMap<>();

    public static String loadFromEnv(String key) {
        return System.getenv(key);
    }

    public static String loadFromEnv(String key, String defaultValue) {
        String value = loadFromEnv(key);
        return value == null ? defaultValue : value;
    }

    @BeforeAll
    public void initializeStorages() throws StorageException {
        SecretKey secretKey = new SecretKey(ENCRYPTION_SECRET, VERSION, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, VERSION);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        StorageConfig config = new StorageConfig()
                .setEnvId(loadFromEnv(INT_INC_ENVIRONMENT_ID))
                .setApiKey(loadFromEnv(INT_INC_API_KEY))
                .setEndPoint(loadFromEnv(INT_INC_ENDPOINT))
                .setSecretKeyAccessor(secretKeyAccessor);
        storageWithApiKey = StorageImpl.getInstance(config);

        config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setClientId(CLIENT_ID)
                .setClientSecret(SECRET)
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor);
        storageOrdinary = StorageImpl.getInstance(config);

        config = config
                .copy()
                .setSecretKeyAccessor(null);
        storageWithoutEncryption = StorageImpl.getInstance(config);

        config = config
                .copy()
                .setSecretKeyAccessor(secretKeyAccessor)
                .setHashSearchKeys(false);
        storageNonHashing = StorageImpl.getInstance(config);

        config = config
                .copy()
                .setHashSearchKeys(true)
                .setNormalizeKeys(true);
        storageIgnoreCase = StorageImpl.getInstance(config);

        SecretKey customSecretKey = new SecretKey(ENCRYPTION_SECRET, VERSION, false, true);
        List<SecretKey> secretKeyList2 = new ArrayList<>();
        secretKeyList2.add(customSecretKey);
        SecretsData anotherSecretsData = new SecretsData(secretKeyList2, customSecretKey.getVersion());
        SecretKeyAccessor anotherAccessor = () -> anotherSecretsData;
        List<Crypto> cryptoList = new ArrayList<>();
        cryptoList.add(new FernetCrypto(true));

        config = config
                .copy()
                .setNormalizeKeys(false)
                .setSecretKeyAccessor(anotherAccessor)
                .setCustomEncryptionConfigsList(cryptoList);
        storageWithCustomCipher = StorageImpl.getInstance(config);
    }

    private Stream<Arguments> storageProvider() {
        return Stream.of(
                generateArguments(storageNonHashing),
                generateArguments(storageWithApiKey),
                generateArguments(storageOrdinary),
                generateArguments(storageWithoutEncryption),
                generateArguments(storageIgnoreCase),
                generateArguments(storageWithCustomCipher)
        );
    }

    private Arguments generateArguments(Storage storage) {
        int hash = storage.hashCode();
        return Arguments.of(storage, RECORD_KEY + hash, BATCH_RECORD_KEY + hash, KEY_2 + hash);
    }

    @ParameterizedTest(name = "batchWriteTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(100)
    public void batchWriteTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        List<Record> records = new ArrayList<>();
        Record record = new Record(batchRecordKey)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(BATCH_WRITE_RANGE_KEY_1)
                .setKey2(key2)
                .setKey3(KEY_3);
        records.add(record);
        storage.batchWrite(MIDIPOP_COUNTRY, records);
    }

    @ParameterizedTest(name = "writeTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(200)
    public void writeTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        Record record = new Record(recordKey)
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
                .setRangeKey10(RANGE_KEY_10)
                .setKey1(KEY_1)
                .setKey2(key2)
                .setKey3(KEY_3)
                .setKey4(KEY_4)
                .setKey5(KEY_5)
                .setKey6(KEY_6)
                .setKey7(KEY_7)
                .setKey8(KEY_8)
                .setKey9(KEY_9)
                .setKey10(KEY_10)
                .setPrecommitBody(PRECOMMIT_BODY)
                .setServiceKey1(SERVICE_KEY_1)
                .setServiceKey2(SERVICE_KEY_2);
        storage.write(MIDIPOP_COUNTRY, record);
    }

    @ParameterizedTest(name = "readTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(300)
    public void readTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        Record incomingRecord = storage.read(MIDIPOP_COUNTRY, recordKey);
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
        assertEquals(PRECOMMIT_BODY, incomingRecord.getPrecommitBody());
        assertEquals(SERVICE_KEY_1, incomingRecord.getServiceKey1());
        assertEquals(SERVICE_KEY_2, incomingRecord.getServiceKey2());
        assertEquals(WRITE_RANGE_KEY_1, incomingRecord.getRangeKey1());
        assertEquals(RANGE_KEY_2, incomingRecord.getRangeKey2());
        assertEquals(RANGE_KEY_3, incomingRecord.getRangeKey3());
        assertEquals(RANGE_KEY_4, incomingRecord.getRangeKey4());
        assertEquals(RANGE_KEY_5, incomingRecord.getRangeKey5());
        assertEquals(RANGE_KEY_6, incomingRecord.getRangeKey6());
        assertEquals(RANGE_KEY_7, incomingRecord.getRangeKey7());
        assertEquals(RANGE_KEY_8, incomingRecord.getRangeKey8());
        assertEquals(RANGE_KEY_9, incomingRecord.getRangeKey9());
        assertEquals(RANGE_KEY_10, incomingRecord.getRangeKey10());
        assertNotNull(incomingRecord.getCreatedAt());
        assertNotNull(incomingRecord.getUpdatedAt());
    }

    @ParameterizedTest(name = "findTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(400)
    public void findTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.RECORD_KEY, recordKey)
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        BatchRecord batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(recordKey, batchRecord.getRecords().get(0).getRecordKey());
        assertNotNull(batchRecord.getRecords().get(0).getCreatedAt());
        assertNotNull(batchRecord.getRecords().get(0).getUpdatedAt());

        builder.clear()
                .keyEq(StringField.RECORD_KEY, batchRecordKey)
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, BATCH_WRITE_RANGE_KEY_1);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(batchRecordKey, batchRecord.getRecords().get(0).getRecordKey());

        builder.clear()
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        assertTrue(batchRecord.getRecords().stream().anyMatch(record
                -> record.getRecordKey().equals(batchRecordKey)));
        assertTrue(batchRecord.getRecords().stream().anyMatch(record
                -> record.getRecordKey().equals(recordKey)));

        builder.clear()
                .keyNotEq(StringField.RECORD_KEY, recordKey)
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(batchRecordKey, batchRecord.getRecords().get(0).getRecordKey());
    }

    @ParameterizedTest(name = "findAdvancedTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(401)
    public void findAdvancedTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.KEY2, key2)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1, BATCH_WRITE_RANGE_KEY_1, WRITE_RANGE_KEY_1 + BATCH_WRITE_RANGE_KEY_1 + 1);
        BatchRecord batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        List<String> resultIdList = new ArrayList<>();
        resultIdList.add(batchRecord.getRecords().get(0).getRecordKey());
        resultIdList.add(batchRecord.getRecords().get(1).getRecordKey());
        assertTrue(resultIdList.contains(recordKey));
        assertTrue(resultIdList.contains(batchRecordKey));
    }

    @ParameterizedTest(name = "findByVersionTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(402)
    public void findByVersionTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.KEY2, key2)
                .keyEq(StringField.VERSION, String.valueOf(VERSION));
        BatchRecord batchRecord1 = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord1.getCount());
        assertEquals(2, batchRecord1.getRecords().size());

        builder.keyEq(StringField.VERSION, String.valueOf(VERSION + 10));
        BatchRecord batchRecord2 = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(0, batchRecord2.getCount());
        assertEquals(0, batchRecord2.getRecords().size());

        builder.keyNotEq(StringField.VERSION, String.valueOf(VERSION));
        BatchRecord batchRecord3 = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(0, batchRecord3.getCount());
        assertEquals(0, batchRecord3.getRecords().size());

        builder.keyNotEq(StringField.VERSION, String.valueOf(VERSION + 10));
        BatchRecord batchRecord4 = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord4.getCount());
        assertEquals(2, batchRecord4.getRecords().size());
    }

    @ParameterizedTest(name = "findByAllFieldsTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(403)
    public void findByAllFieldsTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
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
                .keyEq(NumberField.RANGE_KEY2, RANGE_KEY_2)
                .keyEq(NumberField.RANGE_KEY3, RANGE_KEY_3)
                .keyEq(NumberField.RANGE_KEY4, RANGE_KEY_4)
                .keyEq(NumberField.RANGE_KEY5, RANGE_KEY_5)
                .keyEq(NumberField.RANGE_KEY6, RANGE_KEY_6)
                .keyEq(NumberField.RANGE_KEY7, RANGE_KEY_7)
                .keyEq(NumberField.RANGE_KEY8, RANGE_KEY_8)
                .keyEq(NumberField.RANGE_KEY9, RANGE_KEY_9)
                .keyEq(NumberField.RANGE_KEY10, RANGE_KEY_10)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(SERVICE_KEY1, SERVICE_KEY_1)
                .keyEq(SERVICE_KEY2, SERVICE_KEY_2);

        BatchRecord batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        Record record = batchRecord.getRecords().get(0);
        assertEquals(recordKey, record.getRecordKey());
        assertEquals(KEY_1, record.getKey1());
        assertEquals(key2, record.getKey2());
        assertEquals(KEY_3, record.getKey3());
        assertEquals(KEY_4, record.getKey4());
        assertEquals(KEY_5, record.getKey5());
        assertEquals(KEY_6, record.getKey6());
        assertEquals(KEY_7, record.getKey7());
        assertEquals(KEY_8, record.getKey8());
        assertEquals(KEY_9, record.getKey9());
        assertEquals(KEY_10, record.getKey10());
        assertEquals(WRITE_RANGE_KEY_1, record.getRangeKey1());
        assertEquals(RANGE_KEY_2, record.getRangeKey2());
        assertEquals(RANGE_KEY_3, record.getRangeKey3());
        assertEquals(RANGE_KEY_4, record.getRangeKey4());
        assertEquals(RANGE_KEY_5, record.getRangeKey5());
        assertEquals(RANGE_KEY_6, record.getRangeKey6());
        assertEquals(RANGE_KEY_7, record.getRangeKey7());
        assertEquals(RANGE_KEY_8, record.getRangeKey8());
        assertEquals(RANGE_KEY_9, record.getRangeKey9());
        assertEquals(RANGE_KEY_10, record.getRangeKey10());
        assertEquals(PROFILE_KEY, record.getProfileKey());
        assertEquals(SERVICE_KEY_1, record.getServiceKey1());
        assertEquals(SERVICE_KEY_2, record.getServiceKey2());
    }

    @ParameterizedTest(name = "findOneTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(404)
    public void findOneTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.KEY2, key2)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        Record record = storage.findOne(MIDIPOP_COUNTRY, builder);
        assertEquals(recordKey, record.getRecordKey());
        assertEquals(RECORD_BODY, record.getBody());
    }

    @ParameterizedTest(name = "deleteTest [{index}] {arguments}")
    @MethodSource("storageProvider")
    @Order(600)
    public void deleteTest(Storage storage, String recordKey, String batchRecordKey, String key2) throws StorageException {
        storage.delete(MIDIPOP_COUNTRY, recordKey);
        storage.delete(MIDIPOP_COUNTRY, batchRecordKey);
        // Cannot read deleted record
        Record writeMethodRecord = storage.read(MIDIPOP_COUNTRY, recordKey);
        Record batchWriteMethodRecord = storage.read(MIDIPOP_COUNTRY, batchRecordKey);
        assertNull(writeMethodRecord);
        assertNull(batchWriteMethodRecord);
    }

    @Test
    @Order(700)
    public void readIgnoreCaseTest() throws StorageException {
        Record record = new Record(RECORD_KEY_IGNORE_CASE)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storageIgnoreCase.write(MIDIPOP_COUNTRY, record);

        Record incomingRecord = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toLowerCase());
        assertEquals(RECORD_KEY_IGNORE_CASE, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());

        incomingRecord = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        assertEquals(RECORD_KEY_IGNORE_CASE, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
    }

    @Test
    @Order(702)
    public void findIgnoreCaseTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE)
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        BatchRecord batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());

        builder = builder.clear()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE.toLowerCase())
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY.toLowerCase())
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());

        builder = builder.clear()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE.toUpperCase())
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY.toUpperCase())
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());
    }

    @Test
    @Order(703)
    public void deleteIgnoreCaseTest() throws StorageException {
        storageIgnoreCase.delete(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        // Cannot read deleted record
        Record record = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE);
        assertNull(record);
        record = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        assertNull(record);
        record = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toLowerCase());
        assertNull(record);
    }

    @Test
    @Order(800)
    public void addAttachmentTest() throws StorageException, IOException {
        Record record = new Record(ATTACHMENT_RECORD_KEY)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1);
        storageOrdinary.write(MIDIPOP_COUNTRY, record);
        Path tempFile = Files.createTempFile(FILE_NAME.split("\\.")[0], FILE_NAME.split("\\.")[1]);
        Files.write(tempFile, FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        InputStream fileInputStream = Files.newInputStream(tempFile);
        AttachmentMeta attachmentMeta = storageOrdinary.addAttachment(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, fileInputStream, FILE_NAME, false, DEFAULT_MIME_TYPE);
        fileId = attachmentMeta.getFileId();
        assertEquals(FILE_NAME, attachmentMeta.getFilename());
        Files.delete(tempFile);
    }

    @Test
    @Order(801)
    void getAttachmentFileTest() throws StorageException, IOException {
        AttachedFile file = storageOrdinary.getAttachmentFile(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, fileId);
        String incomingFileContent = IOUtils.toString(file.getFileContent(), StandardCharsets.UTF_8.name());
        assertEquals(FILE_CONTENT, incomingFileContent);
        assertEquals(FILE_NAME, file.getFileName());
    }

    @Test
    @Order(802)
    void getAttachmentMetaTest() throws StorageException {
        AttachmentMeta meta = storageOrdinary.getAttachmentMeta(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, fileId);
        assertEquals(fileId, meta.getFileId());
        assertEquals(FILE_NAME, meta.getFilename());
        assertTrue(meta.getMimeType().contains(DEFAULT_MIME_TYPE));
    }

    @Test
    @Order(803)
    void updateAttachmentMetaTest() throws StorageException {
        AttachmentMeta meta = storageOrdinary.updateAttachmentMeta(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, fileId, NEW_FILE_NAME, MIME_TYPE);
        assertEquals(fileId, meta.getFileId());
        assertEquals(NEW_FILE_NAME, meta.getFilename());
        assertEquals(MIME_TYPE, meta.getMimeType());
    }

    @Test
    @Order(804)
    void deleteAttachmentTest() throws StorageException {
        storageOrdinary.deleteAttachment(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, fileId);
        AttachedFile file = storageOrdinary.getAttachmentFile(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, fileId);
        assertNull(file.getFileContent());
    }

    @Test
    @Order(805)
    void addAttachmentMultipleFilesTest() throws StorageException {
        for (int i = 0; i < 3; i++) {
            String fileName = UUID.randomUUID().toString();
            String fileContent = UUID.randomUUID().toString();
            InputStream fileInputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
            AttachmentMeta attachmentMeta = storageOrdinary.addAttachment(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, fileInputStream, fileName, false);
            attachmentFiles.put(attachmentMeta.getFileId(), fileContent);
        }
        attachmentFiles.forEach((idFile, fileContent) -> {
            Exception ex = null;
            try {
                AttachedFile file = storageOrdinary.getAttachmentFile(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, idFile);
                String incomingFileContent = IOUtils.toString(file.getFileContent(), StandardCharsets.UTF_8.name());
                assertEquals(fileContent, incomingFileContent);
            } catch (StorageException | IOException exception) {
                LOG.error("Exception during attached files reading", exception);
                ex = exception;
            }
            assertNull(ex);
        });
    }

    @Test
    @Order(806)
    public void deleteOneOfAttachmentMultipleFilesTest() throws StorageException {
        storageOrdinary.deleteAttachment(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, (String) attachmentFiles.keySet().toArray()[0]);
        Record incomingRecord = storageOrdinary.read(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY);
        assertEquals(2, incomingRecord.getAttachments().size());
    }

    @Test
    @Order(807)
    public void getAttachmentFileFromNonExistentRecordTest() throws StorageException {
        AttachedFile file = storageOrdinary.getAttachmentFile(MIDIPOP_COUNTRY, UUID.randomUUID().toString(), fileId);
        assertNull(file.getFileContent());
    }

    @Test
    @Order(808)
    public void getNonExistentAttachmentFileTest() throws StorageException {
        AttachedFile file = storageOrdinary.getAttachmentFile(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, UUID.randomUUID().toString());
        assertNull(file.getFileContent());
    }

    @Test
    @Order(810)
    public void getAttachmentMetaFromNonExistentFileTest() throws StorageException {
        AttachmentMeta meta = storageOrdinary.getAttachmentMeta(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, UUID.randomUUID().toString());
        assertNull(meta);
    }

    @Test
    @Order(811)
    public void updateAttachmentMetaForNonExistentFileTest() {
        String nonExistentFileId = UUID.randomUUID().toString();
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storageOrdinary.updateAttachmentMeta(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, nonExistentFileId, NEW_FILE_NAME, MIME_TYPE));
        assertTrue(ex.getMessage().contains("Code=404"));
        assertTrue(ex.getMessage().contains(nonExistentFileId));
    }

    @Test
    @Order(812)
    public void addAttachmentWithUnusualFileNameTest() throws StorageException, IOException {
        String fileName = "Naïve file.txt";
        Path tempFile = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        Files.write(tempFile, FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        InputStream fileInputStream = Files.newInputStream(tempFile);
        AttachmentMeta attachmentMeta = storageOrdinary.addAttachment(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY, fileInputStream, fileName, false, DEFAULT_MIME_TYPE);
        fileId = attachmentMeta.getFileId();
        assertEquals(fileName, attachmentMeta.getFilename());
        Files.delete(tempFile);
    }

    @Test
    @Order(899)
    public void deleteRecordWithAttachment() throws StorageException {
        assertTrue(storageOrdinary.delete(MIDIPOP_COUNTRY, ATTACHMENT_RECORD_KEY));
    }

    @Test
    @Order(900)
    public void findWithSearchKeys() throws StorageException {
        String recordKey = "Non hashing " + RECORD_KEY;
        Record record = new Record(recordKey)
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
                .setRangeKey10(RANGE_KEY_10)
                .setKey1(KEY_1)
                .setPrecommitBody(PRECOMMIT_BODY)
                .setServiceKey1(SERVICE_KEY_1)
                .setServiceKey2(SERVICE_KEY_2);
        storageNonHashing.write(MIDIPOP_COUNTRY, record);

        FindFilterBuilder builder = FindFilterBuilder.create()
                .searchKeysLike(KEY_1.split("-")[2]);
        BatchRecord batchRecord = storageNonHashing.find(MIDIPOP_COUNTRY, builder);

        assertEquals(1, batchRecord.getCount());
        assertEquals(recordKey, batchRecord.getRecords().get(0).getRecordKey());
        assertEquals(RECORD_BODY, batchRecord.getRecords().get(0).getBody());

        storageNonHashing.delete(MIDIPOP_COUNTRY, recordKey);
    }

    @Test
    @Order(901)
    public void utf8EncodingTest() throws StorageException {
        String recordKey = "utf8" + RECORD_KEY;
        String key1 = "Louis César de La Baume Le Blanc" + TEMP;
        Record record = new Record(recordKey)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey1(key1)
                .setPrecommitBody(PRECOMMIT_BODY);
        storageNonHashing.write(MIDIPOP_COUNTRY, record);

        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.KEY1, key1);
        BatchRecord batchRecord = storageNonHashing.find(MIDIPOP_COUNTRY, builder);

        assertEquals(1, batchRecord.getCount());
        assertEquals(recordKey, batchRecord.getRecords().get(0).getRecordKey());
        assertEquals(RECORD_BODY, batchRecord.getRecords().get(0).getBody());
        assertEquals(key1, batchRecord.getRecords().get(0).getKey1());

        storageNonHashing.delete(MIDIPOP_COUNTRY, recordKey);
    }

    @Test
    @Order(1000)
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
                String randomKey = "RecordKey" + TEMP + UUID.randomUUID().toString();
                Thread currentThread = Thread.currentThread();
                currentThread.setName("connectionPoolTest #" + numb);
                Record record = new Record(randomKey)
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
