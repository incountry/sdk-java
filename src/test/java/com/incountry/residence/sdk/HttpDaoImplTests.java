package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.http.mocks.FakeHttpAgent;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.ApiKeyTokenClient;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpDaoImplTests {

    private String secret = "passwordpasswordpasswordpassword";
    private int version = 0;
    private int currentVersion = 0;
    private String fakeEndpoint = "http://fakeEndpoint.localhost:8081";
    private TokenClient tokenClient = new ApiKeyTokenClient("<apiKey>");

    private Storage initializeStorage(boolean isKey, boolean encrypt, Dao dao) throws StorageClientException, StorageServerException {
        SecretKeyAccessor secretKeyAccessor = initializeSecretKeyAccessor(isKey);
        return StorageImpl.getInstance("envId", encrypt ? secretKeyAccessor : null, dao);
    }

    private CryptoManager initCryptoManager(boolean isKey, boolean encrypt) throws StorageClientException {
        SecretKeyAccessor secretKeyAccessor = initializeSecretKeyAccessor(isKey);
        return new CryptoManager(encrypt ? secretKeyAccessor : null, "envId", null, false);
    }


    private SecretKeyAccessor initializeSecretKeyAccessor(boolean isKey) throws StorageClientException {
        SecretKey secretKey = new SecretKey(secret, version, isKey);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, currentVersion);
        return () -> secretsData;
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
    void writeTest(String country,
                   String key,
                   String body,
                   String key2,
                   String key3,
                   String profileKey,
                   Integer rangeKey,
                   boolean isKey,
                   boolean encrypt) throws StorageException, MalformedURLException {
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));
        String expectedPath = "/v2/storage/records/" + country;

        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        storage.write(country, record);

        String received = agent.getCallBody();
        String callPath = new URL(agent.getCallUrl()).getPath();
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
    void readTest(String country,
                  String key,
                  String body,
                  String key2,
                  String key3,
                  String profileKey,
                  Integer rangeKey,
                  boolean isKey,
                  boolean encrypt) throws StorageException, MalformedURLException {

        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        CryptoManager cryptoManager = initCryptoManager(isKey, encrypt);
        String keyHash = cryptoManager.createKeyHash(key);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;

        FakeHttpAgent agent = new FakeHttpAgent(JsonUtils.toJsonString(record, cryptoManager));
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));

        Record fetched = storage.read(country, key);
        assertEquals(expectedPath, new URL(agent.getCallUrl()).getPath());
        assertEquals(key, fetched.getKey());
        assertEquals(body, fetched.getBody());
        assertEquals(profileKey, fetched.getProfileKey());
        assertEquals(key2, fetched.getKey2());
        assertEquals(key3, fetched.getKey3());
        assertEquals(rangeKey, fetched.getRangeKey());
    }

    @ParameterizedTest()
    @MethodSource("recordArgs")
    void deleteTest(String country,
                    String key,
                    String body,
                    String key2,
                    String key3,
                    String profileKey,
                    Integer rangeKey,
                    boolean isKey,
                    boolean encrypt) throws StorageException, IOException {

        FakeHttpAgent agent = new FakeHttpAgent("{}");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));
        storage.delete(country, key);
        CryptoManager cryptoManager = initCryptoManager(false, encrypt);
        String keyHash = cryptoManager.createKeyHash(key);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;
        String callPath = new URL(agent.getCallUrl()).getPath();
        assertEquals(expectedPath, callPath);
    }

    @Test
    void batchWriteNullTest() throws StorageServerException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));
        assertThrows(StorageClientException.class, () -> storage.batchWrite("US", null));
        assertThrows(StorageClientException.class, () -> storage.batchWrite("US", new ArrayList<>()));
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void batchWriteTest(String country,
                        String key,
                        String body,
                        String key2,
                        String key3,
                        String profileKey,
                        Integer rangeKey,
                        boolean isKey,
                        boolean encrypt) throws StorageException {

        FakeHttpAgent agent = new FakeHttpAgent("ok");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));

        List<Record> records = new ArrayList<>();
        Record record = new Record(key, body, profileKey, rangeKey, key2, key3);
        records.add(record);
        storage.batchWrite(country, records);

        String encryptedHttpBody = agent.getCallBody();
        CryptoManager cryptoManager = initCryptoManager(isKey, encrypt);
        String keyHash = cryptoManager.createKeyHash(key);
        JsonArray responseList = new Gson().fromJson(encryptedHttpBody, JsonObject.class).getAsJsonArray("records");
        for (JsonElement oneJsonRecord : responseList) {
            String keyFromResponse = ((JsonObject) oneJsonRecord).get("key").getAsString();
            String encryptedBody = ((JsonObject) oneJsonRecord).get("body").getAsString();
            String actualBodyStr = cryptoManager.decrypt(encryptedBody, 0);
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
    void testWritePopApiResponse() throws StorageClientException, StorageServerException, StorageCryptoException {
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList("ok", "Ok", "OK", "okokok", null));
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));
        String country = "US";
        Record record = new Record("key", "body");
        Record resRecord = storage.write(country, record); //ok
        assertNotNull(resRecord);
        resRecord = storage.write(country, record); //Ok
        assertNotNull(resRecord);
        resRecord = storage.write(country, record); //OK
        assertNotNull(resRecord);
        resRecord = storage.write(country, record); //okokok
        assertNotNull(resRecord);
        resRecord = storage.write(country, record); //null
        assertNotNull(resRecord);
    }

    @Test
    void testBatchWritePopApiResponse() throws StorageClientException, StorageServerException, StorageCryptoException {
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList("ok", "Ok", "OK", "okokok", null));
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));
        String country = "US";
        List<Record> list = Collections.singletonList(new Record("key", "body"));
        BatchRecord batchRecord = storage.batchWrite(country, list); //ok
        assertNotNull(batchRecord);
        batchRecord = storage.batchWrite(country, list); //Ok
        assertNotNull(batchRecord);
        batchRecord = storage.batchWrite(country, list); //OK
        assertNotNull(batchRecord);
        batchRecord = storage.batchWrite(country, list); //OKokok
        assertNotNull(batchRecord);
        batchRecord = storage.batchWrite(country, list); //null
        assertNotNull(batchRecord);
    }

    @Test
    void testReadPopApiResponse() throws StorageClientException, StorageServerException, StorageCryptoException {
        String goodReRespPTE = "{\n" +
                "  \"version\": 0,\n" +
                "  \"key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
                "}";

        String goodRespEnc = "{\n" +
                "  \"version\": 0,\n" +
                "  \"key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
                "  \"body\": \"2:9hWd62kupxyqWXeyORiLqzyJ/TY1F3iN5tpMzI+R41kg4m2SD/QVMRqV/4q1ROUE9UZG0TGSWk61bPcshNbnB0RsEvR2dNZW07oaXo/YvWOfWTa4WyJJdzljxxHuBg5q81ItZ9y84LV7uTzKmqWtKQmT9w==\"\n" +
                "}";
        String notJson = "StringNotJson";
        String wrongJson = "{\"FirstName\":\"<first name>\"}";
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(goodReRespPTE, goodRespEnc, null, notJson, wrongJson));
        Storage storage = initializeStorage(true, true, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));
        String country = "US";
        String someKey = "key1";
        Record recordPte = storage.read(country, someKey);
        assertNotNull(recordPte);

        Record recordEnc = storage.read(country, someKey);
        assertNotNull(recordEnc);
        assertNotEquals(recordEnc, recordPte);

        Record nullRecord = storage.read(country, someKey);
        assertNull(nullRecord);

        StorageServerException ex1 = assertThrows(StorageServerException.class, () -> storage.read(country, someKey));
        assertEquals("Response error", ex1.getMessage());
        assertEquals("java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $", ex1.getCause().getMessage());

        StorageServerException ex2 = assertThrows(StorageServerException.class, () -> storage.read(country, someKey));
        assertEquals("Null required record fields: key, body", ex2.getMessage());
    }

    @Test
    void testSearchPopApiResponse() throws StorageClientException, StorageServerException, StorageCryptoException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .profileKeyEq("profileKey");
        Record rec = new Record("key", "body", "profileKey", null, null, null);
        String encrypted = JsonUtils.toJsonString(rec, initCryptoManager(false, false));
        String goodResponse = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(goodResponse, "StringNotJson"));
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(fakeEndpoint, agent, tokenClient));
        String country = "US";
        BatchRecord batchRecord = storage.find(country, builder);
        assertNotNull(batchRecord);
        assertTrue(batchRecord.getRecords().size() > 0);
        assertThrows(StorageServerException.class, () -> storage.find(country, builder));
    }

    @Test
    void testLoadCountriesPopApiResponse() throws StorageServerException {
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(countryLoadResponse, "StringNotJson"));
        Dao dao = new HttpDaoImpl(null, agent, tokenClient);
        assertNotNull(dao);
        assertThrows(StorageServerException.class, () -> new HttpDaoImpl(null, agent, tokenClient));
    }

    @RepeatedTest(3)
    void testLoadCountriesInDefaultEndPoint(RepetitionInfo repeatInfo) throws StorageServerException, StorageCryptoException, StorageClientException {
        iterateLogLevel(repeatInfo, HttpDaoImpl.class);
        FakeHttpAgent agent = new FakeHttpAgent(countryLoadResponse);
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        Record record = new Record("1", "body");
        agent.setResponse("OK");
        storage.write("US", record);
        assertEquals("https://us.api.incountry.io/v2/storage/records/us", agent.getCallUrl());
        agent.setResponse("OK");
        storage.write("us", record);
        assertEquals("https://us.api.incountry.io/v2/storage/records/us", agent.getCallUrl());
        agent.setResponse("OK");
        storage.write("RU", record);
        assertEquals("https://ru.api.incountry.io/v2/storage/records/ru", agent.getCallUrl());
        agent.setResponse("OK");
        storage.write("ru", record);
        assertEquals("https://ru.api.incountry.io/v2/storage/records/ru", agent.getCallUrl());
        agent.setResponse(countryLoadResponse);
        assertThrows(StorageClientException.class, () -> storage.write("AE", record));
        agent.setResponse(countryLoadResponse);
        assertThrows(StorageClientException.class, () -> storage.write("ae", record));
        agent.setResponse(countryLoadResponse);
        assertThrows(StorageClientException.class, () -> storage.write("Other", record));
    }

    @Test
    void testLoadCountriesNegative() {
        FakeHttpAgent agent = new FakeHttpAgent("[]");
        StorageServerException ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country list is empty", ex.getMessage());
        agent.setResponse("");
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country list is empty", ex.getMessage());
        agent.setResponse("[{}]");
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country id is empty: TransferPop{id='null', name='null', type='null'}", ex.getMessage());
        agent.setResponse("[{'id'=''}]");
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country id is empty: TransferPop{id='', name='null', type='null'}", ex.getMessage());
        agent.setResponse("[{'id'='123'}]");
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country name is empty: TransferPop{id='123', name='null', type='null'}", ex.getMessage());
        agent.setResponse("[{'id'='123','name'=''}]");
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country name is empty: TransferPop{id='123', name='', type='null'}", ex.getMessage());
        agent.setResponse("[{'id'='123','name'='456'}]");
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country type is invalid: TransferPop{id='123', name='456', type='null'}", ex.getMessage());
        agent.setResponse("[{'id'='123','name'='456', 'type'=''}]");
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country type is invalid: TransferPop{id='123', name='456', type=''}", ex.getMessage());
        agent.setResponse("[{'id'='123','name'='456', 'type'='super'}]");
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(HttpDaoImpl.DEFAULT_ENDPOINT, agent, tokenClient));
        assertEquals("Response error: country type is invalid: TransferPop{id='123', name='456', type='super'}", ex.getMessage());
    }

    @Test
    void popTest() {
        String name = "us";
        String host = "http://localhost";
        POP pop = new POP(host, name);
        assertEquals(name, pop.getName());
        assertEquals(host, pop.getHost());
        assertEquals("PoP{host='" + host + "', name='" + name + "'}", pop.toString());
    }

    @Test
    void testHttpDaoWithoutCrypto() throws StorageServerException, StorageClientException, StorageCryptoException {
        String readResponse = null;
        String deleteResponse = "{}";
        String createResponse = "OK";
        String createResponseBad = "Not OK!";
        String createResponseNull = null;
        FakeHttpAgent httpAgent = new FakeHttpAgent(Arrays.asList(
                readResponse, deleteResponse, createResponse, createResponseBad, createResponseNull));
        HttpDaoImpl dao = new HttpDaoImpl(fakeEndpoint, httpAgent, tokenClient);
        String country = "US";
        String key = "key1";
        assertNull(dao.read(country, key, null));
        dao.delete(country, key, null);
        dao.createRecord(country, new Record(key, "<body>"), null);
        dao.createRecord(country, new Record(key, "<body>"), null);
        dao.createRecord(country, new Record(key, "<body>"), null);
    }

    private String countryLoadResponse = "[\n" +
            "  {\n" +
            "    \"id\": \"AE\",\n" +
            "    \"name\": \"ae\",\n" +
            "    \"type\": \"mini\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"RU\",\n" +
            "    \"name\": \"ru\",\n" +
            "    \"type\": \"mid\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"US\",\n" +
            "    \"name\": \"us\",\n" +
            "    \"type\": \"mid\"\n" +
            "  }\n" +
            "]";
}
