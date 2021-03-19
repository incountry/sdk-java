package com.incountry.residence.sdk;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.http.mocks.FakeHttpAgent;
import com.incountry.residence.sdk.tools.DtoTransformer;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.crypto.SecretsData;
import com.incountry.residence.sdk.tools.containers.MetaInfoTypes;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
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
import java.util.UUID;
import java.util.stream.Stream;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static com.incountry.residence.sdk.helper.ResponseUtils.getRecordStubResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpDaoImplTests {

    private static final String SECRET = "passwordpasswordpasswordpassword";
    private static final String FAKE_ENDPOINT = "http://fakeEndpoint.localhost:8081";
    private static final String ENV_ID = "envId";
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private SecretsData secretsData;
    private DtoTransformer transformer;
    private HashUtils hashUtils;

    @BeforeEach
    private void init() throws StorageClientException {
        secretsData = SecretsDataGenerator.fromPassword(SECRET);
        hashUtils = new HashUtils(ENV_ID, false);
        transformer = new DtoTransformer(new CryptoProvider(null),
                hashUtils,
                true,
                () -> secretsData);
    }

    private Storage initializeStorage(HttpDaoImpl dao) throws StorageClientException, StorageCryptoException {
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENV_ID)
                .setApiKey("apiKey")
                .setSecretKeyAccessor(() -> secretsData);
        return StorageImpl.newStorage(config, dao);
    }

    private static Stream<Arguments> recordArgs() {
        return Stream.of(
                Arguments.of("us", "someRecordKey", null, null, null, null, null),
                Arguments.of("us", "someRecordKey", "body", null, null, null, null),
                Arguments.of("us", "someRecordKey", "body", "key2", null, null, null),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", null, null),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", "profileKey", null),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", "profileKey", 1L),

                Arguments.of("us", "someRecordKey", null, null, null, null, null),
                Arguments.of("us", "someRecordKey", "body", null, null, null, null),
                Arguments.of("us", "someRecordKey", "body", "key2", null, null, null),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", null, null),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", "profileKey", null),
                Arguments.of("us", "someRecordKey", "body", "key2", "key3", "profileKey", 1L)
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
                   Long rangeKey1) throws StorageException, MalformedURLException {
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        String expectedPath = "/v2/storage/records/" + country;

        Record record = new Record(recordKey, body)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        agent.setResponse(getRecordStubResponse(record, transformer));
        storage.write(country, record);

        String received = agent.getCallBody();
        String callPath = new URL(agent.getCallUrl()).getPath();

        TransferRecord receivedRecord = GSON.fromJson(received, TransferRecord.class);

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
                  Long rangeKey1) throws StorageException, MalformedURLException {

        Record record = new Record(recordKey, body)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);

        String keyHash = hashUtils.getSha256Hash(recordKey);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;

        FakeHttpAgent agent = new FakeHttpAgent(GSON.toJson(transformer.getTransferRecord(record)));
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

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
                    Long rangeKey1) throws StorageException, IOException {

        FakeHttpAgent agent = new FakeHttpAgent("{}");
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        storage.delete(country, recordKey);
        String keyHash = hashUtils.getSha256Hash(recordKey);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;
        String callPath = new URL(agent.getCallUrl()).getPath();
        assertEquals(expectedPath, callPath);
    }

    @Test
    void batchWriteNullTest() throws StorageClientException, StorageCryptoException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
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
                        Long rangeKey1) throws StorageException {

        FakeHttpAgent agent = new FakeHttpAgent("ok");
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        List<Record> records = new ArrayList<>();
        Record record = new Record(recordKey, body)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setKey2(key2)
                .setKey3(key3);
        records.add(record);
        storage.batchWrite(country, records);

        String encryptedHttpBody = agent.getCallBody();
        String keyHash = hashUtils.getSha256Hash(recordKey);
        JsonArray responseList = new Gson().fromJson(encryptedHttpBody, JsonObject.class).getAsJsonArray("records");
        for (JsonElement oneJsonRecord : responseList) {
            String keyFromResponse = ((JsonObject) oneJsonRecord).get("record_key").getAsString();
            String encryptedBody = ((JsonObject) oneJsonRecord).get("body").getAsString();
            String actualBodyStr = new CryptoProvider(null).decrypt(encryptedBody, secretsData, 0);
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
    void testBatchWritePopApiResponse() throws StorageClientException, StorageServerException, StorageCryptoException {
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList("ok", "Ok", "OK", "okokok", null));
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        String country = "US";
        List<Record> list = Collections.singletonList(new Record("key", "body"));
        List<Record> recordList = storage.batchWrite(country, list); //ok
        assertNotNull(recordList);
        recordList = storage.batchWrite(country, list); //Ok
        assertNotNull(recordList);
        recordList = storage.batchWrite(country, list); //OK
        assertNotNull(recordList);
        recordList = storage.batchWrite(country, list); //OKokok
        assertNotNull(recordList);
        recordList = storage.batchWrite(country, list); //null
        assertNotNull(recordList);
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
                "  \"record_key\": \"4ece651b948f24dac10c9f1d3387741a07e5c4f6bf5412b06100945163176703\",\n" +
                "  \"body\": \"2:J0r3EemBgUWwTcVXSIlV71TaSYZJajwVMFEASnizOcK3jcfJ4tpwXjBYimUg8VrHqov7BVdXQbPSLhIh9beUzSsWE0CqVXRWNN9AIeCfyr9/MV/Qc//XPd2wuIvUOKP/RW6Al70ooCsGvc0Rv79rQIg/jEjIYYl1uNCAKmQj8E4lrLf2HaLTKUX6GsAHrEjUp1pXOEQ6StgUOPE73jlp9AzsTg==\"\n" +
                "}";
        String notJson = "StringNotJson";
        String wrongJson = "{\"FirstName\":\"<first name>\"}";
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(goodReRespPTE, goodRespEnc, null, notJson, wrongJson));
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        String country = "US";
        String someKey = "someRecordKey";
        Record recordPte = storage.read(country, someKey);
        assertNotNull(recordPte);

        Record recordEnc = storage.read(country, someKey);
        assertNotNull(recordEnc);
        assertNotEquals(recordEnc, recordPte);

        Record nullRecord = storage.read(country, someKey);
        assertNull(nullRecord);

        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> storage.read(country, someKey));
        assertEquals("Unexpected exception", ex1.getMessage());
        assertTrue(ex1.getCause() instanceof JsonSyntaxException);

        StorageServerException ex2 = assertThrows(StorageServerException.class, () -> storage.read(country, someKey));
        assertEquals("Null required record fields: recordKey", ex2.getMessage());
    }

    @Test
    void testSearchPopApiResponse() throws StorageClientException, StorageServerException, StorageCryptoException {
        FindFilter filter = new FindFilter()
                .limitAndOffset(1, 0)
                .keyEq(StringField.PROFILE_KEY, "profileKey");
        Record record = new Record("someRecordKey", "body")
                .setProfileKey("profileKey");
        String encrypted = GSON.toJson(transformer.getTransferRecord(record));
        String goodResponse = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(goodResponse, "StringNotJson"));
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        String country = "US";
        FindResult findResult = storage.find(country, filter);
        assertNotNull(findResult);
        assertTrue(findResult.getRecords().size() > 0);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.find(country, filter));
        assertEquals("Unexpected exception", ex.getMessage());
        assertTrue(ex.getCause() instanceof JsonSyntaxException);
    }

    @Test
    void testLoadCountriesPopApiResponse() {
        String countryLoadBadResponseNullName = "{ \"countries\": [{\"direct\":true } ] }";
        String countryLoadBadResponseEmptyName = "{ \"countries\": [{\"direct\":true, \"name\": \"\" } ] }";
        String countryLoadBadResponseNullId = "{ \"countries\": [{\"name\":\"USA\" } ] }";
        String countryLoadBadResponseEmptyId = "{ \"countries\": [{\"name\":\"USA\",\"id\":\"\" } ] }";
        String countryLoadBadResponseEmptyCountries = "{ \"countries\": [ ] }";
        FakeHttpAgent agent = new FakeHttpAgent(Arrays.asList(COUNTRY_LOAD_RESPONSE,
                "StringNotJson",
                countryLoadBadResponseNullName,
                countryLoadBadResponseEmptyName,
                countryLoadBadResponseNullId,
                countryLoadBadResponseEmptyId,
                countryLoadBadResponseEmptyCountries));
        Dao correctDao = new HttpDaoImpl(null, null, null, agent);
        assertNotNull(correctDao);

        for (int i = 0; i < 6; i++) {
            StorageServerException ex = assertThrows(StorageServerException.class, () ->
                    new HttpDaoImpl(null, null, null, agent).read("US", "recordKey"));
            assertEquals("Country list is empty", ex.getMessage());
        }
    }

    @RepeatedTest(3)
    void testLoadCountriesInDefaultEndPoint(RepetitionInfo repeatInfo) throws StorageServerException, StorageCryptoException, StorageClientException {
        iterateLogLevel(repeatInfo, HttpDaoImpl.class);
        FakeHttpAgent agent = new FakeHttpAgent(COUNTRY_LOAD_RESPONSE);
        Storage storage = initializeStorage(new HttpDaoImpl(null, null, "https://localhost:8080", agent));
        Record record = new Record("1", "body");
        String recordResponse = getRecordStubResponse(record, transformer);
        agent.setResponse(recordResponse);
        storage.write("US", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/us", agent.getCallUrl());
        agent.setResponse(recordResponse);
        storage.write("us", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/us", agent.getCallUrl());
        agent.setResponse(recordResponse);
        storage.write("RU", record);
        assertEquals("https://ru-mt-01.api.incountry.io/v2/storage/records/ru", agent.getCallUrl());
        agent.setResponse(recordResponse);
        storage.write("ru", record);
        assertEquals("https://ru-mt-01.api.incountry.io/v2/storage/records/ru", agent.getCallUrl());
        //country 'PU' has no separate endpoint
        storage.write("PU", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/pu", agent.getCallUrl());
        agent.setResponse(recordResponse);
        storage.write("pu", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/pu", agent.getCallUrl());
        //country 'SU' is not in country list
        storage.write("SU", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/su", agent.getCallUrl());
        agent.setResponse(recordResponse);
        storage.write("su", record);
        assertEquals("https://us-mt-01.api.incountry.io/v2/storage/records/su", agent.getCallUrl());
    }

    @Test
    void testLoadCountriesInDefaultEndPointWithMask() throws StorageServerException, StorageCryptoException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent(COUNTRY_LOAD_RESPONSE);
        Storage storage = initializeStorage(new HttpDaoImpl(null, "-test-01.debug.org", null, agent));
        Record record = new Record("1", "body");
        String recordResponse = getRecordStubResponse(record, transformer);
        agent.setResponse(recordResponse);

        //US is midpop
        storage.write("US", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/us", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org", agent.getAudienceUrl());
        agent.setResponse(recordResponse);
        storage.write("us", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/us", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org", agent.getAudienceUrl());

        //RU is midpop
        agent.setResponse(recordResponse);
        storage.write("RU", record);
        assertEquals("https://ru-test-01.debug.org/v2/storage/records/ru", agent.getCallUrl());
        assertEquals("https://ru-test-01.debug.org", agent.getAudienceUrl());
        agent.setResponse(recordResponse);
        storage.write("ru", record);
        assertEquals("https://ru-test-01.debug.org/v2/storage/records/ru", agent.getCallUrl());
        assertEquals("https://ru-test-01.debug.org", agent.getAudienceUrl());

        //country 'PU' is minipop
        storage.write("PU", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/pu", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org https://pu-test-01.debug.org", agent.getAudienceUrl());
        agent.setResponse(recordResponse);
        storage.write("pu", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/pu", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org https://pu-test-01.debug.org", agent.getAudienceUrl());

        //country 'SU' is not in country list
        storage.write("SU", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/su", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org https://su-test-01.debug.org", agent.getAudienceUrl());
        agent.setResponse(recordResponse);
        storage.write("su", record);
        assertEquals("https://us-test-01.debug.org/v2/storage/records/su", agent.getCallUrl());
        assertEquals("https://us-test-01.debug.org https://su-test-01.debug.org", agent.getAudienceUrl());
    }

    @Test
    void testWriteToCustomEndpointWithMinipop() throws StorageServerException, StorageCryptoException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(new HttpDaoImpl("https://us.test.org", "test.org", null, agent));
        Record record = new Record("1", "body");
        String response = getRecordStubResponse(record, transformer);
        agent.setResponse(response);
        storage.write("US", record);
        assertEquals("https://us.test.org/v2/storage/records/us", agent.getCallUrl());
    }

    @Test
    void testWriteToCustomEndpointWithMinipopAndSecondaryUrl() throws StorageServerException, StorageCryptoException, StorageClientException {
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(new HttpDaoImpl("https://ustest.org", "test.org", null, agent));
        Record record = new Record("1", "body");
        agent.setResponse(getRecordStubResponse(record, transformer));
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
    void endPointAndAudiencePositive() throws StorageServerException, StorageCryptoException, StorageClientException {
        //storage has only custom endpoint
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        Storage storage = initializeStorage(new HttpDaoImpl("https://custom.io", null, null, agent));
        Record record = new Record("1", "body");
        //AG is minipop
        String recordResponse = getRecordStubResponse(record, transformer);
        agent.setResponse(recordResponse);
        storage.write("AG", record);
        assertEquals("https://custom.io/v2/storage/records/ag", agent.getCallUrl());
        assertEquals("https://custom.io", agent.getAudienceUrl());

        //storage has only endpoint mask
        agent.setResponse(COUNTRY_LOAD_RESPONSE);
        storage = initializeStorage(new HttpDaoImpl(null, "-custom-01.test.io", null, agent));
        //US is midpop
        agent.setResponse(recordResponse);
        storage.write("US", record);
        assertEquals("https://us-custom-01.test.io/v2/storage/records/us", agent.getCallUrl());
        assertEquals("https://us-custom-01.test.io", agent.getAudienceUrl());
        //RU is midpop
        storage.write("RU", record);
        assertEquals("https://ru-custom-01.test.io/v2/storage/records/ru", agent.getCallUrl());
        assertEquals("https://ru-custom-01.test.io", agent.getAudienceUrl());
        //AG is minipop
        storage.write("AG", record);
        assertEquals("https://us-custom-01.test.io/v2/storage/records/ag", agent.getCallUrl());
        assertEquals("https://us-custom-01.test.io https://ag-custom-01.test.io", agent.getAudienceUrl());

        //storage has endpoint and endpoint mask
        storage = initializeStorage(new HttpDaoImpl("https://super-server.io", "-custom-02.io", null, agent));
        //US is midpop
//        agent.setResponse("OK");
        storage.write("US", record);
        assertEquals("https://super-server.io/v2/storage/records/us", agent.getCallUrl());
        assertEquals("https://super-server.io https://us-custom-02.io", agent.getAudienceUrl());
        //RU is midpop
        storage.write("RU", record);
        assertEquals("https://super-server.io/v2/storage/records/ru", agent.getCallUrl());
        assertEquals("https://super-server.io https://ru-custom-02.io", agent.getAudienceUrl());
        //AG is minipop
        storage.write("AG", record);
        assertEquals("https://super-server.io/v2/storage/records/ag", agent.getCallUrl());
        assertEquals("https://super-server.io https://ag-custom-02.io", agent.getAudienceUrl());
    }

    @Test
    void addAttachmentTest() throws StorageClientException, StorageServerException, IOException, StorageCryptoException {
        String country = "us";
        String recordKey = UUID.randomUUID().toString();
        String fileContent = "File content " + recordKey;
        String fileId = "file_id_" + recordKey;
        String fileName = "file_name_" + recordKey + ".txt";

        FakeHttpAgent agent = new FakeHttpAgent(String.format("{\"file_id\":\"%s\"}", fileId));
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        String keyHash = hashUtils.getSha256Hash(recordKey);
        String expectedPath = "/v2/storage/records/" + country + "/" + keyHash + "/attachments";
        ByteArrayInputStream stream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        AttachmentMeta attachmentMeta = storage.addAttachment(country, recordKey, stream, fileName, false);
        String callPath = new URL(agent.getCallUrl()).getPath();
        assertEquals(fileId, attachmentMeta.getFileId());
        assertEquals(expectedPath, callPath);
        assertEquals(stream, agent.getDataStream());
    }

    @ParameterizedTest
    @MethodSource("recordArgs")
    void deleteAttachmentTest(String country,
                              String recordKey,
                              String body,
                              String key2,
                              String key3,
                              String profileKey,
                              Long rangeKey1) throws StorageClientException, StorageServerException, IOException, StorageCryptoException {

        String fileId = "1";
        FakeHttpAgent agent = new FakeHttpAgent("{}");
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        storage.deleteAttachment(country, recordKey, fileId);
        String keyHash = hashUtils.getSha256Hash(recordKey);
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
                               Long rangeKey1) throws StorageClientException, StorageServerException, IOException, StorageCryptoException {
        String fileId = "1";
        String fileContent = "Hello world!";

        Path tempFile = Files.createTempFile("sdk_incountry_unit_tests_file", "txt");
        InputStream fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));

        Map<MetaInfoTypes, String> metaInfo1 = new HashMap<>();
        metaInfo1.put(MetaInfoTypes.NAME, "fileName");

        String expectedResponse = IOUtils.toString(Files.newInputStream(tempFile), StandardCharsets.UTF_8.name());
        FakeHttpAgent agent = new FakeHttpAgent(expectedResponse, metaInfo1, fileInputStream);
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        AttachedFile file = storage.getAttachmentFile(country, recordKey, fileId);
        assertEquals(expectedResponse, IOUtils.toString(file.getFileContent(), StandardCharsets.UTF_8.name()));

        agent = new FakeHttpAgent(null, null, null);
        storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        file = storage.getAttachmentFile(country, recordKey, fileId);
        assertNull(file.getFileContent());

        Map<MetaInfoTypes, String> metaInfo3 = new HashMap<>();
        agent = new FakeHttpAgent(null, metaInfo3, null);
        storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
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
                                  Long rangeKey1) throws StorageClientException, StorageServerException, StorageCryptoException {

        String fileId = "1";
        String fileName = "test_file";
        String mimeType = "text/plain";

        FakeHttpAgent agent = new FakeHttpAgent("{}");
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

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
                               Long rangeKey1) throws StorageClientException, StorageServerException, StorageCryptoException {

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
        Storage storage = initializeStorage(new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        AttachmentMeta attachmentMeta = storage.getAttachmentMeta(country, recordKey, fileId);
        assertEquals(fileId, attachmentMeta.getFileId());
        assertEquals(downloadLink, attachmentMeta.getDownloadLink());
        assertEquals(fileName, attachmentMeta.getFilename());
        assertEquals(hash, attachmentMeta.getHash());
        assertEquals(mimeType, attachmentMeta.getMimeType());
        assertEquals(size, attachmentMeta.getSize());
        assertNull(attachmentMeta.getCreatedAt());
        assertNull(attachmentMeta.getUpdatedAt());
    }

    private static final String COUNTRY_LOAD_RESPONSE = "{\n" +
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
