package com.incountry;

import com.google.gson.*;
import com.incountry.crypto.Crypto;
import com.incountry.crypto.impl.CryptoImpl;
import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StorageTest {

    private Storage storage;
    private Crypto crypto;

    private String secret = "passwordpasswordpasswordpassword";
    private int version = 0;
    private boolean isKey = true;
    private int currentVersion = 0;

    @BeforeEach
    public void init() throws StorageServerException {
        SecretKeyAccessor secretKeyAccessor = initializeSecretKeyAccessor();
        storage = new Storage(
                "envId",
                "apiKey",
                secretKeyAccessor
        );
        crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");
    }

    private SecretKeyAccessor initializeSecretKeyAccessor() {
        SecretKeysData secretKeysData = new SecretKeysData();
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(version);
        secretKey.setIsKey(isKey);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        secretKeysData.setSecrets(secretKeyList);
        secretKeysData.setCurrentVersion(currentVersion);

        return SecretKeyAccessor.getAccessor( () -> secretKeysData);
    }

    private static Stream<Arguments> recordArgs() {
        return Stream.of(
                Arguments.of("us", "key1", null, null, null, null, null),
                Arguments.of("us", "key1", "body", null, null, null, null),
                Arguments.of("us", "key1", "body", "key2", null, null, null),
                Arguments.of("us", "key1", "body", "key2", "key3", null, null),
                Arguments.of("us", "key1", "body", "key2", "key3", "profileKey", null),
                Arguments.of("us", "key1", "body", "key2", "key3", "profileKey", 1)
        );
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    public void writeTest(String country, String key, String body, String key2, String key3, String profileKey, Integer rangeKey) throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setHttpAgent(agent);
        Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        storage.write(record);

        String encrypted = agent.getCallBody();
        String keyHash = crypto.createKeyHash(key);
        JSONObject response = new JSONObject(encrypted);
        String responseKey = response.getString("key");
        String encryptedBody = response.getString("body");
        String actualBodyStr = crypto.decrypt(encryptedBody, 0);
        JSONObject bodyJsonObj = new JSONObject(actualBodyStr);
        String actualBody = body != null ? bodyJsonObj.getString("payload") : null;

        assertEquals(keyHash, responseKey);
        assertEquals(body, actualBody);
    }


    @ParameterizedTest
    @MethodSource("recordArgs")
    public void readTest(String country, String key, String body, String key2, String key3, String profileKey, Integer rangeKey) throws StorageException {
        Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        FakeHttpAgent agent = new FakeHttpAgent(record.toJsonString(crypto));
        storage.setHttpAgent(agent);
        Record fetched = storage.read(country, key);

        assertEquals(key, fetched.getKey());
        assertEquals(body, fetched.getBody());
        assertEquals(profileKey, fetched.getProfileKey());
        assertEquals(key2, fetched.getKey2());
        assertEquals(key3, fetched.getKey3());
        assertEquals(rangeKey, fetched.getRangeKey());
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    public void deleteTest(String country, String key) throws StorageException, IOException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setHttpAgent(agent);
        storage.delete(country, key);

        String keyHash = crypto.createKeyHash(key);
        String expectedPath = "/v2/storage/records/"+ country + "/" + keyHash;

        String callPath = new URL(agent.getCallEndpoint()).getPath();

        assertEquals(expectedPath, callPath);
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    public void batchWriteTest(String country, String key, String body, String key2, String key3, String profileKey, Integer rangeKey) throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setHttpAgent(agent);
        List<Record> records = new ArrayList<>();
        records.add(new Record(country, key, body, profileKey, rangeKey, key2, key3));
        storage.batchWrite(country, records);

        String encrypted = agent.getCallBody();
        String keyHash = crypto.createKeyHash(key);
        JsonArray responseList = new Gson().fromJson(encrypted, JsonObject.class).getAsJsonArray("records");
        for (JsonElement response : responseList) {
            String keyFromResponse = ((JsonObject)response).get("key").getAsString();
            String encryptedBody = ((JsonObject)response).get("body").getAsString();
            String actualBodyStr = crypto.decrypt(encryptedBody, 0);
            JsonObject bodyJsonObj = (JsonObject)JsonParser.parseString(actualBodyStr);
            String actualBody = body != null ? bodyJsonObj.get("payload").getAsString() : null;

            assertEquals(keyHash, keyFromResponse);
            assertEquals(body, actualBody);
        }
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    public void migrateTest(String country, String key, String body, String key2, String key3, String profileKey, Integer rangeKey) throws StorageException {
        Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encrypted = rec.toJsonString(crypto);
        String content = "{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        FakeHttpAgent agent = new FakeHttpAgent(content);
        storage.setHttpAgent(agent);
        BatchRecord batchRecord = BatchRecord.fromString(content, crypto);

        int migratedRecords = batchRecord.getCount();
        int totalLeft =  batchRecord.getTotal() - batchRecord.getCount();
        MigrateResult migrateResult = storage.migrate("us", 2);

        assertEquals(migratedRecords, migrateResult.getMigrated());
        assertEquals(totalLeft, migrateResult.getTotalLeft());
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    public void findTest(String country, String key, String body, String key2, String key3, String profileKey, Integer rangeKey) throws StorageException {
        FindOptions options = new FindOptions(1,0);
        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(profileKey));
        Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String encrypted = rec.toJsonString(crypto);
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        storage.setHttpAgent(agent);
        BatchRecord batchRecord = storage.find(country, filter, options);

        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(key, batchRecord.getRecords().get(0).getKey());
        assertEquals(body, batchRecord.getRecords().get(0).getBody());
    }
}
