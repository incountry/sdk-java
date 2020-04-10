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
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StorageSingleTests {
    private Storage storage;
    private Crypto crypto;
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
    private String apiKey = "apiKey";

    @BeforeEach
    public void initializeStorage() throws StorageServerException {
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(version);
        secretKey.setIsKey(true);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretKeysData secretKeysData = new SecretKeysData();
        secretKeysData.setSecrets(secretKeyList);
        secretKeysData.setCurrentVersion(currentVersion);

        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(() -> secretKeysData);

        storage = new StorageImpl(
                environmentId,
                apiKey,
                secretKeyAccessor
        );

        crypto = new CryptoImpl(secretKeyAccessor.getKey(), environmentId);
    }

    @Test
    public void migrateTest() throws StorageException {
        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        String content = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        storage.setDao(new HttpDaoImpl(null, new FakeHttpAgent(content)));
        BatchRecord batchRecord = JsonUtils.batchRecordFromString(content, crypto);

        int migratedRecords = batchRecord.getCount();
        int totalLeft = batchRecord.getTotal() - batchRecord.getCount();
        MigrateResult migrateResult = storage.migrate("us", 2);

        assertEquals(migratedRecords, migrateResult.getMigrated());
        assertEquals(totalLeft, migrateResult.getTotalLeft());
    }

    @Test
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq(profileKey);
        Record rec = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        storage.setDao(new HttpDaoImpl(null, agent));
        BatchRecord batchRecord = storage.find(country, builder);

        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(key, batchRecord.getRecords().get(0).getKey());
        assertEquals(body, batchRecord.getRecords().get(0).getBody());
    }

    @Test
    public void testCustomEndpoint() throws StorageException, IOException {
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor("password");
        String endpoint = "https://custom.endpoint.io";

        StorageImpl storage = new StorageImpl(environmentId, apiKey, endpoint, true, secretKeyAccessor);
        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setDao(new HttpDaoImpl(endpoint, agent));
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
        storage.setDao(new HttpDaoImpl(null, agent));
        BatchRecord batchRecord = storage.find(country, builder);
        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"offset\":0,\"limit\":1}}";
        assertEquals(new Gson().fromJson(expected, JsonObject.class), new Gson().fromJson(callBody, JsonObject.class));

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
        storage.setDao(new HttpDaoImpl(null, agent));

        Record foundRecord = storage.findOne(country, builder);

        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"offset\":0,\"limit\":1}}";
        assertEquals(new Gson().fromJson(expected, JsonObject.class), new Gson().fromJson(callBody, JsonObject.class));

        assertEquals(key, foundRecord.getKey());
        assertEquals(body, foundRecord.getBody());
        assertEquals(key2, foundRecord.getKey2());
        assertEquals(key3, foundRecord.getKey3());
        assertEquals(profileKey, foundRecord.getProfileKey());
        assertEquals(rangeKey, foundRecord.getRangeKey());
    }

    @Test
    public void testFindWithEncByMultipleSecrets() throws StorageException {
        Crypto cryptoOther = new CryptoImpl(SecretKeyAccessor.getAccessor("otherpassword").getKey(), environmentId);

        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(profileKey);

        Record recOtherEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        Record recEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encryptedRecOther = JsonUtils.toJsonString(recOtherEnc, cryptoOther);
        String encryptedRec = JsonUtils.toJsonString(recEnc, crypto);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        storage.setDao(new HttpDaoImpl(null, agent));

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
    public void testFindWithoutEncWithEncryptedData() throws StorageException {
        Crypto cryptoWithEnc = new CryptoImpl(SecretKeyAccessor.getAccessor("password").getKey(), environmentId);
        Crypto cryptoWithPT = new CryptoImpl(environmentId);
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(2, 0)
                .profileKeyEq(profileKey);

        Record recWithEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        Record recWithPTEnc = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encryptedRec = JsonUtils.toJsonString(recWithEnc, cryptoWithEnc);
        String encryptedPTRec = JsonUtils.toJsonString(recWithPTEnc, cryptoWithPT);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        storage.setDao(new HttpDaoImpl(null, agent));

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

        FakeHttpAgent agent = new FakeHttpAgent(null);
        storage.setDao(new HttpDaoImpl(null, agent));
        BatchRecord findResult = storage.find(country, builder);
        assertEquals(0, findResult.getRecords().size());
    }

    @Test
    public void testReadNotFound() throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent(null);
        storage.setDao(new HttpDaoImpl(null, agent));
        Record readRecord = storage.read(country, key);
        assertNull(readRecord);

    }

    @Test
    public void testErrorFindOneInsufficientArgs() throws StorageException {
        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(record, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        storage.setDao(new HttpDaoImpl(null, agent));
        assertThrows(IllegalArgumentException.class, () -> storage.find(null, null));
        assertThrows(IllegalArgumentException.class, () -> storage.find(country, null));
    }

    @Test
    public void testInitErrorOnInsufficientArgs() {
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(SecretKeysData::new);
        assertThrows(IllegalArgumentException.class, () -> new StorageImpl(null, null, secretKeyAccessor));
    }

    @Test
    public void testErrorReadInsufficientArgs() {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = null;
        try {
            dao = new HttpDaoImpl(null, agent);
        } catch (StorageServerException ex) {
            assertNull(ex);
        }
        assertNotNull(dao);
        storage.setDao(dao);
        assertThrows(IllegalArgumentException.class, () -> storage.read(null, null));
    }

    @Test
    public void testErrorDeleteInsufficientArgs() {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = null;
        try {
            dao = new HttpDaoImpl(null, agent);
        } catch (StorageServerException ex) {
            assertNull(ex);
        }
        assertNotNull(dao);
        storage.setDao(dao);
        assertThrows(IllegalArgumentException.class, () -> storage.delete(null, null));
    }

    @Test
    public void testErrorMigrateWhenEncryptionOff() throws StorageException {
        StorageImpl storage = new StorageImpl(
                environmentId,
                apiKey,
                null
        );
        assertThrows(StorageException.class, () -> storage.migrate(null, 100));
    }
}
