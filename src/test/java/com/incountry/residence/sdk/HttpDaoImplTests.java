package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.http.FakeHttpAgent;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpDaoImplTests {

    private String secret = "passwordpasswordpasswordpassword";
    private int version = 0;
    private int currentVersion = 0;
    private String fakeEndpoint = "http://fakeEndpoint.localhost:8081";

    private Storage initializeStorage(boolean isKey, boolean encrypt, HttpDaoImpl dao) {
        SecretKeyAccessor secretKeyAccessor = initializeSecretKeyAccessor(isKey);
        Storage storage;
        if (encrypt) {
            storage = StorageImpl.getInstance("envId", secretKeyAccessor, dao);
        } else {
            storage = StorageImpl.getInstance("envId", null, dao);
        }
        return storage;
    }

    private Crypto initCrypto(boolean isKey, boolean encrypt) {
        SecretKeyAccessor secretKeyAccessor = initializeSecretKeyAccessor(isKey);
        Crypto crypto;
        if (encrypt) {
            crypto = new CryptoImpl(secretKeyAccessor.getSecretsData(), "envId");
        } else {
            crypto = new CryptoImpl("envId");
        }
        return crypto;
    }


    private SecretKeyAccessor initializeSecretKeyAccessor(boolean isKey) {
        SecretKey secretKey = new SecretKey(secret, version, isKey);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, currentVersion);
        return SecretKeyAccessor.getAccessor(() -> secretsData);
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
        FakeHttpAgent agent = new FakeHttpAgent("");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, agent));
        String expectedPath = "/v2/storage/records/" + country;

        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        storage.write(country, record);

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
        checkEmptyHttpFields(received, record);
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

        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        Crypto crypto = initCrypto(isKey, encrypt);
        String keyHash = crypto.createKeyHash(key);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;

        FakeHttpAgent agent = new FakeHttpAgent(JsonUtils.toJsonString(record, crypto));
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, agent));

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

        FakeHttpAgent agent = new FakeHttpAgent("");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, agent));
        storage.delete(country, key);
        Crypto crypto = initCrypto(isKey, encrypt);
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

        FakeHttpAgent agent = new FakeHttpAgent("");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, agent));

        List<Record> records = new ArrayList<>();
        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        records.add(record);
        storage.batchWrite(country, records);

        String encryptedHttpBody = agent.getCallBody();
        Crypto crypto = initCrypto(isKey, encrypt);
        String keyHash = crypto.createKeyHash(key);
        JsonArray responseList = new Gson().fromJson(encryptedHttpBody, JsonObject.class).getAsJsonArray("records");
        for (JsonElement oneJsonRecord : responseList) {
            String keyFromResponse = ((JsonObject) oneJsonRecord).get("key").getAsString();
            String encryptedBody = ((JsonObject) oneJsonRecord).get("body").getAsString();
            String actualBodyStr = crypto.decrypt(encryptedBody, 0);
            JsonObject bodyJsonObj = (JsonObject) JsonParser.parseString(actualBodyStr);
            String actualBody = body != null ? bodyJsonObj.get("payload").getAsString() : null;
            assertEquals(keyHash, keyFromResponse);
            assertEquals(body, actualBody);
            checkEmptyHttpFields(oneJsonRecord.toString(), record);
        }
    }

    private void checkEmptyHttpFields(String received, Record record) {
        JsonObject jsonObject = new Gson().fromJson(received, JsonObject.class);
        assertEquals(record.getKey() == null, jsonObject.get("key") == null);
        assertEquals(record.getKey2() == null, jsonObject.get("key2") == null);
        assertEquals(record.getKey3() == null, jsonObject.get("key3") == null);
        assertEquals(record.getRangeKey() == null, jsonObject.get("range_key") == null);
        assertEquals(record.getProfileKey() == null, jsonObject.get("profile_key") == null);
        //key1 & body aren't checked because it's always not null
    }

    @Test
    public void testLoadCountriesInDefaultEndPoint() throws StorageServerException, StorageCryptoException {
        FakeHttpAgent agent = new FakeHttpAgent(countryLoadResponse);
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent));
        Record record = new Record("1", "body");
        agent.setResponse("");
        storage.write("US", record);
        assertEquals("https://us.api.incountry.io/v2/storage/records/us", agent.getCallEndpoint());
        agent.setResponse("");
        storage.write("us", record);
        assertEquals("https://us.api.incountry.io/v2/storage/records/us", agent.getCallEndpoint());
        agent.setResponse("");
        storage.write("RU", record);
        assertEquals("https://ru.api.incountry.io/v2/storage/records/ru", agent.getCallEndpoint());
        agent.setResponse("");
        storage.write("ru", record);
        assertEquals("https://ru.api.incountry.io/v2/storage/records/ru", agent.getCallEndpoint());
        agent.setResponse(countryLoadResponse);
        assertThrows(IllegalArgumentException.class, () -> storage.write("PU", record));
        agent.setResponse(countryLoadResponse);
        assertThrows(IllegalArgumentException.class, () -> storage.write("pu", record));
    }

    private String countryLoadResponse = "{\n" +
            "  \"countries\": [\n" +
            "    {\n" +
            "      \"direct\": true,\n" +
            "      \"id\": \"US\",\n" +
            "      \"latencies\": [\n" +
            "        {\n" +
            "          \"country\": \"IN\",\n" +
            "          \"latency\": 320\n" +
            "        },\n" +
            "        {\n" +
            "          \"country\": \"US\",\n" +
            "          \"latency\": 420\n" +
            "        }\n" +
            "      ],\n" +
            "      \"latitude\": 37.09024,\n" +
            "      \"longitude\": -95.712891,\n" +
            "      \"name\": \"United States\",\n" +
            "      \"region\": \"AMER\",\n" +
            "      \"status\": \"active\",\n" +
            "      \"type\": \"mid\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"direct\": true,\n" +
            "      \"id\": \"RU\",\n" +
            "      \"latencies\": [\n" +
            "        {\n" +
            "          \"country\": \"IN\",\n" +
            "          \"latency\": 320\n" +
            "        },\n" +
            "        {\n" +
            "          \"country\": \"US\",\n" +
            "          \"latency\": 420\n" +
            "        }\n" +
            "      ],\n" +
            "      \"latitude\": 61.52401,\n" +
            "      \"longitude\": 105.318756,\n" +
            "      \"name\": \"Russia\",\n" +
            "      \"region\": \"EMEA\",\n" +
            "      \"status\": \"active\",\n" +
            "      \"type\": \"mid\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"direct\": false,\n" +
            "      \"id\": \"PU\",\n" +
            "      \"latencies\": [],\n" +
            "      \"latitude\": null,\n" +
            "      \"longitude\": null,\n" +
            "      \"name\": \"Peru\",\n" +
            "      \"region\": \"AMER\",\n" +
            "      \"status\": \"active\",\n" +
            "      \"type\": \"mini\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
