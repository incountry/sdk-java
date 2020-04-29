package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.http.FakeHttpAgent;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.crypto.Crypto;
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

public class StorageSingleTests {
    private Crypto crypto;
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

    @BeforeEach
    public void initializeAccessorAndCrypto() throws StorageClientException {
        SecretKey secretKey = new SecretKey(secret, version, true);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, currentVersion);
        secretKeyAccessor = () -> secretsData;
        crypto = new CryptoImpl(secretKeyAccessor, environmentId);
    }

    @Test
    public void migrateTest() throws StorageException {
        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        String content = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, new FakeHttpAgent(Arrays.asList(content, "OK"))));
        BatchRecord batchRecord = JsonUtils.batchRecordFromString(content, crypto);

        int migratedRecords = batchRecord.getCount();
        int totalLeft = batchRecord.getTotal() - batchRecord.getCount();
        MigrateResult migrateResult = storage.migrate("us", 2);

        assertEquals(migratedRecords, migrateResult.getMigrated());
        assertEquals(totalLeft, migrateResult.getTotalLeft());
    }

    @Test
    public void migrateNegativeTest() throws StorageException {
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, new FakeHttpAgent("")));
        assertThrows(StorageClientException.class, () -> storage.migrate("us", 0));
    }

    @Test
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq(profileKey);
        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));
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
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(endpoint, agent));
        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        storage.write(country, record);
        String expectedURL = endpoint + "/v2/storage/records/" + country;
        String realURL = new URL(agent.getCallEndpoint()).toString();
        assertEquals(expectedURL, realURL);
    }

    @Test
    public void testFindWithEnc() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq(profileKey);

        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));
        BatchRecord batchRecord = storage.find(country, builder);
        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}";
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
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));

        Record foundRecord = storage.findOne(country, builder);

        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}";
        assertEquals(expected, callBody);

        assertEquals(key, foundRecord.getKey());
        assertEquals(body, foundRecord.getBody());
        assertEquals(key2, foundRecord.getKey2());
        assertEquals(key3, foundRecord.getKey3());
        assertEquals(profileKey, foundRecord.getProfileKey());
        assertEquals(rangeKey, foundRecord.getRangeKey());

        assertThrows(StorageClientException.class, () -> storage.findOne(country, null));
    }

    @Test
    public void testFindWithEncByMultipleSecrets() throws StorageException {
        Crypto cryptoOther = new CryptoImpl(() -> SecretsDataGenerator.fromPassword("otherpassword"), environmentId);

        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(profileKey);

        Record recOtherEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        Record recEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encryptedRecOther = JsonUtils.toJsonString(recOtherEnc, cryptoOther);
        String encryptedRec = JsonUtils.toJsonString(recEnc, crypto);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));

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
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));
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
        Crypto cryptoWithEnc = new CryptoImpl(() -> SecretsDataGenerator.fromPassword("password"), environmentId);
        Crypto cryptoWithPT = new CryptoImpl(environmentId);
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(profileKey);

        Record recWithEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        Record recWithPTEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encryptedRec = JsonUtils.toJsonString(recWithEnc, cryptoWithEnc);
        String encryptedPTRec = JsonUtils.toJsonString(recWithPTEnc, cryptoWithPT);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));

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
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));
        BatchRecord findResult = storage.find(country, builder);
        assertEquals(0, findResult.getRecords().size());
    }

    @Test
    public void testReadNotFound() throws StorageException {
        String string = null;
        FakeHttpAgent agent = new FakeHttpAgent(string);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));
        Record readRecord = storage.read(country, key);
        assertNull(readRecord);
    }

    @Test
    public void testErrorFindOneInsufficientArgs() throws StorageException {
        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(record, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, new HttpDaoImpl(fakeEndpoint, agent));
        assertThrows(StorageClientException.class, () -> storage.find(null, null));
        assertThrows(StorageClientException.class, () -> storage.find(country, null));
    }

    @Test
    public void testInitErrorOnInsufficientArgs() throws StorageClientException {
        SecretsData secretData = new SecretsData(Arrays.asList(new SecretKey("secret", 1, false)), 1);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(null, null, null, secretKeyAccessor));
    }

    @Test
    public void testErrorReadInsufficientArgs() throws StorageServerException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(fakeEndpoint, agent);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.read(null, null));
    }

    @Test
    public void testErrorDeleteInsufficientArgs() throws StorageClientException, StorageServerException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(fakeEndpoint, agent);
        assertNotNull(dao);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.delete(null, null));
    }

    @Test
    public void testErrorMigrateWhenEncryptionOff() throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(fakeEndpoint, agent);
        assertNotNull(dao);
        Storage storage = StorageImpl.getInstance(environmentId, secretKeyAccessor, dao);
        assertThrows(StorageClientException.class, () -> storage.migrate(null, 100));
    }
}
