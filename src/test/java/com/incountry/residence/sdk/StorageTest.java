package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.http.mocks.FakeHttpAgent;
import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.ApiKeyTokenClient;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private static final String SECRET = "passwordpasswordpasswordpassword";
    private static final String COUNTRY = "us";
    private static final String KEY = "some_key";
    private static final String KEY_2 = "key2";
    private static final String KEY_3 = "key3";
    private static final String PROFILE_KEY = "profileKey";
    private static final Integer RANGE_KEY = 1;
    private static final String BODY = "body";

    private CryptoManager cryptoManager;
    private SecretKeyAccessor secretKeyAccessor;
    private final TokenClient tokenClient = new ApiKeyTokenClient("<apiKey>");

    @BeforeEach
    public void initializeAccessorAndCrypto() throws StorageClientException {
        int version = 0;
        SecretKey secretKey = new SecretKey(SECRET, version, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, version);
        secretKeyAccessor = () -> secretsData;
        cryptoManager = new CryptoManager(secretKeyAccessor, ENVIRONMENT_ID, null, false);
    }

    @RepeatedTest(3)
    void migratePositiveTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        Record rec = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        String encrypted = JsonUtils.toJsonString(rec, cryptoManager);
        String content = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, new FakeHttpAgent(Arrays.asList(content, "OK"))));
        BatchRecord batchRecord = JsonUtils.batchRecordFromString(content, cryptoManager);

        int migratedRecords = batchRecord.getCount();
        int totalLeft = batchRecord.getTotal() - batchRecord.getCount();
        MigrateResult migrateResult = storage.migrate("us", 2);

        assertEquals(migratedRecords, migrateResult.getMigrated());
        assertEquals(totalLeft, migrateResult.getTotalLeft());
    }

    @Test
    void migrateNegativeTest() throws StorageException {
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, new FakeHttpAgent("")));
        assertThrows(StorageClientException.class, () -> storage.migrate("us", 0));
        Storage storage2 = StorageImpl.getInstance(ENVIRONMENT_ID, null, new HttpDaoImpl(FAKE_ENDPOINT, null, new FakeHttpAgent("")));
        assertThrows(StorageClientException.class, () -> storage2.migrate("us", 1));
    }

    @RepeatedTest(3)
    void findTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq(PROFILE_KEY);
        Record rec = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        String encrypted = JsonUtils.toJsonString(rec, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));
        BatchRecord batchRecord = storage.find(COUNTRY, builder);

        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(KEY, batchRecord.getRecords().get(0).getKey());
        assertEquals(BODY, batchRecord.getRecords().get(0).getBody());
    }

    @RepeatedTest(3)
    void testCustomEndpoint(RepetitionInfo repeatInfo) throws StorageException, IOException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(endpoint, null, agent));
        Record record = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        storage.write(COUNTRY, record);
        String expectedURL = endpoint + "/v2/storage/records/" + COUNTRY;
        String realURL = new URL(agent.getCallUrl()).toString();
        assertEquals(expectedURL, realURL);
    }

    @Test
    void testNegativeWriteNullKey() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(endpoint, null, agent));
        Record record = new Record(null, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        assertThrows(StorageClientException.class, () -> storage.write(COUNTRY, record));
    }

    @Test
    void testNegativeWriteNullRecord() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(endpoint, null, agent));
        assertThrows(StorageClientException.class, () -> storage.write(COUNTRY, null));
    }

    @Test
    void testNegativeWriteNullCountry() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(endpoint, null, agent));
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
                .profileKeyEq(PROFILE_KEY);

        Record rec = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        String encrypted = JsonUtils.toJsonString(rec, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));
        BatchRecord batchRecord = storage.find(COUNTRY, builder);
        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + cryptoManager.createKeyHash(PROFILE_KEY) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}";
        assertEquals(expected, callBody);

        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(KEY, batchRecord.getRecords().get(0).getKey());
        assertEquals(BODY, batchRecord.getRecords().get(0).getBody());
        assertEquals(KEY_2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(KEY_3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY, batchRecord.getRecords().get(0).getRangeKey());
    }

    @RepeatedTest(3)
    void testFindOne(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq(PROFILE_KEY);

        Record rec = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        String encrypted = JsonUtils.toJsonString(rec, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));

        Record foundRecord = storage.findOne(COUNTRY, builder);

        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + cryptoManager.createKeyHash(PROFILE_KEY) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}";
        assertEquals(expected, callBody);

        assertEquals(KEY, foundRecord.getKey());
        assertEquals(BODY, foundRecord.getBody());
        assertEquals(KEY_2, foundRecord.getKey2());
        assertEquals(KEY_3, foundRecord.getKey3());
        assertEquals(PROFILE_KEY, foundRecord.getProfileKey());
        assertEquals(RANGE_KEY, foundRecord.getRangeKey());

        agent.setResponse("{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}");
        foundRecord = storage.findOne(COUNTRY, builder);
        assertNull(foundRecord);

        assertThrows(StorageClientException.class, () -> storage.findOne(COUNTRY, null));
    }

    @Test
    void testFindWithEncByMultipleSecrets() throws StorageException {
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("otherpassword");
        CryptoManager otherManager = new CryptoManager(accessor, ENVIRONMENT_ID, null, false);

        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(PROFILE_KEY);

        Record recOtherEnc = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        Record recEnc = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        String encryptedRecOther = JsonUtils.toJsonString(recOtherEnc, otherManager);
        String encryptedRec = JsonUtils.toJsonString(recEnc, cryptoManager);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));

        BatchRecord batchRecord = storage.find(COUNTRY, builder);
        assertEquals(1, batchRecord.getErrors().size());
        assertEquals(encryptedRecOther, batchRecord.getErrors().get(0).getRawData());
        assertEquals("Record Parse Exception", batchRecord.getErrors().get(0).getMessage());

        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(KEY, batchRecord.getRecords().get(0).getKey());
        assertEquals(BODY, batchRecord.getRecords().get(0).getBody());
        assertEquals(KEY_2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(KEY_3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY, batchRecord.getRecords().get(0).getRangeKey());
    }

    @Test
    void testFindNullFilterSending() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq("SomeValue");
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));
        storage.find(COUNTRY, builder);
        String body = agent.getCallBody();
        JsonObject json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNotNull(json.get("key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key"));
        assertNull(json.get("profile_key"));

        builder.clear().key2Eq("SomeValue");
        storage.find(COUNTRY, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("key"));
        assertNotNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key"));
        assertNull(json.get("profile_key"));

        builder.clear().key3Eq("SomeValue");
        storage.find(COUNTRY, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("key"));
        assertNull(json.get("key2"));
        assertNotNull(json.get("key3"));
        assertNull(json.get("range_key"));
        assertNull(json.get("profile_key"));

        builder.clear().rangeKeyEq(123321);
        storage.find(COUNTRY, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNotNull(json.get("range_key"));
        assertNull(json.get("profile_key"));

        builder.clear().profileKeyEq("SomeValue");
        storage.find(COUNTRY, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key"));
        assertNotNull(json.get("profile_key"));
    }

    @Test
    void testFindWithEncAndFoundPTE() throws StorageException {
        CryptoManager cryptoAsInStorage = new CryptoManager(() -> secretKeyAccessor.getSecretsData(), ENVIRONMENT_ID, null, false);
        CryptoManager cryptoWithPT = new CryptoManager(null, ENVIRONMENT_ID, null, false);
        Record recWithEnc = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        Record recWithPTEnc = new Record(KEY + 1, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        String encryptedRec = JsonUtils.toJsonString(recWithEnc, cryptoAsInStorage);
        String encryptedPTRec = JsonUtils.toJsonString(recWithPTEnc, cryptoWithPT);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));
        BatchRecord batchRecord = storage.find(COUNTRY, FindFilterBuilder.create());
        assertEquals(0, batchRecord.getErrors().size());
        assertEquals(2, batchRecord.getRecords().size());
    }

    @Test
    void testFindWithoutEncWithEncryptedData() throws StorageException {
        CryptoManager cryptoWithEnc = new CryptoManager(() -> secretKeyAccessor.getSecretsData(), ENVIRONMENT_ID, null, false);
        CryptoManager cryptoWithPT = new CryptoManager(null, ENVIRONMENT_ID, null, false);
        Record recWithEnc = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        Record recWithPTEnc = new Record(KEY + 1, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        String encryptedRec = JsonUtils.toJsonString(recWithEnc, cryptoWithEnc);
        String encryptedPTRec = JsonUtils.toJsonString(recWithPTEnc, cryptoWithPT);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, null, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));

        BatchRecord batchRecord = storage.find(COUNTRY, FindFilterBuilder.create());
        assertEquals(1, batchRecord.getErrors().size());
        assertEquals(encryptedRec, batchRecord.getErrors().get(0).getRawData());
        assertEquals("Record Parse Exception", batchRecord.getErrors().get(0).getMessage());

        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(KEY + 1, batchRecord.getRecords().get(0).getKey());
        assertEquals(BODY, batchRecord.getRecords().get(0).getBody());
        assertEquals(KEY_2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(KEY_3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY, batchRecord.getRecords().get(0).getRangeKey());
    }

    @Test
    void testFindIncorrectRecords() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(PROFILE_KEY);
        String string = null;
        FakeHttpAgent agent = new FakeHttpAgent(string);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));
        BatchRecord findResult = storage.find(COUNTRY, builder);
        assertEquals(0, findResult.getRecords().size());
    }

    @RepeatedTest(3)
    void testReadNotFound(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String string = null;
        FakeHttpAgent agent = new FakeHttpAgent(string);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));
        Record readRecord = storage.read(COUNTRY, KEY);
        assertNull(readRecord);
    }

    @Test
    void testErrorFindOneInsufficientArgs() throws StorageException {
        Record record = new Record(KEY, BODY, PROFILE_KEY, RANGE_KEY, KEY_2, KEY_3);
        String encrypted = JsonUtils.toJsonString(record, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, new HttpDaoImpl(FAKE_ENDPOINT, null, agent));
        assertThrows(StorageClientException.class, () -> storage.find(null, null));
        assertThrows(StorageClientException.class, () -> storage.find(COUNTRY, null));
    }

    @RepeatedTest(3)
    void testInitErrorOnInsufficientArgs(RepetitionInfo repeatInfo) throws StorageClientException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret", 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(null, null, null, secretKeyAccessor));
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(null, secretKeyAccessor, null));

        StorageConfig config = new StorageConfig().setSecretKeyAccessor(secretKeyAccessor);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    void testErrorReadInsufficientArgs() throws StorageServerException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, agent);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.read(null, null));
    }

    @RepeatedTest(3)
    void testErrorDeleteInsufficientArgs(RepetitionInfo repeatInfo) throws StorageClientException, StorageServerException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, agent);
        assertNotNull(dao);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.delete(null, null));
    }

    @Test
    void testErrorMigrateWhenEncryptionOff() throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, agent);
        assertNotNull(dao);
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.migrate(null, 100));
    }

    @Test
    void testNegativeWithEmptyConstructor() {
        assertThrows(StorageClientException.class, StorageImpl::getInstance);
    }

    @Test
    void testPositiveWithConstructor2() throws StorageClientException, StorageServerException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret", 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        Storage storage = StorageImpl.getInstance(ENVIRONMENT_ID, "apiKey", FAKE_ENDPOINT, secretKeyAccessor);
        assertNotNull(storage);
    }

    @Test
    void testPositiveWithConstructor3() throws StorageClientException, StorageServerException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret", 1, false)), 1);
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
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret", 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    void testNegativeWithConstructor4nullDao() {
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(ENVIRONMENT_ID, secretKeyAccessor, null));
    }

    @Test
    void positiveTestWithClientId() throws StorageClientException, StorageServerException {
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret", 1, false)), 1);
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
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret", 1, false)), 1);
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
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret", 1, false)), 1);
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
        SecretsData secretData = new SecretsData(Collections.singletonList(new SecretKey("secret", 1, false)), 1);
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
        assertTrue(storage.delete(COUNTRY, KEY));
        server.stop(0);
    }

    @Test
    void negativeTestIllegalTimeout() {
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setApiKey("<apiKey>")
                .setHttpTimeout(0);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Connection timeout can't be <1", ex.getMessage());
    }

    @Test
    void negativeTestTimeoutError() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer("{}", 200, PORT, 5);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(1)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvId("<envId>");
        Storage storage = StorageImpl.getInstance(config);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storage.delete(COUNTRY, KEY));
        assertEquals("Server request error: DELETE", ex.getMessage());
        assertEquals("Read timed out", ex.getCause().getMessage());
        server.stop(0);
    }
}
