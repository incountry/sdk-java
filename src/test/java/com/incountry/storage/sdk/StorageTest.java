package com.incountry.storage.sdk;

import com.google.gson.*;
import com.incountry.storage.sdk.tools.crypto.Crypto;
import com.incountry.storage.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.storage.sdk.dto.*;
import com.incountry.storage.sdk.tools.exceptions.StorageException;
import com.incountry.storage.sdk.tools.exceptions.StorageServerException;
import com.incountry.storage.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.storage.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.storage.sdk.tools.keyaccessor.key.SecretKeysData;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;

public class StorageTest {

    public static class StorageParamTests {

        private Crypto crypto;

        private String secret = "passwordpasswordpasswordpassword";
        private int version = 0;
        private int currentVersion = 0;

        public Storage initializeStorage(boolean isKey, boolean encrypt) throws StorageServerException {
            SecretKeyAccessor secretKeyAccessor = initializeSecretKeyAccessor(isKey);
            Storage storage;
            if (encrypt) {
                storage = new Storage(
                        "envId",
                        "apiKey",
                        secretKeyAccessor
                );
                crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");
            } else {
                storage = new Storage("envId", "apiKey", null);
                crypto = new CryptoImpl("envId");
            }
            return storage;
        }

        private SecretKeyAccessor initializeSecretKeyAccessor(boolean isKey) {
            SecretKeysData secretKeysData = new SecretKeysData();
            SecretKey secretKey = new SecretKey();
            secretKey.setSecret(secret);
            secretKey.setVersion(version);
            secretKey.setIsKey(isKey);
            List<SecretKey> secretKeyList = new ArrayList<>();
            secretKeyList.add(secretKey);
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
            Storage storage = initializeStorage(isKey, encrypt);

            String expectedPath = "/v2/storage/records/" + country;

            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            storage.write(record);

            String received = agent.getCallBody();
            String callPath = new URL(agent.getCallEndpoint()).getPath();

            Record receivedRecord = Record.fromString(received, null);

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

            Storage storage = initializeStorage(isKey, encrypt);

            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            String keyHash = crypto.createKeyHash(key);
            String expectedPath = "/v2/storage/records/" + country + "/" + keyHash;

            FakeHttpAgent agent = new FakeHttpAgent(record.toJsonString(crypto));
            storage.setHttpAgent(agent);

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

            Storage storage = initializeStorage(isKey, encrypt);
            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
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

            Storage storage = initializeStorage(isKey, encrypt);
            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            List<Record> records = new ArrayList<>();
            records.add(new Record(country, key, body, profileKey, rangeKey, key2, key3));
            storage.batchWrite(country, records);

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

    public static class StorageSingleTests {

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

            storage = new Storage(
                    environmentId,
                    apiKey,
                    secretKeyAccessor
            );

            crypto = new CryptoImpl(secretKeyAccessor.getKey(), environmentId);
        }

        @Test
        public void migrateTest() throws StorageException {
            Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            String encrypted = rec.toJsonString(crypto);
            String content = "{\"data\":[" + encrypted + "],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
            FakeHttpAgent agent = new FakeHttpAgent(content);
            storage.setHttpAgent(agent);
            BatchRecord batchRecord = BatchRecord.fromString(content, crypto);

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
            String encrypted = rec.toJsonString(crypto);
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

            Storage storage = new Storage(environmentId, apiKey, endpoint, true, secretKeyAccessor);
            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            storage.write(record);

            String expectedURL = endpoint + "/v2/storage/records/" + country;

            String realURL = new URL(agent.getCallEndpoint()).toString();

            assertEquals(expectedURL, realURL);
        }

        @Test
        public void testFindWithEnc() throws StorageException {
            FindOptions options = new FindOptions(1,0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));

            Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            String encrypted = rec.toJsonString(crypto);
            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
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
            FindOptions options = new FindOptions(1,0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));

            Record rec = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            String encrypted = rec.toJsonString(crypto);
            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
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
            String encryptedRecOther = recOtherEnc.toJsonString(cryptoOther);
            String encryptedRec = recEnc.toJsonString(crypto);

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
            String encryptedRec = reсWithEnc.toJsonString(cryptoWithEnc);
            String encryptedPTRec = recWithPTEnc.toJsonString(cryptoWithPT);

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
            String encrypted = record.toJsonString(crypto);
            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
            storage.setHttpAgent(agent);
            assertThrows(IllegalArgumentException.class, () -> storage.find(null, null, null));
            assertThrows(IllegalArgumentException.class, () -> storage.find(country, null, null));
            assertThrows(IllegalArgumentException.class, () -> storage.find(country, filter, null));
        }

        @Test
        public void testInitErrorOnInsufficientArgs() {
            SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(() -> new SecretKeysData());
            assertThrows(IllegalArgumentException.class, () ->  new Storage(null,null, secretKeyAccessor));
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
            Storage storage = new Storage(
                    environmentId,
                    apiKey,
                    null
            );
            assertThrows(StorageException.class, () -> storage.migrate(null, 100));
        }
    }
}
