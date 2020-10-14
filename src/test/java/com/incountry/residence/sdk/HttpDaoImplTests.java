package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.StringField;
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
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.models.MetaInfoTypes;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private Storage initializeStorage(boolean isKey, boolean encrypt, HttpDaoImpl dao) throws StorageClientException, StorageServerException {
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
                Arguments.of("us", "someRecordKey", null, null, null, null, null, true, true),
                Arguments.of("us", "someRecordKey", "body", null, null, null, null, true, true),
                Arguments.of("us", "someRecordKey", "body", "key2", null, null, null, true, true),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", null, null, true, false),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", "profileKey", null, true, false),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", "profileKey", 1L, true, false),

                Arguments.of("us", "someRecordKey", null, null, null, null, null, false, false),
                Arguments.of("us", "someRecordKey", "body", null, null, null, null, false, false),
                Arguments.of("us", "someRecordKey", "body", "key2", null, null, null, false, false),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", null, null, false, true),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", "profileKey", null, false, true),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", "profileKey", 1L, false, true)
        );
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void writeTest(String country,
                   String recordKey,
                   String body,
                   String key2,
                   String key3,
                   String profileKey,
                   Long rangeKey1,
                   boolean isKey,
                   boolean encrypt) throws StorageException, MalformedURLException {
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        String expectedPath = "/v2/storage/records/" + country;

        Record record = new Record(recordKey, body)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        storage.write(country, record);

        String received = agent.getCallBody();
        String callPath = new URL(agent.getCallUrl()).getPath();
        Record receivedRecord = JsonUtils.recordFromString(received, null);

        assertEquals(expectedPath, callPath);
        assertNotEquals(recordKey, receivedRecord.getRecordKey());
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
        if (rangeKey1 != null) {
            assertEquals(rangeKey1, receivedRecord.getRangeKey1());
        }
        checkEmptyHttpFields(received, record);
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void readTest(String country,
                  String recordKey,
                  String body,
                  String key2,
                  String key3,
                  String profileKey,
                  Long rangeKey1,
                  boolean isKey,
                  boolean encrypt) throws StorageException, MalformedURLException {

        Record record = new Record(recordKey, body)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        CryptoManager cryptoManager = initCryptoManager(isKey, encrypt);
        String keyHash = cryptoManager.createKeyHash(recordKey);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;

        FakeHttpAgent agent = new FakeHttpAgent(JsonUtils.toJsonString(record, cryptoManager));
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, null, null, agent));

        Record fetched = storage.read(country, recordKey);
        assertEquals(expectedPath, new URL(agent.getCallUrl()).getPath());
        assertEquals(recordKey, fetched.getRecordKey());
        assertEquals(body, fetched.getBody());
        assertEquals(profileKey, fetched.getProfileKey());
        assertEquals(key2, fetched.getKey2());
        assertEquals(key3, fetched.getKey3());
        assertEquals(rangeKey1, fetched.getRangeKey1());
    }

    @ParameterizedTest()
    @MethodSource("recordArgs")
    void deleteTest(String country,
                    String recordKey,
                    String body,
                    String key2,
                    String key3,
                    String profileKey,
                    Long rangeKey1,
                    boolean isKey,
                    boolean encrypt) throws StorageException, IOException {

        FakeHttpAgent agent = new FakeHttpAgent("{}");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        storage.delete(country, recordKey);
        CryptoManager cryptoManager = initCryptoManager(false, encrypt);
        String keyHash = cryptoManager.createKeyHash(recordKey);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;
        String callPath = new URL(agent.getCallUrl()).getPath();
        assertEquals(expectedPath, callPath);
    }

    @Test
    void batchWriteNullTest() throws StorageServerException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> storage.batchWrite("US", null));
        assertEquals("Can't write empty batch", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> storage.batchWrite("US", new ArrayList<>()));
        assertEquals("Can't write empty batch", ex2.getMessage());
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void batchWriteTest(String country,
                        String recordKey,
                        String body,
                        String key2,
                        String key3,
                        String profileKey,
                        Long rangeKey1,
                        boolean isKey,
                        boolean encrypt) throws StorageException {

        FakeHttpAgent agent = new FakeHttpAgent("ok");
        Storage storage = initializeStorage(isKey, encrypt, new HttpDaoImpl(fakeEndpoint, null, null, agent));

        List<Record> records = new ArrayList<>();
        Record record = new Record(recordKey, body)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        records.add(record);
        storage.batchWrite(country, records);

        String encryptedHttpBody = agent.getCallBody();
        CryptoManager cryptoManager = initCryptoManager(isKey, encrypt);
        String keyHash = cryptoManager.createKeyHash(recordKey);
        JsonArray responseList = new Gson().fromJson(encryptedHttpBody, JsonObject.class).getAsJsonArray("records");
        for (JsonElement oneJsonRecord : responseList) {
            String keyFromResponse = ((JsonObject) oneJsonRecord).get("record_key").getAsString();
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
        assertEquals(record.getRecordKey() == null, jsonObject.get("record_key") == null);
        assertEquals(record.getKey2() == null, jsonObject.get("key2") == null);
        assertEquals(record.getKey3() == null, jsonObject.get("key3") == null);
        assertEquals(record.getRangeKey1() == null, jsonObject.get("range_key1") == null);
        assertEquals(record.getProfileKey() == null, jsonObject.get("profile_key") == null);
        //recordKey & body aren't checked because it's always not null
    }

    @Test
    void testWritePopApiResponse() throws StorageClientException, StorageServerException, StorageCryptoException {
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList("ok", "Ok", "OK", "okokok", null));
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
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
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
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
                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
                "}";

        String goodRespEnc = "{\n" +
                "  \"version\": 0,\n" +
                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
                "  \"body\": \"2:9hWd62kupxyqWXeyORiLqzyJ/TY1F3iN5tpMzI+R41kg4m2SD/QVMRqV/4q1ROUE9UZG0TGSWk61bPcshNbnB0RsEvR2dNZW07oaXo/YvWOfWTa4WyJJdzljxxHuBg5q81ItZ9y84LV7uTzKmqWtKQmT9w==\"\n" +
                "}";
        String notJson = "StringNotJson";
        String wrongJson = "{\"FirstName\":\"<first name>\"}";
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(goodReRespPTE, goodRespEnc, null, notJson, wrongJson));
        Storage storage = initializeStorage(true, true, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        String country = "US";
        String someKey = "someRecordKey";
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
        assertEquals("Null required record fields: recordKey, body", ex2.getMessage());
    }

    @Test
    void testSearchPopApiResponse() throws StorageClientException, StorageServerException, StorageCryptoException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .limitAndOffset(1, 0)
                .keyEq(StringField.PROFILE_KEY, "profileKey");
        Record record = new Record("someRecordKey", "body")
                .setProfileKey("profileKey");
        String encrypted = JsonUtils.toJsonString(record, initCryptoManager(false, false));
        String goodResponse = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(goodResponse, "StringNotJson"));
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        String country = "US";
        BatchRecord batchRecord = storage.find(country, builder);
        assertNotNull(batchRecord);
        assertTrue(batchRecord.getRecords().size() > 0);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storage.find(country, builder));
        assertEquals("Response error", ex.getMessage());
    }

    @Test
    void testLoadCountriesPopApiResponse() throws StorageServerException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(countryLoadResponse,
                "StringNotJson",
                countryLoadBadResponseNullName,
                countryLoadBadResponseEmptyName,
                countryLoadBadResponseNullId,
                countryLoadBadResponseEmptyId,
                countryLoadBadResponseEmptyCountries));
        Dao dao = new HttpDaoImpl(null, null, null, agent);
        assertNotNull(dao);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(null, null, null, agent));
        assertEquals("Response error", ex.getMessage());
        assertEquals("java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $", ex.getCause().getMessage());
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(null, null, null, agent));
        assertEquals("Response error: country name is empty TransferPop{name='null', id='null', status='null', region='null', direct=true}", ex.getMessage());
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(null, null, null, agent));
        assertEquals("Response error: country name is empty TransferPop{name='', id='null', status='null', region='null', direct=true}", ex.getMessage());
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(null, null, null, agent));
        assertEquals("Response error: country id is empty TransferPop{name='USA', id='null', status='null', region='null', direct=false}", ex.getMessage());
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(null, null, null, agent));
        assertEquals("Response error: country id is empty TransferPop{name='USA', id='', status='null', region='null', direct=false}", ex.getMessage());
        ex = assertThrows(StorageServerException.class, () -> new HttpDaoImpl(null, null, null, agent));
        assertEquals("Response error: country list is empty", ex.getMessage());
    }

    @RepeatedTest(3)
    void testLoadCountriesInDefaultEndPoint(RepetitionInfo repeatInfo) throws StorageServerException, StorageCryptoException, StorageClientException {
        iterateLogLevel(repeatInfo, HttpDaoImpl.class);
        FakeHttpAgent agent = new FakeHttpAgent(countryLoadResponse);
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(null, null, "https://localhost:8080", agent));
        Record record = new Record("1", "body");
        agent.setResponse("OK");
        storage.write("US", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/us", agent.getCallUrl());
        agent.setResponse("OK");
        storage.write("us", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/us", agent.getCallUrl());
        agent.setResponse("OK");
        storage.write("RU", record);
        assertEquals("https://ru-mt-01.api.incountry.io/v2/storage/records/ru", agent.getCallUrl());
        agent.setResponse("OK");
        storage.write("ru", record);
        assertEquals("https://ru-mt-01.api.incountry.io/v2/storage/records/ru", agent.getCallUrl());
        //country 'PU' has no separate endpoint
        storage.write("PU", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/pu", agent.getCallUrl());
        agent.setResponse(countryLoadResponse);
        storage.write("pu", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/pu", agent.getCallUrl());
        //country 'SU' is not in country list
        storage.write("SU", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/su", agent.getCallUrl());
        agent.setResponse(countryLoadResponse);
        storage.write("su", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/su", agent.getCallUrl());
    }

    @Test
    void testLoadCountriesInDefaultEndPointWithMask() throws StorageServerException, StorageCryptoException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent(countryLoadResponse);
        Storage storage = initializeStorage(false, false, new HttpDaoImpl(null, "-test-01.debug.org", null, agent));
        Record record = new Record("1", "body");
        agent.setResponse("OK");

        //US is midpop
        storage.write("US", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/us", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org", agent.getAudienceUrl());
        agent.setResponse("OK");
        storage.write("us", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/us", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org", agent.getAudienceUrl());

        //RU is midpop
        agent.setResponse("OK");
        storage.write("RU", record);
        assertEquals("https://ru-test-01.debug.org/v2/storage/records/ru", agent.getCallUrl());
        assertEquals("https://ru-test-01.debug.org", agent.getAudienceUrl());
        agent.setResponse("OK");
        storage.write("ru", record);
        assertEquals("https://ru-test-01.debug.org/v2/storage/records/ru", agent.getCallUrl());
        assertEquals("https://ru-test-01.debug.org", agent.getAudienceUrl());

        //country 'PU' is minipop
        storage.write("PU", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/pu", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org https://pu-test-01.debug.org", agent.getAudienceUrl());
        agent.setResponse(countryLoadResponse);
        storage.write("pu", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/pu", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org https://pu-test-01.debug.org", agent.getAudienceUrl());

        //country 'SU' is not in country list
        storage.write("SU", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/su", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org https://su-test-01.debug.org", agent.getAudienceUrl());
        agent.setResponse(countryLoadResponse);
        storage.write("su", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/su", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org https://su-test-01.debug.org", agent.getAudienceUrl());
    }

    @Test
    void testWriteToCustomEndpointWithMinipop() throws StorageServerException, StorageCryptoException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(false, false, new HttpDaoImpl("https://us.test.org", "test.org", null, agent));
        Record record = new Record("1", "body");
        agent.setResponse("OK");
        storage.write("US", record);
        assertEquals("https://us.test.org/v2/storage/records/us", agent.getCallUrl());
    }

    @Test
    void testWriteToCustomEndpointWithMinipopAndSecondaryUrl() throws StorageServerException, StorageCryptoException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(false, false, new HttpDaoImpl("https://ustest.org", "test.org", null, agent));
        Record record = new Record("1", "body");
        agent.setResponse("OK");
        storage.write("US", record);
        assertEquals("https://ustest.org/v2/storage/records/us", agent.getCallUrl());
    }

    @Test
    void popTest() {
        String name = "us";
        String host = "http://localhost";
        String region = "amer";
        POP pop = new POP(host, name, region);
        assertEquals(name, pop.getName());
        assertEquals(host, pop.getHost());
        assertEquals(region, pop.getRegion(null));
        assertEquals("POP{host='" + host + "', name='" + name + "', region='" + region + "'}", pop.toString());
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
        HttpDaoImpl dao = new HttpDaoImpl(fakeEndpoint, null, null, httpAgent);
        String country = "US";
        String recordKey = "someRecordKey";
        assertNull(dao.read(country, recordKey, null));
        dao.delete(country, recordKey, null);
        dao.createRecord(country, new Record(recordKey, "<body>"), null);
        dao.createRecord(country, new Record(recordKey, "<body>"), null);
        dao.createRecord(country, new Record(recordKey, "<body>"), null);
    }

    @Test
    void testEndPointAndAudience() throws StorageServerException, StorageCryptoException, StorageClientException {
        //storage has only custom endpoint
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(false, false, new HttpDaoImpl("https://custom.io", null, null, agent));
        Record record = new Record("1", "body");
        //AG is minipop
        storage.write("AG", record);
        assertEquals("https://custom.io/v2/storage/records/ag", agent.getCallUrl());
        assertEquals("https://custom.io", agent.getAudienceUrl());

        //storage has only endpoint mask
        agent.setResponse(countryLoadResponse);
        storage = initializeStorage(false, false, new HttpDaoImpl(null, "-custom-01.test.io", null, agent));
        //US is midpop
        agent.setResponse("OK");
        storage.write("US", record);
        assertEquals("https://us-custom-01.test.io/v2/storage/records/us", agent.getCallUrl());
        assertEquals("https://us-custom-01.test.io", agent.getAudienceUrl());
        //RU is midpop
        agent.setResponse("OK");
        storage.write("RU", record);
        assertEquals("https://ru-custom-01.test.io/v2/storage/records/ru", agent.getCallUrl());
        assertEquals("https://ru-custom-01.test.io", agent.getAudienceUrl());
        //AG is minipop
        agent.setResponse("OK");
        storage.write("AG", record);
        assertEquals("https://us-custom-01.test.io/v2/storage/records/ag", agent.getCallUrl());
        assertEquals("https://us-custom-01.test.io https://ag-custom-01.test.io", agent.getAudienceUrl());

        //storage has endpoint and endpoint mask
        storage = initializeStorage(false, false, new HttpDaoImpl("https://super-server.io", "-custom-02.io", null, agent));
        //US is midpop
        agent.setResponse("OK");
        storage.write("US", record);
        assertEquals("https://super-server.io/v2/storage/records/us", agent.getCallUrl());
        assertEquals("https://super-server.io https://us-custom-02.io", agent.getAudienceUrl());
        //RU is midpop
        agent.setResponse("OK");
        storage.write("RU", record);
        assertEquals("https://super-server.io/v2/storage/records/ru", agent.getCallUrl());
        assertEquals("https://super-server.io https://ru-custom-02.io", agent.getAudienceUrl());
        //AG is minipop
        agent.setResponse("OK");
        storage.write("AG", record);
        assertEquals("https://super-server.io/v2/storage/records/ag", agent.getCallUrl());
        assertEquals("https://super-server.io https://ag-custom-02.io", agent.getAudienceUrl());


    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void addAttachmentTest(String country,
                           String recordKey,
                           String body,
                           String key2,
                           String key3,
                           String profileKey,
                           Long rangeKey1,
                           boolean isKey,
                           boolean encrypt) throws StorageClientException, StorageServerException, IOException {
        String fileContent = "Hello world!";
        String fileId = "123456";
        String fileName = "sdk_incountry_unit_tests_file.txt";

        FakeHttpAgent agent = new FakeHttpAgent(String.format("{\"file_id\":\"%s\"}", fileId));
        Storage storage = initializeStorage(isKey, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        CryptoManager cryptoManager = initCryptoManager(isKey, encrypt);
        String keyHash = cryptoManager.createKeyHash(recordKey);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash +  "/attachments";

        Path tempFile = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        InputStream fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));

        String responseFileId = storage.addAttachment(country, recordKey, fileInputStream, fileName, false);
        String received = agent.getCallBody();
        String callPath = new URL(agent.getCallUrl()).getPath();

        assertEquals(fileId, responseFileId);
        assertEquals(expectedPath, callPath);
        assertEquals(received, fileContent);

        fileInputStream.close();
        Files.delete(tempFile);
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void deleteAttachmentTest(String country,
                              String recordKey,
                              String body,
                              String key2,
                              String key3,
                              String profileKey,
                              Long rangeKey1,
                              boolean isKey,
                              boolean encrypt) throws StorageClientException, StorageServerException, IOException {

        String fileId = "1";
        FakeHttpAgent agent = new FakeHttpAgent("{}");
        Storage storage = initializeStorage(isKey, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        storage.deleteAttachment(country, recordKey, fileId);
        CryptoManager cryptoManager = initCryptoManager(isKey, encrypt);
        String keyHash = cryptoManager.createKeyHash(recordKey);
        String expectedPath = "/v2/storage/records/"
                .concat(country)
                .concat("/")
                .concat(keyHash)
                .concat("/attachments/")
                .concat(fileId);
        String callPath = new URL(agent.getCallUrl()).getPath();
        assertEquals(expectedPath, callPath);
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void getAttachmentFileTest(String country,
                               String recordKey,
                               String body,
                               String key2,
                               String key3,
                               String profileKey,
                               Long rangeKey1,
                               boolean isKey,
                               boolean encrypt) throws StorageClientException, StorageServerException, IOException {
        String fileId = "1";
        String fileContent = "Hello world!";

        Path tempFile = Files.createTempFile("sdk_incountry_unit_tests_file", "txt");
        InputStream fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));

        Map<MetaInfoTypes, String> metaInfo1 = new HashMap<>();
        metaInfo1.put(MetaInfoTypes.NAME, "fileName");
        metaInfo1.put(MetaInfoTypes.EXTENSION, "txt");

        String expectedResponse = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8.name());
        FakeHttpAgent agent = new FakeHttpAgent(expectedResponse, metaInfo1);
        Storage storage = initializeStorage(isKey, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));

        AttachedFile file = storage.getAttachmentFile(country, recordKey, fileId);
        assertEquals(expectedResponse, IOUtils.toString(file.getFileContent(), StandardCharsets.UTF_8.name()));

        agent = new FakeHttpAgent(null, null);
        storage = initializeStorage(isKey, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        file = storage.getAttachmentFile(country, recordKey, fileId);
        assertNull(file.getFileContent());

        Map<MetaInfoTypes, String> metaInfo2 = new HashMap<>();
        metaInfo2.put(MetaInfoTypes.NAME, "fileName");
        agent = new FakeHttpAgent(null, metaInfo2);
        storage = initializeStorage(isKey, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        file = storage.getAttachmentFile(country, recordKey, fileId);
        assertNull(file.getFileExtension());

        Map<MetaInfoTypes, String> metaInfo3 = new HashMap<>();
        metaInfo3.put(MetaInfoTypes.EXTENSION, "txt");
        agent = new FakeHttpAgent(null, metaInfo3);
        storage = initializeStorage(isKey, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));
        file = storage.getAttachmentFile(country, recordKey, fileId);
        assertNull(file.getFileName());

        fileInputStream.close();
        Files.delete(tempFile);
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void updateAttachmentMetaTest(String country,
                                  String recordKey,
                                  String body,
                                  String key2,
                                  String key3,
                                  String profileKey,
                                  Long rangeKey1,
                                  boolean isKey,
                                  boolean encrypt) throws StorageClientException, StorageServerException {

        String fileId = "1";
        String fileName = "test_file";
        String mimeType = "text/plain";

        FakeHttpAgent agent = new FakeHttpAgent("{}");
        Storage storage = initializeStorage(isKey, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));

        storage.updateAttachmentMeta(country, recordKey, fileId, fileName, mimeType);

        JsonObject received = new Gson().fromJson(agent.getCallBody(), JsonObject.class);
        assertEquals(fileName, received.get("filename").getAsString());
        assertEquals(mimeType, received.get("mime_type").getAsString());
    }

    @SuppressWarnings({"java:S5785", "java:S5863"})
    @ParameterizedTest
    @MethodSource("recordArgs")
    void getAttachmentMetaTest(String country,
                               String recordKey,
                               String body,
                               String key2,
                               String key3,
                               String profileKey,
                               Long rangeKey1,
                               boolean isKey,
                               boolean encrypt) throws StorageClientException, StorageServerException {

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
        Storage storage = initializeStorage(isKey, false, new HttpDaoImpl(fakeEndpoint, null, null, agent));

        AttachmentMeta attachmentMeta = storage.getAttachmentMeta(country, recordKey, fileId);
        assertEquals(fileId, attachmentMeta.getFileId());
        assertEquals(downloadLink, attachmentMeta.getDownloadLink());
        assertEquals(fileName, attachmentMeta.getFileName());
        assertEquals(hash, attachmentMeta.getHash());
        assertEquals(mimeType, attachmentMeta.getMimeType());
        assertEquals(size, attachmentMeta.getSize());
        assertNull(attachmentMeta.getCreatedAt());
        assertNull(attachmentMeta.getUpdatedAt());
    }

    private String countryLoadBadResponseNullName = "{ \"countries\": [{\"direct\":true } ] }";
    private String countryLoadBadResponseEmptyName = "{ \"countries\": [{\"direct\":true, \"name\": \"\" } ] }";
    private String countryLoadBadResponseNullId = "{ \"countries\": [{\"name\":\"USA\" } ] }";
    private String countryLoadBadResponseEmptyId = "{ \"countries\": [{\"name\":\"USA\",\"id\":\"\" } ] }";
    private String countryLoadBadResponseEmptyCountries = "{ \"countries\": [ ] }";

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
