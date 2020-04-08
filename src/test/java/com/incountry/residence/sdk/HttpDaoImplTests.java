package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class HttpDaoImplTests {

    private Crypto crypto;

    private String secret = "passwordpasswordpasswordpassword";
    private int version = 0;
    private int currentVersion = 0;

    public StorageImpl initializeStorage(boolean isKey, boolean encrypt) throws StorageServerException {
        SecretKeyAccessor secretKeyAccessor = initializeSecretKeyAccessor(isKey);
        StorageImpl storage;
        if (encrypt) {
            storage = new StorageImpl(
                    "envId",
                    "apiKey",
                    secretKeyAccessor
            );
            crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");
        } else {
            storage = new StorageImpl("envId", "apiKey", null);
            crypto = new CryptoImpl("envId");
        }
        return storage;
    }

    private SecretKeyAccessor initializeSecretKeyAccessor(boolean isKey) {
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(version);
        secretKey.setIsKey(isKey);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretKeysData secretKeysData = new SecretKeysData();
        secretKeysData.setSecrets(secretKeyList);
        secretKeysData.setCurrentVersion(currentVersion);

        return SecretKeyAccessor.getAccessor(() -> secretKeysData);
    }

    private static Stream<Arguments> recordArgs() {
        return Stream.of(
                Arguments.of("us", "key1", null, null, null, null, null, true, true),
                Arguments.of("us", "key1", "body", null, null, null, null, true, true),
                Arguments.of("us", "key1", "body", "key2", null, null, null, true, true),
                Arguments.of("us", "key1", "body", "key2", "key3", null, null, true, false),
                Arguments.of("us", "key1", "body", "key2", "key3", "profileKey", null, true, false),
                Arguments.of("us", "key1", "body", "key2", "key3", "profileKey", 1, true, false),

                Arguments.of("us", "key1", null, null, null, null, null, false, false),
                Arguments.of("us", "key1", "body", null, null, null, null, false, false),
                Arguments.of("us", "key1", "body", "key2", null, null, null, false, false),
                Arguments.of("us", "key1", "body", "key2", "key3", null, null, false, true),
                Arguments.of("us", "key1", "body", "key2", "key3", "profileKey", null, false, true),
                Arguments.of("us", "key1", "body", "key2", "key3", "profileKey", 1, false, true)
        );
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    public void writeTest(String country,
                          String key,
                          String body,
                          String key2,
                          String key3,
                          String profileKey,
                          Integer rangeKey,
                          boolean isKey,
                          boolean encrypt) throws StorageException, MalformedURLException {
        StorageImpl storage = initializeStorage(isKey, encrypt);

        String expectedPath = "/v2/storage/records/" + country;

        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setDao(new HttpDaoImpl(null, agent));
        Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        storage.create(record);

        String received = agent.getCallBody();
        String callPath = new URL(agent.getCallEndpoint()).getPath();
        Record receivedRecord = JsonUtils.recordFromString(received, null);

        assertEquals(expectedPath, callPath);
        assertNotEquals(key, receivedRecord.getKey());
        if (key2 != null) {
            assertNotEquals(key2, receivedRecord.getKey2());
        }
        if (key3 != null) {
            assertNotEquals(key3, receivedRecord.getKey3());
        }
        if (profileKey != null) {
            assertNotEquals(profileKey, receivedRecord.getProfileKey());
        }
        if (body != null) {
            assertNotEquals(body, receivedRecord.getBody());
        }
        if (rangeKey != null) {
            assertEquals(rangeKey, receivedRecord.getRangeKey());
        }

    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    public void readTest(String country,
                         String key,
                         String body,
                         String key2,
                         String key3,
                         String profileKey,
                         Integer rangeKey,
                         boolean isKey,
                         boolean encrypt) throws StorageException, MalformedURLException {

        StorageImpl storage = initializeStorage(isKey, encrypt);

        Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
        String keyHash = crypto.createKeyHash(key);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;

        FakeHttpAgent agent = new FakeHttpAgent(JsonUtils.toJsonString(record, crypto));
        storage.setDao(new HttpDaoImpl(null, agent));

        Record fetched = storage.read(country, key);
        assertEquals(expectedPath, new URL(agent.getCallEndpoint()).getPath());
        assertEquals(key, fetched.getKey());
        assertEquals(body, fetched.getBody());
        assertEquals(profileKey, fetched.getProfileKey());
        assertEquals(key2, fetched.getKey2());
        assertEquals(key3, fetched.getKey3());
        assertEquals(rangeKey, fetched.getRangeKey());
    }

    @ParameterizedTest()
    @MethodSource("recordArgs")
    public void deleteTest(String country,
                           String key,
                           String body,
                           String key2,
                           String key3,
                           String profileKey,
                           Integer rangeKey,
                           boolean isKey,
                           boolean encrypt) throws StorageException, IOException {

        StorageImpl storage = initializeStorage(isKey, encrypt);
        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setDao(new HttpDaoImpl(null, agent));
        storage.delete(country, key);
        String keyHash = crypto.createKeyHash(key);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;
        String callPath = new URL(agent.getCallEndpoint()).getPath();
        assertEquals(expectedPath, callPath);
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    public void batchWriteTest(String country,
                               String key,
                               String body,
                               String key2,
                               String key3,
                               String profileKey,
                               Integer rangeKey,
                               boolean isKey,
                               boolean encrypt) throws StorageException {

        StorageImpl storage = initializeStorage(isKey, encrypt);
        FakeHttpAgent agent = new FakeHttpAgent("");
        storage.setDao(new HttpDaoImpl(null, agent));
        List<Record> records = new ArrayList<>();
        records.add(new Record(country, key, body, profileKey, rangeKey, key2, key3));
        storage.createBatch(country, records);

        String encrypted = agent.getCallBody();
        String keyHash = crypto.createKeyHash(key);
        JsonArray responseList = new Gson().fromJson(encrypted, JsonObject.class).getAsJsonArray("records");
        for (JsonElement response : responseList) {
            String keyFromResponse = ((JsonObject) response).get("key").getAsString();
            String encryptedBody = ((JsonObject) response).get("body").getAsString();
            String actualBodyStr = crypto.decrypt(encryptedBody, 0);
            JsonObject bodyJsonObj = (JsonObject) JsonParser.parseString(actualBodyStr);
            String actualBody = body != null ? bodyJsonObj.get("payload").getAsString() : null;

            assertEquals(keyHash, keyFromResponse);
            assertEquals(body, actualBody);
        }
    }
}
