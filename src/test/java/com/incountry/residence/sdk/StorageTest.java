package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.http.mocks.FakeHttpAgent;
import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.containers.MetaInfoTypes;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageTest {
    private static final int PORT = 8767;
    private static final String ENVIRONMENT_ID = "envId";
    private static final String FAKE_ENDPOINT = "http://fakeEndpoint.localhost:8081";
    private static final byte[] SECRET = "passwordpasswordpasswordpassword".getBytes(StandardCharsets.UTF_8);
    private static final String COUNTRY = "us";
    private static final String RECORD_KEY = "some_key";
    private static final String KEY_2 = "key2";
    private static final String KEY_3 = "key3";
    private static final String PROFILE_KEY = "profileKey";
    private static final Long RANGE_KEY_1 = 1L;
    private static final String BODY = "body";
    private static final Integer HTTP_POOL_SIZE = 5;

    private CryptoManager cryptoManager;
    private SecretKeyAccessor secretKeyAccessor;

    @BeforeEach
    public void initializeAccessorAndCrypto() throws StorageClientException {
        int version = 0;
        SecretKey secretKey = new SecretKey(SECRET, version, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, version);
        secretKeyAccessor = () -> secretsData;
        cryptoManager = new CryptoManager(secretKeyAccessor, ENVIRONMENT_ID, null, false, true);
    }

    @RepeatedTest(3)
    void migratePositiveTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encrypted = JsonUtils.toJsonString(record, cryptoManager);
        //return 2 records, 1 correct, 1 invalid
        String response1 = "{\"data\":[" + encrypted + ",{\"not_record\":1}],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}";
        //return 1 correct record
        String response2 = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        //return 0 records
        String response3 = "{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}";
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor,
                new HttpDaoImpl(FAKE_ENDPOINT, null, null,
                        new FakeHttpAgent(Arrays.asList(response1, "OK", response2, "OK", response3, "OK"))));

        String expected1 = "MigrateResult{migrated=1, totalLeft=1, errors=[com.incountry.residence.sdk.tools.exceptions.RecordException: Record Parse Exception]}";
        runMigrationChecks(response1, expected1, storage);
        String expected2 = "MigrateResult{migrated=1, totalLeft=0, errors=[]}";
        runMigrationChecks(response2, expected2, storage);
        String expected3 = "MigrateResult{migrated=0, totalLeft=0, errors=[]}";
        runMigrationChecks(response3, expected3, storage);
    }

    private void runMigrationChecks(String response1, String expected1, Storage storage) throws StorageServerException, StorageClientException, StorageCryptoException {
        BatchRecord batchRecord = JsonUtils.batchRecordFromString(response1, cryptoManager);
        int migratedRecords = batchRecord.getRecords().size();
        int totalLeft = batchRecord.getTotal() - batchRecord.getRecords().size();
        MigrateResult migrateResult = storage.migrate("us", 2);
        assertEquals(migratedRecords, migrateResult.getMigrated());
        assertEquals(totalLeft, migrateResult.getTotalLeft());
        if (batchRecord.getErrors() != null && !batchRecord.getErrors().isEmpty()) {
            assertEquals(batchRecord.getErrors().get(0).getRawData(), migrateResult.getErrors().get(0).getRawData());
        }
        assertEquals(expected1, migrateResult.toString());
    }

    @Test
    void migrateNegativeTest() throws StorageException {
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, new FakeHttpAgent("")));
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> storage.migrate("us", 0));
        assertEquals("Limit can't be < 1", ex1.getMessage());
        Storage storage2 = StorageImpl.getInstance(ENVIRONMENT_ID, null, new HttpDaoImpl(FAKE_ENDPOINT, null, null, new FakeHttpAgent("")));
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> storage2.migrate("us", 1));
        assertEquals("Migration is not supported when encryption is off", ex2.getMessage());
    }

    @RepeatedTest(3)
    void findTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encrypted = JsonUtils.toJsonString(record, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        BatchRecord batchRecord = storage.find(COUNTRY, builder);

        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY, batchRecord.getRecords().get(0).getRecordKey());
        assertEquals(BODY, batchRecord.getRecords().get(0).getBody());
    }

    @RepeatedTest(3)
    void testCustomEndpoint(RepetitionInfo repeatInfo) throws StorageException, IOException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(endpoint, null, null, agent));
        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storage.write(COUNTRY, record);
        String expectedURL = endpoint + "/v2/storage/records/" + COUNTRY;
        String realURL = new URL(agent.getCallUrl()).toString();
        assertEquals(expectedURL, realURL);
    }

    @Test
    void testNegativeWriteNullKey() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(endpoint, null, null, agent));
        Record record = new Record(null, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.write(COUNTRY, record));
        assertEquals("Key can't be null", ex.getMessage());
    }

    @Test
    void testNegativeWriteNullRecord() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(endpoint, null, null, agent));
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.write(COUNTRY, null));
        assertEquals("Can't write null record", ex.getMessage());
    }

    @Test
    void testNegativeWriteNullCountry() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(endpoint, null, null, agent));
        String key = "<key>";
        Record record = new Record(key, "<body>");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.write(null, record));
        assertEquals("Country can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.read(null, key));
        assertEquals("Country can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.delete(null, key));
        assertEquals("Country can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.batchWrite(null, Collections.singletonList(record)));
        assertEquals("Country can't be null", ex.getMessage());
    }

    @Test
    void testFindWithEnc() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);

        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encrypted = JsonUtils.toJsonString(record, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        BatchRecord batchRecord = storage.find(COUNTRY, builder);
        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + cryptoManager.createKeyHash(PROFILE_KEY) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}";
        assertEquals(expected, callBody);

        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY, batchRecord.getRecords().get(0).getRecordKey());
        assertEquals(BODY, batchRecord.getRecords().get(0).getBody());
        assertEquals(KEY_2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(KEY_3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY_1, batchRecord.getRecords().get(0).getRangeKey1());
        assertEquals(0, batchRecord.getRecords().get(0).getAttachments().size());
    }

    @RepeatedTest(3)
    void testFindOne(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);

        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encrypted = JsonUtils.toJsonString(record, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        Record foundRecord = storage.findOne(COUNTRY, builder);

        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + cryptoManager.createKeyHash(PROFILE_KEY) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}";
        assertEquals(expected, callBody);

        assertEquals(RECORD_KEY, foundRecord.getRecordKey());
        assertEquals(BODY, foundRecord.getBody());
        assertEquals(KEY_2, foundRecord.getKey2());
        assertEquals(KEY_3, foundRecord.getKey3());
        assertEquals(PROFILE_KEY, foundRecord.getProfileKey());
        assertEquals(RANGE_KEY_1, foundRecord.getRangeKey1());

        agent.setResponse("{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}");
        foundRecord = storage.findOne(COUNTRY, builder);
        assertNull(foundRecord);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.findOne(COUNTRY, null));
        assertEquals("Filters can't be null", ex.getMessage());
    }

    @Test
    void testFindWithEncByMultipleSecrets() throws StorageException {
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("otherpassword");
        CryptoManager otherManager = new CryptoManager(accessor, ENVIRONMENT_ID, null, false, true);

        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);

        Record recOtherEnc = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        Record recEnc = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedRecOther = JsonUtils.toJsonString(recOtherEnc, otherManager);
        String encryptedRec = JsonUtils.toJsonString(recEnc, cryptoManager);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        BatchRecord batchRecord = storage.find(COUNTRY, builder);
        assertEquals(1, batchRecord.getErrors().size());
        assertEquals(encryptedRecOther, batchRecord.getErrors().get(0).getRawData());
        assertEquals("Record Parse Exception", batchRecord.getErrors().get(0).getMessage());

        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY, batchRecord.getRecords().get(0).getRecordKey());
        assertEquals(BODY, batchRecord.getRecords().get(0).getBody());
        assertEquals(KEY_2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(KEY_3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY_1, batchRecord.getRecords().get(0).getRangeKey1());
    }

    @Test
    void testFindNullFilterSending() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.RECORD_KEY, "SomeValue");
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        storage.find(COUNTRY, builder);
        String body = agent.getCallBody();
        JsonObject json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNotNull(json.get("record_key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key1"));
        assertNull(json.get("profile_key"));

        builder.clear().keyEq(StringField.KEY2, "SomeValue");
        storage.find(COUNTRY, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("record_key"));
        assertNotNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key1"));
        assertNull(json.get("profile_key"));

        builder.clear().keyEq(StringField.KEY3, "SomeValue");
        storage.find(COUNTRY, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("record_key"));
        assertNull(json.get("key2"));
        assertNotNull(json.get("key3"));
        assertNull(json.get("range_key1"));
        assertNull(json.get("profile_key"));

        builder.clear().keyEq(NumberField.RANGE_KEY1, 123321L);
        storage.find(COUNTRY, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("record_key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNotNull(json.get("range_key1"));
        assertNull(json.get("profile_key"));

        builder.clear().keyEq(StringField.PROFILE_KEY, "SomeValue");
        storage.find(COUNTRY, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("record_key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key1"));
        assertNotNull(json.get("profile_key"));
    }

    @Test
    void testFindWithEncAndFoundPTE() throws StorageException {
        CryptoManager cryptoAsInStorage = new CryptoManager(() -> secretKeyAccessor.getSecretsData(), ENVIRONMENT_ID, null, false, true);
        CryptoManager cryptoWithPT = new CryptoManager(null, ENVIRONMENT_ID, null, false, true);
        Record recWithEnc = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        Record recWithPTEnc = new Record(RECORD_KEY + 1, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedRec = JsonUtils.toJsonString(recWithEnc, cryptoAsInStorage);
        String encryptedPTRec = JsonUtils.toJsonString(recWithPTEnc, cryptoWithPT);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        BatchRecord batchRecord = storage.find(COUNTRY, FindFilterBuilder.create());
        assertEquals(0, batchRecord.getErrors().size());
        assertEquals(2, batchRecord.getRecords().size());
    }

    @Test
    void testFindWithoutEncWithEncryptedData() throws StorageException {
        CryptoManager cryptoWithEnc = new CryptoManager(() -> secretKeyAccessor.getSecretsData(), ENVIRONMENT_ID, null, false, true);
        CryptoManager cryptoWithPT = new CryptoManager(null, ENVIRONMENT_ID, null, false, true);
        Record recWithEnc = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        Record recWithPTEnc = new Record(RECORD_KEY + 1, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedRec = JsonUtils.toJsonString(recWithEnc, cryptoWithEnc);
        String encryptedPTRec = JsonUtils.toJsonString(recWithPTEnc, cryptoWithPT);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, null, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        BatchRecord batchRecord = storage.find(COUNTRY, FindFilterBuilder.create());
        assertEquals(1, batchRecord.getErrors().size());
        assertEquals(encryptedRec, batchRecord.getErrors().get(0).getRawData());
        assertEquals("Record Parse Exception", batchRecord.getErrors().get(0).getMessage());

        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY + 1, batchRecord.getRecords().get(0).getRecordKey());
        assertEquals(BODY, batchRecord.getRecords().get(0).getBody());
        assertEquals(KEY_2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(KEY_3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY_1, batchRecord.getRecords().get(0).getRangeKey1());
    }

    @Test
    void testFindIncorrectRecords() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        String string = null;
        FakeHttpAgent agent = new FakeHttpAgent(string);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        BatchRecord findResult = storage.find(COUNTRY, builder);
        assertEquals(0, findResult.getRecords().size());
    }

    @RepeatedTest(3)
    void testReadNotFound(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String string = null;
        FakeHttpAgent agent = new FakeHttpAgent(string);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        Record readRecord = storage.read(COUNTRY, RECORD_KEY);
        assertNull(readRecord);
    }

    @Test
    void testErrorFindOneInsufficientArgs() throws StorageException {
        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encrypted = JsonUtils.toJsonString(record, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> storage.find(null, null));
        assertEquals("Country can't be null", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> storage.find(COUNTRY, null));
        assertEquals("Filters can't be null", ex2.getMessage());
    }

    @RepeatedTest(3)
    void testInitErrorOnInsufficientArgs(RepetitionInfo repeatInfo) throws StorageClientException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret".getBytes(StandardCharsets.UTF_8), 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(null, null, null, secretKeyAccessor));
        assertEquals("Please pass environment_id param or set INC_ENVIRONMENT_ID env var", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(null, secretKeyAccessor, null));
        assertEquals("Please pass environment_id param or set INC_ENVIRONMENT_ID env var", ex2.getMessage());

        StorageConfig config = new StorageConfig().setSecretKeyAccessor(secretKeyAccessor);
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Please pass environment_id param or set INC_ENVIRONMENT_ID env var", ex3.getMessage());
    }

    @Test
    void testErrorReadInsufficientArgs() throws StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, dao);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.read(null, null));
        assertEquals("Country can't be null", ex.getMessage());
    }

    @RepeatedTest(3)
    void testErrorDeleteInsufficientArgs(RepetitionInfo repeatInfo) throws StorageClientException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent);
        assertNotNull(dao);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, dao);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.delete(null, null));
        assertEquals("Country can't be null", ex.getMessage());
    }

    @Test
    void testErrorMigrateWhenEncryptionOff() throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent);
        assertNotNull(dao);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, dao);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.migrate(null, 100));
        assertEquals("Country can't be null", ex.getMessage());
    }

    @Test
    void testNegativeWithEmptyConstructor() {
        StorageClientException ex = assertThrows(StorageClientException.class, StorageImpl::getInstance);
        assertEquals("Please pass environment_id param or set INC_ENVIRONMENT_ID env var", ex.getMessage());
    }

    @Test
    void testPositiveWithConstructor2() throws StorageClientException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret".getBytes(StandardCharsets.UTF_8), 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, "apiKey", FAKE_ENDPOINT, secretKeyAccessor);
        assertNotNull(storage);
    }

    @Test
    void testPositiveWithConstructor3() throws StorageClientException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret".getBytes(StandardCharsets.UTF_8), 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setApiKey("apiKey")
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor);
        Storage storage = StorageImpl.getInstance(config);
        assertNotNull(storage);
    }

    @Test
    void testNegativeWithConstructor3emptyApikey() throws StorageClientException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret".getBytes(StandardCharsets.UTF_8), 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Please pass (clientId, clientSecret) in configuration or set (INC_CLIENT_ID, INC_CLIENT_SECRET) env vars", ex.getMessage());
    }

    @Test
    void testNegativeWithConstructor4nullDao() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, null));
        assertEquals("Please pass (clientId, clientSecret) in configuration or set (INC_CLIENT_ID, INC_CLIENT_SECRET) env vars", ex.getMessage());
    }

    @Test
    void positiveTestWithClientId() throws StorageClientException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret".getBytes(StandardCharsets.UTF_8), 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setClientId("clientId")
                .setClientSecret("clientSecret");
        assertNotNull(StorageImpl.getInstance(config));
    }

    @Test
    void negativeTestNullClientSecret() throws StorageClientException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret".getBytes(StandardCharsets.UTF_8), 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setClientId("clientId");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Please pass (clientId, clientSecret) in configuration or set (INC_CLIENT_ID, INC_CLIENT_SECRET) env vars", ex.getMessage());
    }

    @Test
    void negativeTestEmptySecret() throws StorageClientException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret".getBytes(StandardCharsets.UTF_8), 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setClientId("")
                .setClientSecret("");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Please pass clientId in configuration or set INC_CLIENT_ID env var", ex.getMessage());
    }

    @Test
    void negativeTestBothAuth() throws StorageClientException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret".getBytes(StandardCharsets.UTF_8), 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setClientId("<clientId>")
                .setApiKey("<apiKey>");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Either apiKey or clientId/clientSecret can be used at the same moment, not both", ex.getMessage());
    }

    @Test
    void positiveTestCustomTimeout() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer("{}", 200, PORT);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>");
        Storage storage = StorageImpl.getInstance(config);
        assertTrue(storage.delete(COUNTRY, RECORD_KEY));
        server.stop(0);
    }

    @Test
    void negativeTestNullResponse() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer((String) null, 200, PORT);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>");
        Storage storage = StorageImpl.getInstance(config);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storage.read(COUNTRY, RECORD_KEY));
        assertEquals("Received record is null", ex.getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestIllegalTimeout() {
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setApiKey("<apiKey>")
                .setHttpTimeout(0)
                .setMaxHttpPoolSize(HTTP_POOL_SIZE);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Connection timeout can't be <1. Expected 'null' or positive value, received=0", ex.getMessage());
    }

    @Test
    void negativeTestTimeoutError() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer("{}", 200, PORT, 5);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(1)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>")
                .setMaxHttpPoolSize(HTTP_POOL_SIZE);
        Storage storage = StorageImpl.getInstance(config);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storage.delete(COUNTRY, RECORD_KEY));
        assertEquals("Server request error: [URL=http://localhost:8767/v2/storage/records/us/463ca9fb48993ae6c598d58aa4a5e6c4e66610e869aff32916ba643387ad4afa, method=DELETE]", ex.getMessage());
        assertEquals("Read timed out", ex.getCause().getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestWithIllegalPoolSize() {
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(1)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>")
                .setMaxHttpPoolSize(0);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("HTTP pool size can't be < 1. Expected 'null' or positive value, received=0", ex.getMessage());

        StorageConfig config1 = config
                .copy()
                .setMaxHttpPoolSize(-1);
        ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config1));
        assertEquals("HTTP pool size can't be < 1. Expected 'null' or positive value, received=-1", ex.getMessage());

        StorageConfig config2 = config
                .copy()
                .setMaxHttpPoolSize(20)
                .setMaxHttpConnectionsPerRoute(0);
        ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config2));
        assertEquals("Max HTTP connections count per route can't be < 1. Expected 'null' or positive value, received=0", ex.getMessage());

        StorageConfig config3 = config2
                .copy()
                .setMaxHttpConnectionsPerRoute(-1);
        ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config3));
        assertEquals("Max HTTP connections count per route can't be < 1. Expected 'null' or positive value, received=-1", ex.getMessage());
    }

    @Test
    void negativeTestIllegalRecordKeysLength() throws StorageException {
        String generatedString = new SecureRandom().ints(97, 123) // from 'a' to 'z'
                .limit(300)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(1)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>")
                .setMaxHttpPoolSize(1)
                .setHashSearchKeys(false);
        Storage storage = StorageImpl.getInstance(config);

        Record record = new Record(RECORD_KEY, BODY)
                .setKey10(generatedString);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.write(COUNTRY, record));
        assertEquals("key1-key20 length can't be more than 256 chars", ex.getMessage());
        Record record1 = new Record(RECORD_KEY, BODY)
                .setKey10("generatedString");
        StorageServerException ex1 = assertThrows(StorageServerException.class, () -> storage.write(COUNTRY, record1));
        assertTrue(ex1.getMessage().startsWith("Server request error"));
    }

    @Test
    void searchKeysTest() throws StorageClientException {
        FindFilterBuilder filterBuilder1 = FindFilterBuilder.create().keyEq(StringField.KEY1, "key");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> filterBuilder1.searchKeysLike("search_keys"));
        assertEquals("SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY20 lookup", ex.getMessage());

        FindFilterBuilder filterBuilder2 = FindFilterBuilder.create().searchKeysLike("search_keys");
        ex = assertThrows(StorageClientException.class, () -> filterBuilder2
                .keyEq(StringField.KEY1, "key"));
        assertEquals("SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY20 lookup", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create()
                .keyEq(StringField.SEARCH_KEYS, "search_keys"));
        assertEquals("SEARCH_KEYS can be used only via searchKeysLike method", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create()
                .searchKeysLike("se"));
        assertEquals("SEARCH_KEYS should contain at least 3 characters and be not longer than 200", ex.getMessage());

        String generatedString = new SecureRandom().ints(97, 123) // from 'a' to 'z'
                .limit(201)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        ex = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create()
                .searchKeysLike(generatedString));
        assertEquals("SEARCH_KEYS should contain at least 3 characters and be not longer than 200", ex.getMessage());
    }

    @RepeatedTest(3)
    void addAttachmentTest(RepetitionInfo repeatInfo) throws StorageException, IOException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String fileName = "sdk_incountry_unit_tests_file.txt";
        String fileId = "123456";
        String fileContent = "Hello world!";
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, new FakeHttpAgent(String.format("{\"file_id\":\"%s\"}", fileId))));
        Path tempFile = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        InputStream fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        AttachmentMeta attachmentMeta = storage.addAttachment("us", recordKey, fileInputStream, fileName, true);
        assertEquals(fileId, attachmentMeta.getFileId());

        fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        attachmentMeta = storage.addAttachment("us", recordKey, fileInputStream, fileName);
        assertEquals(fileId, attachmentMeta.getFileId());

        fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        attachmentMeta = storage.addAttachment("us", recordKey, fileInputStream, fileName, "text/plain");
        assertEquals(fileId, attachmentMeta.getFileId());

        fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        attachmentMeta = storage.addAttachment("us", recordKey, fileInputStream, fileName, true, "text/plain");
        assertEquals(fileId, attachmentMeta.getFileId());
        fileInputStream.close();
        Files.delete(tempFile);
    }

    @RepeatedTest(3)
    void deleteAttachmentTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String country = "us";
        String fileId = "1";
        FakeHttpAgent agent = new FakeHttpAgent("{}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        assertTrue(storage.deleteAttachment(country, recordKey, fileId));
    }

    @RepeatedTest(3)
    void getAttachmentTest(RepetitionInfo repeatInfo) throws StorageException, IOException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String country = "us";
        String fileId = "123456";
        String fileContent = "Hello world!";
        String fileName = "sdk_incountry_unit_tests_file";
        String fileExtension = "txt";
        Path tempFile = Files.createTempFile(fileName, fileExtension);
        InputStream fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));

        Map<MetaInfoTypes, String> metaInfo = new HashMap<>();
        metaInfo.put(MetaInfoTypes.NAME, fileName);
        String expectedResponse = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8.name());
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, new FakeHttpAgent(expectedResponse, metaInfo)));

        AttachedFile file = storage.getAttachmentFile(country, recordKey, fileId);

        assertEquals(expectedResponse, IOUtils.toString(file.getFileContent(), StandardCharsets.UTF_8.name()));
        assertEquals(fileName, file.getFileName());
    }

    @RepeatedTest(3)
    void updateAttachmentMetaTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String country = "us";
        String fileId = "1";
        String downloadLink = "some_link";
        String fileName = "test_file";
        String hash = "1234567890";
        String mimeType = "text/plain";
        int size = 1000;

        JsonObject response = new JsonObject();
        response.addProperty("file_id", fileId);
        response.addProperty("download_link", downloadLink);
        response.addProperty("filename", fileName);
        response.addProperty("hash", hash);
        response.addProperty("mime_type", mimeType);
        response.addProperty("size", size);

        FakeHttpAgent agent = new FakeHttpAgent(new Gson().toJson(response));
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        AttachmentMeta attachmentMeta = storage.updateAttachmentMeta(country, recordKey, fileId, fileName, null);
        assertEquals(JsonUtils.getDataFromAttachmentMetaJson(response.toString()), attachmentMeta);

        attachmentMeta = storage.updateAttachmentMeta(country, recordKey, fileId, null, mimeType);
        assertEquals(JsonUtils.getDataFromAttachmentMetaJson(response.toString()), attachmentMeta);

        attachmentMeta = storage.updateAttachmentMeta(country, recordKey, fileId, fileName, mimeType);
        assertEquals(JsonUtils.getDataFromAttachmentMetaJson(response.toString()), attachmentMeta);
    }

    @RepeatedTest(3)
    void getAttachmentMetaTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String country = "us";
        String fileId = "1";
        String downloadLink = "some_link";
        String fileName = "test_file";
        String hash = "1234567890";
        String mimeType = "text/plain";
        int size = 1000;

        JsonObject response = new JsonObject();
        response.addProperty("file_id", fileId);
        response.addProperty("download_link", downloadLink);
        response.addProperty("filename", fileName);
        response.addProperty("hash", hash);
        response.addProperty("mime_type", mimeType);
        response.addProperty("size", size);

        FakeHttpAgent agent = new FakeHttpAgent(new Gson().toJson(response));
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        AttachmentMeta attachmentMeta = storage.getAttachmentMeta(country, recordKey, fileId);
        assertEquals(JsonUtils.getDataFromAttachmentMetaJson(response.toString()), attachmentMeta);
    }

    @Test
    void addAttachmentTestWithIllegalParams() throws StorageException, IOException {
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>");
        Storage storage = StorageImpl.getInstance(config);
        String recordKey = "key";
        InputStream inputStream = null;
        String fileName = "sdk_incountry_unit_tests_file.txt";
        Path tempFile = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        try {
            inputStream = Files.newInputStream(tempFile);
            Files.write(tempFile, "Hello world!".getBytes(StandardCharsets.UTF_8));

            InputStream fileInputStream = inputStream;
            StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.addAttachment(null, recordKey, fileInputStream, fileName, false));
            assertEquals("Country can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("", recordKey, fileInputStream, fileName, false));
            assertEquals("Country can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", null, fileInputStream, fileName, false));
            assertEquals("Key can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", "", fileInputStream, fileName, false));
            assertEquals("Key can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", recordKey, null, fileName, false));
            assertEquals("Input stream can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", recordKey, new InputStream() {
                @Override
                public int read() {
                    return -1;
                }

                @Override
                public int available() {
                    return -1;
                }
            }, fileName, false));
            assertEquals("Input stream can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", recordKey, new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }

                @Override
                public int available() throws IOException {
                    throw new IOException();
                }
            }, fileName, false));
            assertEquals("Input stream is not available", ex.getMessage());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            Files.delete(tempFile);
        }
    }

    @RepeatedTest(3)
    void updateAttachmentMetaWithIllegalParams(RepetitionInfo repeatInfo) throws StorageClientException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>");
        Storage storage = StorageImpl.getInstance(config);
        String recordKey = "key";
        String fileId = "123";
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.updateAttachmentMeta("us", recordKey, fileId, null, null));
        assertEquals("File name and MIME type can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.updateAttachmentMeta("us", recordKey, fileId, "", ""));
        assertEquals("File name and MIME type can't be null", ex.getMessage());
    }

    @Test
    void deleteAttachmentTestWithIllegalParams() throws StorageClientException {
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>");
        Storage storage = StorageImpl.getInstance(config);
        String recordKey = "key";
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.deleteAttachment("us", recordKey, null));
        assertEquals("File ID can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.deleteAttachment("us", recordKey, ""));
        assertEquals("File ID can't be null", ex.getMessage());
    }

    @Test
    void nullConfigNegativeTest() {
        StorageClientException ex = assertThrows(StorageClientException.class, () ->
                StorageImpl.getInstance((StorageConfig) null));
        assertEquals("Storage configuration is null", ex.getMessage());
    }
}
