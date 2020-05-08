package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.http.FakeAuthClient;
import com.incountry.residence.sdk.http.FakeHttpAgent;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.http.AuthClient;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StorageTest {
    private CryptoManager cryptoManager;
    private SecretKeyAccessor secretKeyAccessor;
    private String secret = "passwordpasswordpasswordpassword";
    int version = 0;
    int currentVersion = 0;

    private String country = "us";
    private String key = "some_key";
    private String profileKey = "profileKey";
    private String key2 = "key2";
    private String key3 = "key3";
    private Integer rangeKey = 1;
    private String body = "body";

    private String environmentId = "envId";
    private String fakeEndpoint = "http://fakeEndpoint.localhost:8081";
    private AuthClient authClient = new FakeAuthClient(300L);

    @BeforeEach
    public void initializeAccessorAndCrypto() throws StorageClientException {
        SecretKey secretKey = new SecretKey(secret, version, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, currentVersion);
        secretKeyAccessor = () -> secretsData;
        cryptoManager = new CryptoManager(secretKeyAccessor, environmentId);
    }

    @Test
    public void migratePositiveTest() throws StorageException {
        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, cryptoManager);
        String content = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, new FakeHttpAgent(Arrays.asList(content, "OK")), authClient));
        BatchRecord batchRecord = JsonUtils.batchRecordFromString(content, cryptoManager);

        int migratedRecords = batchRecord.getCount();
        int totalLeft = batchRecord.getTotal() - batchRecord.getCount();
        MigrateResult migrateResult = storage.migrate("us", 2);

        assertEquals(migratedRecords, migrateResult.getMigrated());
        assertEquals(totalLeft, migrateResult.getTotalLeft());
    }

    @Test
    public void migrateNegativeTest() throws StorageException {
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, new FakeHttpAgent(""), authClient));
        assertThrows(StorageClientException.class, () -> storage.migrate("us", 0));
        Storage storage2 = StorageImpl.getInstance(environmentId, null, new HttpDaoImpl(fakeEndpoint, new FakeHttpAgent(""), authClient));
        assertThrows(StorageClientException.class, () -> storage2.migrate("us", 1));
    }

    @Test
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq(profileKey);
        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));
        BatchRecord batchRecord = storage.find(country, builder);

        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(key, batchRecord.getRecords().get(0).getKey());
        assertEquals(body, batchRecord.getRecords().get(0).getBody());
    }

    @Test
    public void testCustomEndpoint() throws StorageException, IOException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(endpoint, agent, authClient));
        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        storage.write(country, record);
        String expectedURL = endpoint + "/v2/storage/records/" + country;
        String realURL = new URL(agent.getCallEndpoint()).toString();
        assertEquals(expectedURL, realURL);
    }

    @Test
    public void testNegativeWriteNullKey() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(endpoint, agent, authClient));
        Record record = new Record(null, body, profileKey, rangeKey, key2, key3);
        assertThrows(StorageClientException.class, () -> storage.write(country, record));
    }

    @Test
    public void testNegativeWriteNullRecord() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(endpoint, agent, authClient));
        assertThrows(StorageClientException.class, () -> storage.write(country, null));
    }

    @Test
    public void testFindWithEnc() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq(profileKey);

        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));
        BatchRecord batchRecord = storage.find(country, builder);
        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + cryptoManager.createKeyHash(profileKey) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}";
        assertEquals(expected, callBody);

        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(key, batchRecord.getRecords().get(0).getKey());
        assertEquals(body, batchRecord.getRecords().get(0).getBody());
        assertEquals(key2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(key3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(profileKey, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(rangeKey, batchRecord.getRecords().get(0).getRangeKey());
    }

    @Test
    public void testFindOne() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq(profileKey);

        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));

        Record foundRecord = storage.findOne(country, builder);

        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + cryptoManager.createKeyHash(profileKey) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}";
        assertEquals(expected, callBody);

        assertEquals(key, foundRecord.getKey());
        assertEquals(body, foundRecord.getBody());
        assertEquals(key2, foundRecord.getKey2());
        assertEquals(key3, foundRecord.getKey3());
        assertEquals(profileKey, foundRecord.getProfileKey());
        assertEquals(rangeKey, foundRecord.getRangeKey());

        agent.setResponse("{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}");
        foundRecord = storage.findOne(country, builder);
        assertNull(foundRecord);

        assertThrows(StorageClientException.class, () -> storage.findOne(country, null));
    }

    @Test
    public void testFindWithEncByMultipleSecrets() throws StorageException {
        CryptoManager otherManager = new CryptoManager(() -> SecretsDataGenerator.fromPassword("otherpassword"), environmentId);

        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(profileKey);

        Record recOtherEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        Record recEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encryptedRecOther = JsonUtils.toJsonString(recOtherEnc, otherManager);
        String encryptedRec = JsonUtils.toJsonString(recEnc, cryptoManager);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));

        BatchRecord batchRecord = storage.find(country, builder);
        assertEquals(1, batchRecord.getErrors().size());
        assertEquals(encryptedRecOther, batchRecord.getErrors().get(0).getRawData());
        assertEquals("Record Parse Exception", batchRecord.getErrors().get(0).getMessage());

        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(key, batchRecord.getRecords().get(0).getKey());
        assertEquals(body, batchRecord.getRecords().get(0).getBody());
        assertEquals(key2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(key3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(profileKey, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(rangeKey, batchRecord.getRecords().get(0).getRangeKey());
    }

    @Test
    public void testFindNullFilterSending() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq("SomeValue");
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));
        storage.find(country, builder);
        String body = agent.getCallBody();
        JsonObject json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNotNull(json.get("key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key"));
        assertNull(json.get("profile_key"));

        builder.clear().key2Eq("SomeValue");
        storage.find(country, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("key"));
        assertNotNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key"));
        assertNull(json.get("profile_key"));

        builder.clear().key3Eq("SomeValue");
        storage.find(country, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("key"));
        assertNull(json.get("key2"));
        assertNotNull(json.get("key3"));
        assertNull(json.get("range_key"));
        assertNull(json.get("profile_key"));

        builder.clear().rangeKeyEq(123321);
        storage.find(country, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNotNull(json.get("range_key"));
        assertNull(json.get("profile_key"));

        builder.clear().profileKeyEq("SomeValue");
        storage.find(country, builder);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key"));
        assertNotNull(json.get("profile_key"));
    }

    @Test
    public void testFindWithoutEncWithEncryptedData() throws StorageException {
        CryptoManager cryptoWithEnc = new CryptoManager(() -> SecretsDataGenerator.fromPassword("password"), environmentId);
        CryptoManager cryptoWithPT = new CryptoManager(environmentId);
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(profileKey);

        Record recWithEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        Record recWithPTEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encryptedRec = JsonUtils.toJsonString(recWithEnc, cryptoWithEnc);
        String encryptedPTRec = JsonUtils.toJsonString(recWithPTEnc, cryptoWithPT);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));

        BatchRecord batchRecord = storage.find(country, builder);
        assertEquals(1, batchRecord.getErrors().size());
        assertEquals(encryptedRec, batchRecord.getErrors().get(0).getRawData());
        assertEquals("Record Parse Exception", batchRecord.getErrors().get(0).getMessage());

        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(key, batchRecord.getRecords().get(0).getKey());
        assertEquals(body, batchRecord.getRecords().get(0).getBody());
        assertEquals(key2, batchRecord.getRecords().get(0).getKey2());
        assertEquals(key3, batchRecord.getRecords().get(0).getKey3());
        assertEquals(profileKey, batchRecord.getRecords().get(0).getProfileKey());
        assertEquals(rangeKey, batchRecord.getRecords().get(0).getRangeKey());
    }

    @Test
    public void testFindIncorrectRecords() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(profileKey);
        String string = null;
        FakeHttpAgent agent = new FakeHttpAgent(string);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));
        BatchRecord findResult = storage.find(country, builder);
        assertEquals(0, findResult.getRecords().size());
    }

    @Test
    public void testReadNotFound() throws StorageException {
        String string = null;
        FakeHttpAgent agent = new FakeHttpAgent(string);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));
        Record readRecord = storage.read(country, key);
        assertNull(readRecord);
    }

    @Test
    public void testErrorFindOneInsufficientArgs() throws StorageException {
        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(record, cryptoManager);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent, authClient));
        assertThrows(StorageClientException.class, () -> storage.find(null, null));
        assertThrows(StorageClientException.class, () -> storage.find(country, null));
    }

    @Test
    public void testInitErrorOnInsufficientArgs() throws StorageClientException {
        SecretsData secretData = new SecretsData(Arrays.asList(new SecretKey("secret", 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(null, null, null, secretKeyAccessor));
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(null, secretKeyAccessor, null));

        StorageConfig config = new StorageConfig().setSecretKeyAccessor(secretKeyAccessor);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    public void testErrorReadInsufficientArgs() throws StorageServerException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(fakeEndpoint, agent, authClient);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.read(null, null));
    }

    @Test
    public void testErrorDeleteInsufficientArgs() throws StorageClientException, StorageServerException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(fakeEndpoint, agent, authClient);
        assertNotNull(dao);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.delete(null, null));
    }

    @Test
    public void testErrorMigrateWhenEncryptionOff() throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(fakeEndpoint, agent, authClient);
        assertNotNull(dao);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.migrate(null, 100));
    }

    @Test
    public void testNegativeWithEmptyConstructor() {
        assertThrows(StorageClientException.class, StorageImpl::getInstance);
    }

    @Test
    public void testPositiveWithConstructor2() throws StorageClientException, StorageServerException {
        SecretsData secretData = new SecretsData(Arrays.asList(new SecretKey("secret", 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        Storage storage = StorageImpl.getInstance(environmentId, "apiKey", fakeEndpoint, secretKeyAccessor);
        assertNotNull(storage);
    }

    @Test
    public void testPositiveWithConstructor3() throws StorageClientException, StorageServerException, StorageCryptoException {
        SecretsData secretData = new SecretsData(Arrays.asList(new SecretKey("secret", 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(environmentId)
                .setApiKey("apiKey")
                .setEndPoint(fakeEndpoint)
                .setSecretKeyAccessor(secretKeyAccessor);
        Storage storage = StorageImpl.getInstance(config);
        assertNotNull(storage);
    }

    @Test
    public void testNegativeWithConstructor3emptyApikey() throws StorageClientException {
        SecretsData secretData = new SecretsData(Arrays.asList(new SecretKey("secret", 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvId(environmentId)
                .setEndPoint(fakeEndpoint)
                .setSecretKeyAccessor(secretKeyAccessor);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    public void testNegativeWithConstructor4nullDao() {
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(environmentId, secretKeyAccessor, null));
    }
}
