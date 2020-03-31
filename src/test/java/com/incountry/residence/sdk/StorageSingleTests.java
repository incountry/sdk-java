package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import com.incountry.residence.sdk.dto.search.FindOptions;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StorageSingleTests {
    private StorageImpl storage;
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
        SecretKeysData secretKeysData = new SecretKeysData();
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(version);
        secretKey.setIsKey(true);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
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
        Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        String content = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        FakeHttpAgent agent = new FakeHttpAgent(content);
        storage.setHttpAgent(agent);
        BatchRecord batchRecord = JsonUtils.batchRecordFromString(content, crypto);

        int migratedRecords = batchRecord.getCount();
        int totalLeft = batchRecord.getTotal() - batchRecord.getCount();
        MigrateResult migrateResult = storage.migrate("us", 2);

        assertEquals(migratedRecords, migrateResult.getMigrated());
        assertEquals(totalLeft, migrateResult.getTotalLeft());
    }

    @Test
    public void findTest() throws StorageException {
        FindOptions options = new FindOptions(1, 0);
        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(profileKey));
        Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        storage.setHttpAgent(agent);
        BatchRecord batchRecord = storage.find(country, filter, options);

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
        storage.setHttpAgent(agent);
        Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        storage.create(record);

        String expectedURL = endpoint + "/v2/storage/records/" + country;

        String realURL = new URL(agent.getCallEndpoint()).toString();

        assertEquals(expectedURL, realURL);
    }

    @Test
    public void testFindWithEnc() throws StorageException {
        FindOptions options = new FindOptions(1, 0);
        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(profileKey));

        Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        storage.setHttpAgent(agent);

        BatchRecord d = storage.find(country, filter, options);

        String callBody = agent.getCallBody();
        assertEquals("{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"offset\":0,\"limit\":1}}", callBody);

        assertEquals(1, d.getCount());
        assertEquals(1, d.getRecords().size());
        assertEquals(key, d.getRecords().get(0).getKey());
        assertEquals(body, d.getRecords().get(0).getBody());
        assertEquals(key2, d.getRecords().get(0).getKey2());
        assertEquals(key3, d.getRecords().get(0).getKey3());
        assertEquals(profileKey, d.getRecords().get(0).getProfileKey());
        assertEquals(rangeKey, d.getRecords().get(0).getRangeKey());
    }

    @Test
    public void testFindOne() throws StorageException {
        FindOptions options = new FindOptions(1, 0);
        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(profileKey));

        Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(rec, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        storage.setHttpAgent(agent);

        Record foundRecord = storage.findOne(country, filter, options);

        String callBody = agent.getCallBody();
        assertEquals("{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"offset\":0,\"limit\":1}}", callBody);

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

        FindOptions options = new FindOptions(2, 0);
        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(profileKey));

        Record recOtherEnc = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        Record recEnc = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encryptedRecOther = JsonUtils.toJsonString(recOtherEnc, cryptoOther);
        String encryptedRec = JsonUtils.toJsonString(recEnc, crypto);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        storage.setHttpAgent(agent);

        BatchRecord d = storage.find(country, filter, options);

        assertEquals(1, d.getErrors().size());
        assertEquals(encryptedRecOther, d.getErrors().get(0).getRawData());
        assertEquals("Record Parse Exception", d.getErrors().get(0).getMessage());

        assertEquals(1, d.getRecords().size());
        assertEquals(key, d.getRecords().get(0).getKey());
        assertEquals(body, d.getRecords().get(0).getBody());
        assertEquals(key2, d.getRecords().get(0).getKey2());
        assertEquals(key3, d.getRecords().get(0).getKey3());
        assertEquals(profileKey, d.getRecords().get(0).getProfileKey());
        assertEquals(rangeKey, d.getRecords().get(0).getRangeKey());
    }

    @Test
    public void testFindWithoutEncWithEncryptedData() throws StorageException {
        Crypto cryptoWithEnc = new CryptoImpl(SecretKeyAccessor.getAccessor("password").getKey(), environmentId);
        Crypto cryptoWithPT = new CryptoImpl(environmentId);

        FindOptions options = new FindOptions(2, 0);
        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(profileKey));

        Record reсWithEnc = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        Record recWithPTEnc = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encryptedRec = JsonUtils.toJsonString(reсWithEnc, cryptoWithEnc);
        String encryptedPTRec = JsonUtils.toJsonString(recWithPTEnc, cryptoWithPT);

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        storage.setHttpAgent(agent);

        BatchRecord d = storage.find(country, filter, options);

        assertEquals(1, d.getErrors().size());
        assertEquals(encryptedRec, d.getErrors().get(0).getRawData());
        assertEquals("Record Parse Exception", d.getErrors().get(0).getMessage());

        assertEquals(1, d.getRecords().size());
        assertEquals(key, d.getRecords().get(0).getKey());
        assertEquals(body, d.getRecords().get(0).getBody());
        assertEquals(key2, d.getRecords().get(0).getKey2());
        assertEquals(key3, d.getRecords().get(0).getKey3());
        assertEquals(profileKey, d.getRecords().get(0).getProfileKey());
        assertEquals(rangeKey, d.getRecords().get(0).getRangeKey());
    }

    @Test
    public void testFindIncorrectRecords() throws StorageException {
        FindOptions options = new FindOptions(2, 0);
        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(profileKey));

        FakeHttpAgent agent = new FakeHttpAgent(null);
        storage.setHttpAgent(agent);
        BatchRecord findResult = storage.find(country, filter, options);

        assertNull(findResult);
    }

    @Test
    public void testReadNotFound() throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent(null);
        storage.setHttpAgent(agent);
        Record readRecord = storage.read(country, key);
        assertNull(readRecord);

    }

    @Test
    public void testErrorFindOneInsufficientArgs() throws StorageException {
        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(profileKey));

        Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encrypted = JsonUtils.toJsonString(record, crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        storage.setHttpAgent(agent);
        assertThrows(IllegalArgumentException.class, () -> storage.find(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> storage.find(country, null, null));
        assertThrows(IllegalArgumentException.class, () -> storage.find(country, filter, null));
    }

    @Test
    public void testInitErrorOnInsufficientArgs() {
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(() -> new SecretKeysData());
        assertThrows(IllegalArgumentException.class, () -> new StorageImpl(null, null, secretKeyAccessor));
    }

    @Test
    public void testErrorReadInsufficientArgs() {
        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setHttpAgent(agent);
        assertThrows(IllegalArgumentException.class, () -> storage.read(null, null));
    }

    @Test
    public void testErrorDeleteInsufficientArgs() {
        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setHttpAgent(agent);
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
