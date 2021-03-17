package com.incountry.residence.sdk;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.crypto.EncryptionSecret;
import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.http.mocks.FakeHttpAgent;
import com.incountry.residence.sdk.http.mocks.FakeHttpServer;
import com.incountry.residence.sdk.tools.DtoTransformer;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.crypto.SecretKeyAccessor;
import com.incountry.residence.sdk.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.crypto.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.containers.MetaInfoTypes;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.incountry.residence.sdk.LogLevelUtils.iterateLogLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageTest {
    private static final int PORT = 8767;
    private static final String ENVIRONMENT_ID = "envId";
    private static final String FAKE_ENDPOINT = "http://fakeEndpoint.localhost:8081";
    private static final byte[] SECRET = "passwordpasswordpasswordpassword".getBytes(StandardCharsets.UTF_8);
    private static final String COUNTRY = "us";
    private static final String RECORD_KEY = "some_key";
    private static final String KEY_2 = "key2";
    private static final String KEY_3 = "key3";
    private static final String PROFILE_KEY = "profileKey";
    private static final Long RANGE_KEY_1 = 1L;
    private static final String BODY = "body";
    private static final Integer HTTP_POOL_SIZE = 5;

    private DtoTransformer dtoTransformer;
    private HashUtils hashUtils;
    private SecretKeyAccessor secretKeyAccessor;
    private Gson gson;

    @BeforeEach
    public void initializeAccessorAndCrypto() throws StorageClientException {
        int version = 0;
        Secret secret = new EncryptionSecret(version, SECRET);
        SecretsData secretsData = new SecretsData(Collections.singletonList(secret), secret);
        secretKeyAccessor = () -> secretsData;
        CryptoProvider cryptoProvider = new CryptoProvider(null);
        hashUtils = new HashUtils(ENVIRONMENT_ID, false);
        dtoTransformer = new DtoTransformer(cryptoProvider,
                hashUtils, true, secretKeyAccessor);
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .disableHtmlEscaping()
                .create();
    }

    @RepeatedTest(3)
    void migratePositive(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedJson = gson.toJson(dtoTransformer.getTransferRecord(record));
        //return 2 records, 1 correct, 1 invalid
        String response1 = "{\"data\":[" + encryptedJson + ",{\"not_record\":1}],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}";
        //return 1 correct record
        String response2 = "{\"data\":[" + encryptedJson + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        //return 0 records
        String response3 = "{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}";
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config,
                new HttpDaoImpl(FAKE_ENDPOINT, null, null,
                        new FakeHttpAgent(Arrays.asList(response1, "OK", response2, "OK", response3, "OK"))));

        String expected1 = "MigrateResult{migrated=1, totalLeft=1, errors=[com.incountry.residence.sdk.tools.exceptions.RecordException: Record parse exception]}";
        runMigrationChecks(response1, expected1, storage);
        String expected2 = "MigrateResult{migrated=1, totalLeft=0, errors=[]}";
        runMigrationChecks(response2, expected2, storage);
        String expected3 = "MigrateResult{migrated=0, totalLeft=0, errors=[]}";
        runMigrationChecks(response3, expected3, storage);
    }

    private void runMigrationChecks(String response1, String expected1, Storage storage) throws StorageServerException, StorageClientException, StorageCryptoException {
        FindResult findResult = dtoTransformer.getFindResult(gson.fromJson(response1, TransferFindResult.class));
        int migratedRecords = findResult.getRecords().size();
        int totalLeft = findResult.getTotal() - findResult.getRecords().size();
        MigrateResult migrateResult = storage.migrate("us", 2);
        assertEquals(migratedRecords, migrateResult.getMigrated());
        assertEquals(totalLeft, migrateResult.getTotalLeft());
        if (findResult.getErrors() != null && !findResult.getErrors().isEmpty()) {
            assertEquals(findResult.getErrors().get(0).getRawData(), migrateResult.getErrors().get(0).getRawData());
        }
        assertEquals(expected1, migrateResult.toString());
    }

    @Test
    void migrateNegative() throws StorageClientException, StorageCryptoException {
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, new FakeHttpAgent("")));
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> storage.migrate("us", 0));
        assertEquals("Limit can't be < 1", ex1.getMessage());
        Storage storage2 = StorageImpl.newStorage(config.setSecretKeyAccessor(null), new HttpDaoImpl(FAKE_ENDPOINT, null, null, new FakeHttpAgent("")));
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> storage2.migrate("us", 1));
        assertEquals("Migration is not supported when encryption is off", ex2.getMessage());
    }

    @RepeatedTest(3)
    void findTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FindFilter builder = new FindFilter()
                .limitAndOffset(1, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedJson = gson.toJson(dtoTransformer.getTransferRecord(record));
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedJson + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        FindResult findResult = storage.find(COUNTRY, builder);

        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        assertEquals(RECORD_KEY, findResult.getRecords().get(0).getRecordKey());
        assertEquals(BODY, findResult.getRecords().get(0).getBody());
    }

    @RepeatedTest(3)
    void customEndpointPositive(RepetitionInfo repeatInfo) throws StorageException, IOException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(endpoint, null, null, agent));
        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storage.write(COUNTRY, record);
        String expectedURL = endpoint + "/v2/storage/records/" + COUNTRY;
        String realURL = new URL(agent.getCallUrl()).toString();
        assertEquals(expectedURL, realURL);
    }

    @Test
    void writeNullKeyNegative() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(endpoint, null, null, agent));
        Record record = new Record(null, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.write(COUNTRY, record));
        assertEquals("Key can't be null", ex.getMessage());
    }

    @Test
    void testNegativeWriteNullRecord() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(endpoint, null, null, agent));
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.write(COUNTRY, null));
        assertEquals("Can't write null record", ex.getMessage());
    }

    @Test
    void testNegativeWriteNullCountry() throws StorageException {
        String endpoint = "https://custom.endpoint.io";
        FakeHttpAgent agent = new FakeHttpAgent("OK");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(endpoint, null, null, agent));
        String key = "<key>";
        Record record = new Record(key, "<body>");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.write(null, record));
        assertEquals("Country can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.read(null, key));
        assertEquals("Country can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.delete(null, key));
        assertEquals("Country can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.batchWrite(null, Collections.singletonList(record)));
        assertEquals("Country can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.batchWrite("us", Collections.singletonList(null)));
        assertEquals("Can't write null record", ex.getMessage());
    }

    @Test
    void findWithEncryptionPositive() throws StorageException {
        FindFilter filter = new FindFilter()
                .limitAndOffset(1, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);

        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedJson = gson.toJson(dtoTransformer.getTransferRecord(record));
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedJson + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        FindResult findResult = storage.find(COUNTRY, filter);
        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + hashUtils.getSha256Hash(PROFILE_KEY) + "\"]},\"options\":{\"offset\":0,\"limit\":1}}";
        assertEquals(expected, callBody);

        assertEquals(1, findResult.getCount());
        assertEquals(1, findResult.getRecords().size());
        assertEquals(RECORD_KEY, findResult.getRecords().get(0).getRecordKey());
        assertEquals(BODY, findResult.getRecords().get(0).getBody());
        assertEquals(KEY_2, findResult.getRecords().get(0).getKey2());
        assertEquals(KEY_3, findResult.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, findResult.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY_1, findResult.getRecords().get(0).getRangeKey1());
        assertEquals(0, findResult.getRecords().get(0).getAttachments().size());
    }

    @RepeatedTest(3)
    void findOnePositive(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FindFilter filter = new FindFilter()
                .limitAndOffset(1, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);

        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedJson = gson.toJson(dtoTransformer.getTransferRecord(record));
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedJson + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        Record foundRecord = storage.findOne(COUNTRY, filter);

        String callBody = agent.getCallBody();
        String expected = "{\"filter\":{\"profile_key\":[\"" + hashUtils.getSha256Hash(PROFILE_KEY) + "\"]},\"options\":{\"offset\":0,\"limit\":1}}";
        assertEquals(expected, callBody);

        assertEquals(RECORD_KEY, foundRecord.getRecordKey());
        assertEquals(BODY, foundRecord.getBody());
        assertEquals(KEY_2, foundRecord.getKey2());
        assertEquals(KEY_3, foundRecord.getKey3());
        assertEquals(PROFILE_KEY, foundRecord.getProfileKey());
        assertEquals(RANGE_KEY_1, foundRecord.getRangeKey1());

        agent.setResponse("{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}");
        foundRecord = storage.findOne(COUNTRY, filter);
        assertNull(foundRecord);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.findOne(COUNTRY, null));
        assertEquals("Filters can't be null", ex.getMessage());
    }

    @Test
    void testFindWithEncByMultipleSecrets() throws StorageException {
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("otherpassword");
        CryptoProvider otherProvider = new CryptoProvider(null);
        DtoTransformer anotherTransformer = new DtoTransformer(otherProvider, hashUtils, true, accessor);

        FindFilter filter = new FindFilter()
                .limitAndOffset(2, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);

        Record record1 = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        Record record2 = record1.copy();
        String encryptedRecOther = gson.toJson(anotherTransformer.getTransferRecord(record1));
        String encryptedRec = gson.toJson(dtoTransformer.getTransferRecord(record2));

        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        FindResult findResult = storage.find(COUNTRY, filter);
        assertEquals(1, findResult.getErrors().size());
        assertEquals(encryptedRecOther, findResult.getErrors().get(0).getRawData());
        assertEquals("Record parse exception", findResult.getErrors().get(0).getMessage());

        assertEquals(1, findResult.getRecords().size());
        assertEquals(RECORD_KEY, findResult.getRecords().get(0).getRecordKey());
        assertEquals(BODY, findResult.getRecords().get(0).getBody());
        assertEquals(KEY_2, findResult.getRecords().get(0).getKey2());
        assertEquals(KEY_3, findResult.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, findResult.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY_1, findResult.getRecords().get(0).getRangeKey1());
    }

    @Test
    void testFindNullFilterSending() throws StorageException {
        FindFilter filter = new FindFilter()
                .keyEq(StringField.RECORD_KEY, "SomeValue");
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[],\"meta\":{\"count\":0,\"limit\":10,\"offset\":0,\"total\":0}}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        storage.find(COUNTRY, filter);
        String body = agent.getCallBody();
        JsonObject json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNotNull(json.get("record_key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key1"));
        assertNull(json.get("profile_key"));

        filter.clear().keyEq(StringField.KEY2, "SomeValue");
        storage.find(COUNTRY, filter);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("record_key"));
        assertNotNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key1"));
        assertNull(json.get("profile_key"));

        filter.clear().keyEq(StringField.KEY3, "SomeValue");
        storage.find(COUNTRY, filter);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("record_key"));
        assertNull(json.get("key2"));
        assertNotNull(json.get("key3"));
        assertNull(json.get("range_key1"));
        assertNull(json.get("profile_key"));

        filter.clear().keyEq(NumberField.RANGE_KEY1, 123321L);
        storage.find(COUNTRY, filter);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("record_key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNotNull(json.get("range_key1"));
        assertNull(json.get("profile_key"));

        filter.clear().keyEq(StringField.PROFILE_KEY, "SomeValue");
        storage.find(COUNTRY, filter);
        body = agent.getCallBody();
        json = (JsonObject) new Gson().fromJson(body, JsonObject.class).get("filter");
        assertNull(json.get("record_key"));
        assertNull(json.get("key2"));
        assertNull(json.get("key3"));
        assertNull(json.get("range_key1"));
        assertNotNull(json.get("profile_key"));
    }

    @Test
    void testFindWithEncAndFoundPTE() throws StorageException {
        Record recWithEnc = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        Record recWithPTEnc = new Record(RECORD_KEY + 1, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedRec = gson.toJson(dtoTransformer.getTransferRecord(recWithEnc));
        DtoTransformer transformerWithPte = new DtoTransformer(new CryptoProvider(null), hashUtils, true, null);
        String encryptedPTRec = gson.toJson(transformerWithPte.getTransferRecord(recWithPTEnc));
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        FindResult findResult = storage.find(COUNTRY, new FindFilter().keyIsNotNull(NumberField.VERSION));
        assertEquals(0, findResult.getErrors().size());
        assertEquals(2, findResult.getRecords().size());
    }

    @Test
    void testFindWithoutEncWithEncryptedData() throws StorageException {
        Record recWithEnc = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        Record recWithPTEnc = new Record(RECORD_KEY + 1, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedRec = gson.toJson(dtoTransformer.getTransferRecord(recWithEnc));
        DtoTransformer transformerWithPte = new DtoTransformer(new CryptoProvider(null), hashUtils, true, null);
        String encryptedPTRec = gson.toJson(transformerWithPte.getTransferRecord(recWithPTEnc));
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        FindResult findResult = storage.find(COUNTRY, new FindFilter().keyIsNotNull(NumberField.RANGE_KEY1));
        assertEquals(1, findResult.getErrors().size());
        assertEquals(encryptedRec, findResult.getErrors().get(0).getRawData());
        assertEquals("Record parse exception", findResult.getErrors().get(0).getMessage());

        assertEquals(1, findResult.getRecords().size());
        assertEquals(RECORD_KEY + 1, findResult.getRecords().get(0).getRecordKey());
        assertEquals(BODY, findResult.getRecords().get(0).getBody());
        assertEquals(KEY_2, findResult.getRecords().get(0).getKey2());
        assertEquals(KEY_3, findResult.getRecords().get(0).getKey3());
        assertEquals(PROFILE_KEY, findResult.getRecords().get(0).getProfileKey());
        assertEquals(RANGE_KEY_1, findResult.getRecords().get(0).getRangeKey1());
    }

    @Test
    void testFindIncorrectRecords() throws StorageException {
        FindFilter filter = new FindFilter()
                .limitAndOffset(2, 0)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        FakeHttpAgent agent = new FakeHttpAgent((String) null);
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storage.find(COUNTRY, filter));
        assertEquals("Response error: Meta is null", ex.getMessage());
    }

    @RepeatedTest(3)
    void testReadNotFound(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FakeHttpAgent agent = new FakeHttpAgent((String) null);
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        Record readRecord = storage.read(COUNTRY, RECORD_KEY);
        assertNull(readRecord);
    }

    @Test
    void testErrorFindOneInsufficientArgs() throws StorageException {
        Record record = new Record(RECORD_KEY, BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        String encryptedRec = gson.toJson(dtoTransformer.getTransferRecord(record));
        FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> storage.find(null, null));
        assertEquals("Country can't be null", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> storage.find(COUNTRY, null));
        assertEquals("Filters can't be null", ex2.getMessage());
    }

    @RepeatedTest(3)
    void testInitErrorOnInsufficientArgs(RepetitionInfo repeatInfo) {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(new StorageConfig()));
        assertEquals("Please pass environment_id param or set INC_ENVIRONMENT_ID env var", ex1.getMessage());
    }

    @Test
    void testErrorReadInsufficientArgs() throws StorageClientException, StorageCryptoException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent);
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, dao);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.read(null, null));
        assertEquals("Country can't be null", ex.getMessage());
    }

    @RepeatedTest(3)
    void testErrorDeleteInsufficientArgs(RepetitionInfo repeatInfo) throws StorageCryptoException, StorageClientException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent);
        assertNotNull(dao);
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, dao);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.delete(null, null));
        assertEquals("Country can't be null", ex.getMessage());
    }

    @Test
    void testErrorMigrateWhenEncryptionOff() throws StorageException {
        FakeHttpAgent agent = new FakeHttpAgent("");
        Dao dao = new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent);
        assertNotNull(dao);
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, dao);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.migrate(null, 100));
        assertEquals("Country can't be null", ex.getMessage());
    }

    @Test
    void testPositiveWithConstructor2() throws StorageClientException, StorageCryptoException {
        Secret secret = new EncryptionSecret(1, "secret".getBytes(StandardCharsets.UTF_8));
        SecretsData secretData = new SecretsData(Collections.singletonList(secret), secret);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiLey")
                .setEndPoint(FAKE_ENDPOINT)
                .setEnvironmentId(ENVIRONMENT_ID);
        Storage storage = StorageImpl.newStorage(config);
        assertNotNull(storage);
    }

    @Test
    void authorizationParamsNegative() throws StorageClientException {
        Secret secret = new EncryptionSecret(1, "secret".getBytes(StandardCharsets.UTF_8));
        SecretsData secretData = new SecretsData(Collections.singletonList(secret), secret);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config));
        assertEquals("Please pass only one parameter combination for authorisation: clientId/clientSecret or apiKey or oauthTokenAccessor", ex.getMessage());
    }

    @Test
    void testNegativeWithConstructorNullDao() {
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config, null));
        assertEquals("Please pass only one parameter combination for authorisation: clientId/clientSecret or apiKey or oauthTokenAccessor", ex.getMessage());
    }

    @Test
    void positiveTestWithClientId() throws StorageClientException, StorageCryptoException {
        Secret secret = new EncryptionSecret(1, "secret".getBytes(StandardCharsets.UTF_8));
        SecretsData secretData = new SecretsData(Collections.singletonList(secret), secret);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setClientId("clientId")
                .setClientSecret("clientSecret");
        assertNotNull(StorageImpl.newStorage(config));
    }

    @Test
    void negativeTestNullClientSecret() throws StorageClientException {
        Secret secret = new EncryptionSecret(1, "secret".getBytes(StandardCharsets.UTF_8));
        SecretsData secretData = new SecretsData(Collections.singletonList(secret), secret);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setClientId("clientId");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config));
        assertEquals("Please pass clientSecret in configuration", ex.getMessage());
    }

    @Test
    void negativeTestEmptySecret() throws StorageClientException {
        Secret secret = new EncryptionSecret(1, "secret".getBytes(StandardCharsets.UTF_8));
        SecretsData secretData = new SecretsData(Collections.singletonList(secret), secret);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setClientId("")
                .setClientSecret("");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config));
        assertEquals("Please pass clientId in configuration", ex.getMessage());
    }

    @Test
    void allAuthParamsNegative() throws StorageClientException {
        Secret secret = new EncryptionSecret(1, "secret".getBytes(StandardCharsets.UTF_8));
        SecretsData secretData = new SecretsData(Collections.singletonList(secret), secret);
        SecretKeyAccessor secretKeyAccessor = () -> secretData;
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setClientId("<clientId>")
                .setApiKey("<apiKey>")
                .setOauthToken("<token>");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config));
        assertEquals("Please pass only one parameter combination for authorisation: clientId/clientSecret or apiKey or oauthTokenAccessor", ex.getMessage());
    }

    @Test
    void customTimeoutPositive() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer("{}", 200, PORT);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvironmentId("<envId>");
        Storage storage = StorageImpl.newStorage(config);
        assertTrue(storage.delete(COUNTRY, RECORD_KEY));
        server.stop(0);
    }

    @Test
    void negativeTestNullResponse() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer((String) null, 200, PORT);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvironmentId("<envId>");
        Storage storage = StorageImpl.newStorage(config);
        assertNull(storage.read(COUNTRY, RECORD_KEY));
        server.stop(0);
    }

    @Test
    void negativeTestIllegalTimeout() {
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setEndPoint(FAKE_ENDPOINT)
                .setApiKey("<apiKey>")
                .setHttpTimeout(0)
                .setMaxHttpPoolSize(HTTP_POOL_SIZE);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config));
        assertEquals("Connection timeout can't be <1. Expected 'null' or positive value, received=0", ex.getMessage());
    }

    @Test
    void negativeTestTimeoutError() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer("{}", 200, PORT, 5);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(1)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvironmentId("<envId>")
                .setMaxHttpPoolSize(HTTP_POOL_SIZE);
        Storage storage = StorageImpl.newStorage(config);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storage.delete(COUNTRY, RECORD_KEY));
        assertEquals("Server request error: [URL=http://localhost:8767/v2/storage/records/us/463ca9fb48993ae6c598d58aa4a5e6c4e66610e869aff32916ba643387ad4afa, method=DELETE]", ex.getMessage());
        assertEquals("Read timed out", ex.getCause().getMessage());
        server.stop(0);
    }

    @Test
    void negativeTestWithIllegalPoolSize() {
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(1)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvironmentId("<envId>")
                .setMaxHttpPoolSize(0);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config));
        assertEquals("HTTP pool size can't be < 1. Expected 'null' or positive value, received=0", ex.getMessage());

        StorageConfig config1 = config
                .copy()
                .setMaxHttpPoolSize(-1);
        ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config1));
        assertEquals("HTTP pool size can't be < 1. Expected 'null' or positive value, received=-1", ex.getMessage());

        StorageConfig config2 = config
                .copy()
                .setMaxHttpPoolSize(20)
                .setMaxHttpConnectionsPerRoute(0);
        ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config2));
        assertEquals("Max HTTP connections count per route can't be < 1. Expected 'null' or positive value, received=0", ex.getMessage());

        StorageConfig config3 = config2
                .copy()
                .setMaxHttpConnectionsPerRoute(-1);
        ex = assertThrows(StorageClientException.class, () -> StorageImpl.newStorage(config3));
        assertEquals("Max HTTP connections count per route can't be < 1. Expected 'null' or positive value, received=-1", ex.getMessage());
    }

    @Test
    void negativeTestIllegalRecordKeysLength() throws StorageException {
        String generatedString = new SecureRandom().ints(97, 123) // from 'a' to 'z'
                .limit(300)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(1)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvironmentId("<envId>")
                .setMaxHttpPoolSize(1)
                .setHashSearchKeys(false);
        Storage storage = StorageImpl.newStorage(config);

        Record record = new Record(RECORD_KEY, BODY)
                .setKey10(generatedString);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.write(COUNTRY, record));
        assertEquals("key1-key20 length can't be more than 256 chars with option 'hashSearchKeys' = false", ex.getMessage());
        Record record1 = new Record(RECORD_KEY, BODY)
                .setKey10("generatedString");
        StorageServerException ex1 = assertThrows(StorageServerException.class, () -> storage.write(COUNTRY, record1));
        assertTrue(ex1.getMessage().startsWith("Server request error"));
    }

    @Test
    void searchKeysTest() throws StorageClientException {
        FindFilter filter = new FindFilter().keyEq(StringField.KEY1, "key");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> filter.searchKeysLike("search_keys"));
        assertEquals("SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY20 lookup", ex.getMessage());

        FindFilter filter2 = new FindFilter().searchKeysLike("search_keys");
        ex = assertThrows(StorageClientException.class, () -> filter2
                .keyEq(StringField.KEY1, "key"));
        assertEquals("SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY20 lookup", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new FindFilter()
                .searchKeysLike("se"));
        assertEquals("SEARCH_KEYS should contain at least 3 characters and be not longer than 200", ex.getMessage());

        String generatedString = new SecureRandom().ints(97, 123) // from 'a' to 'z'
                .limit(201)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        ex = assertThrows(StorageClientException.class, () -> new FindFilter()
                .searchKeysLike(generatedString));
        assertEquals("SEARCH_KEYS should contain at least 3 characters and be not longer than 200", ex.getMessage());
    }

    @RepeatedTest(3)
    void addAttachmentTest(RepetitionInfo repeatInfo) throws StorageException, IOException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String fileName = "sdk_incountry_unit_tests_file.txt";
        String fileId = "123456";
        String fileContent = "Hello world!";
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, new FakeHttpAgent(String.format("{\"file_id\":\"%s\"}", fileId))));
        Path tempFile = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        InputStream fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        AttachmentMeta attachmentMeta = storage.addAttachment("us", recordKey, fileInputStream, fileName, true);
        assertEquals(fileId, attachmentMeta.getFileId());

        fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        attachmentMeta = storage.addAttachment("us", recordKey, fileInputStream, fileName);
        assertEquals(fileId, attachmentMeta.getFileId());

        fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        attachmentMeta = storage.addAttachment("us", recordKey, fileInputStream, fileName, "text/plain");
        assertEquals(fileId, attachmentMeta.getFileId());

        fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        attachmentMeta = storage.addAttachment("us", recordKey, fileInputStream, fileName, true, "text/plain");
        assertEquals(fileId, attachmentMeta.getFileId());
        fileInputStream.close();
        Files.delete(tempFile);
    }

    @RepeatedTest(3)
    void deleteAttachmentTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String country = "us";
        String fileId = "1";
        FakeHttpAgent agent = new FakeHttpAgent("{}");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        assertTrue(storage.deleteAttachment(country, recordKey, fileId));
    }

    @RepeatedTest(3)
    void getAttachmentTest(RepetitionInfo repeatInfo) throws StorageException, IOException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String country = "us";
        String fileId = "123456";
        String fileContent = "Hello world!";
        String fileName = "sdk_incountry_unit_tests_file";
        String fileExtension = "txt";
        Path tempFile = Files.createTempFile(fileName, fileExtension);
        InputStream fileInputStream = Files.newInputStream(tempFile);
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));

        Map<MetaInfoTypes, String> metaInfo = new HashMap<>();
        metaInfo.put(MetaInfoTypes.NAME, fileName);
        String expectedResponse = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8.name());
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, new FakeHttpAgent(expectedResponse, metaInfo, Files.newInputStream(tempFile))));
        AttachedFile file = storage.getAttachmentFile(country, recordKey, fileId);

        assertEquals(expectedResponse, IOUtils.toString(file.getFileContent(), StandardCharsets.UTF_8.name()));
        assertEquals(fileName, file.getFileName());
    }

    @RepeatedTest(3)
    void updateAttachmentMetaTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String country = "us";
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
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));

        AttachmentMeta attachmentMeta = storage.updateAttachmentMeta(country, recordKey, fileId, fileName, null);
        assertEquals(gson.fromJson(response.toString(), AttachmentMeta.class), attachmentMeta);

        attachmentMeta = storage.updateAttachmentMeta(country, recordKey, fileId, null, mimeType);
        assertEquals(gson.fromJson(response.toString(), AttachmentMeta.class), attachmentMeta);

        attachmentMeta = storage.updateAttachmentMeta(country, recordKey, fileId, fileName, mimeType);
        assertEquals(gson.fromJson(response.toString(), AttachmentMeta.class), attachmentMeta);
    }

    @RepeatedTest(3)
    void getAttachmentMetaTest(RepetitionInfo repeatInfo) throws StorageException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        String recordKey = "key";
        String country = "us";
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
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setSecretKeyAccessor(secretKeyAccessor)
                .setApiKey("apiKey");
        Storage storage = StorageImpl.newStorage(config, new HttpDaoImpl(FAKE_ENDPOINT, null, null, agent));
        AttachmentMeta attachmentMeta = storage.getAttachmentMeta(country, recordKey, fileId);
        assertEquals(gson.fromJson(response.toString(), AttachmentMeta.class), attachmentMeta);
    }

    @Test
    void addAttachmentTestWithIllegalParams() throws StorageException, IOException {
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvironmentId("<envId>");
        Storage storage = StorageImpl.newStorage(config);
        String recordKey = "key";
        InputStream inputStream = null;
        String fileName = "sdk_incountry_unit_tests_file.txt";
        Path tempFile = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        try {
            inputStream = Files.newInputStream(tempFile);
            Files.write(tempFile, "Hello world!".getBytes(StandardCharsets.UTF_8));

            InputStream fileInputStream = inputStream;
            StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.addAttachment(null, recordKey, fileInputStream, fileName, false));
            assertEquals("Country can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("", recordKey, fileInputStream, fileName, false));
            assertEquals("Country can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", null, fileInputStream, fileName, false));
            assertEquals("Key can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", "", fileInputStream, fileName, false));
            assertEquals("Key can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", recordKey, null, fileName, false));
            assertEquals("Input stream can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", recordKey, new InputStream() {
                @Override
                public int read() {
                    return -1;
                }

                @Override
                public int available() {
                    return -1;
                }
            }, fileName, false));
            assertEquals("Input stream can't be null", ex.getMessage());
            ex = assertThrows(StorageClientException.class, () -> storage.addAttachment("us", recordKey, new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }

                @Override
                public int available() throws IOException {
                    throw new IOException();
                }
            }, fileName, false));
            assertEquals("Input stream is not available", ex.getMessage());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            Files.delete(tempFile);
        }
    }

    @RepeatedTest(3)
    void updateAttachmentMetaWithIllegalParams(RepetitionInfo repeatInfo) throws StorageClientException, StorageCryptoException {
        iterateLogLevel(repeatInfo, StorageImpl.class);
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvironmentId("<envId>");
        Storage storage = StorageImpl.newStorage(config);
        String recordKey = "key";
        String fileId = "123";
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.updateAttachmentMeta("us", recordKey, fileId, null, null));
        assertEquals("File name and MIME type can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.updateAttachmentMeta("us", recordKey, fileId, "", ""));
        assertEquals("File name and MIME type can't be null", ex.getMessage());
    }

    @Test
    void deleteAttachmentTestWithIllegalParams() throws StorageClientException, StorageCryptoException {
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setApiKey("<apiKey>")
                .setEnvironmentId("<envId>");
        Storage storage = StorageImpl.newStorage(config);
        String recordKey = "key";
        StorageClientException ex = assertThrows(StorageClientException.class, () -> storage.deleteAttachment("us", recordKey, null));
        assertEquals("File ID can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> storage.deleteAttachment("us", recordKey, ""));
        assertEquals("File ID can't be null", ex.getMessage());
    }

    @Test
    void nullConfigNegativeTest() {
        StorageClientException ex = assertThrows(StorageClientException.class, () ->
                StorageImpl.newStorage(null));
        assertEquals("Storage configuration is null", ex.getMessage());
    }

    @Test
    void oauthTokenPositive() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer("{}", 200, PORT);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setOauthToken("<token>")
                .setEnvironmentId("<envId>");
        Storage storage = StorageImpl.newStorage(config);
        assertTrue(storage.delete(COUNTRY, RECORD_KEY));
        server.stop(0);
    }

    @Test
    void oauthTokenNegative() throws StorageException, IOException {
        FakeHttpServer server = new FakeHttpServer("{}", 200, PORT);
        server.start();
        StorageConfig config = new StorageConfig()
                .setHttpTimeout(31)
                .setEndPoint("http://localhost:" + PORT)
                .setOauthTokenAccessor(() -> {
                    throw new NullPointerException();
                })
                .setEnvironmentId("<envId>");
        Storage storage = StorageImpl.newStorage(config);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> storage.delete(COUNTRY, RECORD_KEY));
        assertEquals("Unexpected error", ex.getMessage());
        assertTrue(ex.getCause() instanceof NullPointerException);
        server.stop(0);
    }
}
