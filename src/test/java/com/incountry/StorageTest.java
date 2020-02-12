package com.incountry;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.incountry.crypto.Crypto;
import com.incountry.crypto.impl.CryptoImpl;
import com.incountry.exceptions.StorageCryptoException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class StorageTest {

    @RunWith(Parameterized.class)
    public static class StorageParamTests {
        Storage storage;
        Crypto crypto;

        @Parameterized.Parameter(0)
        public String country;
        @Parameterized.Parameter(1)
        public String key;
        @Parameterized.Parameter(2)
        public String body;
        @Parameterized.Parameter(3)
        public String key2;
        @Parameterized.Parameter(4)
        public String key3;
        @Parameterized.Parameter(5)
        public String profileKey;
        @Parameterized.Parameter(6)
        public Integer rangeKey;
        @Parameterized.Parameter(7)
        public SecretKeyAccessor secretKeyAccessor;

        @Parameterized.Parameters(name = "{index}:withParams({0}, {1}, {2}")
        public static Iterable<Object[]> dataForTest() {
            return Arrays.asList(new Object[][]{
                {"us", "key1", null, null, null, null, null, initializeSecretKeyAccessorWithString()},
                {"us", "key1", "body", null, null, null, null, initializeSecretKeyAccessorWithString()},
                {"us", "key1", "body", "key2", null, null, null, initializeSecretKeyAccessorWithString()},
                {"us", "key1", "body", "key2", "key3", null, null, initializeSecretKeyAccessorWithString()},
                {"us", "key1", "body", "key2", "key3", "profileKey", null, initializeSecretKeyAccessorWithString()},
                {"us", "key1", "body", "key2", "key3", "profileKey", 1, initializeSecretKeyAccessorWithString()},
                {"us", "key1", null, null, null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue()},
                {"us", "key1", "body", null, null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue()},
                {"us", "key1", "body", "key2", null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue()},
                {"us", "key1", "body", "key2", "key3", null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue()},
                {"us", "key1", "body", "key2", "key3", "profileKey", null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue()},
                {"us", "key1", "body", "key2", "key3", "profileKey", 1, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue()},
                {"us", "key1", null, null, null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse()},
                {"us", "key1", "body", null, null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse()},
                {"us", "key1", "body", "key2", null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse()},
                {"us", "key1", "body", "key2", "key3", null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse()},
                {"us", "key1", "body", "key2", "key3", "profileKey", null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse()},
                {"us", "key1", "body", "key2", "key3", "profileKey", 1, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse()},
            });
        }

        private static SecretKeyAccessor initializeSecretKeyAccessorWithString() {
            return SecretKeyAccessor.getAccessor("passwordpasswordpasswordpassword");
        }

        private static SecretKeyAccessor initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue() {
            return SecretKeyAccessor.getAccessor(new SecretKeyGenerator <String>() {
                @Override
                public String generate() {
                    return "{\n" +
                            "  \"secrets\": [\n" +
                            "    {\n" +
                            "      \"secret\": \"passwordpasswordpasswordpassword\",\n" +
                            "      \"version\": 0,\n" +
                            "      \"isKey\": \"true\"\n" +
                            "    }\n" +
                            "  ],\n" +
                            "  \"currentVersion\": 0\n" +
                            "}";
                }
            });
        }

        private static SecretKeyAccessor initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse() {
            return SecretKeyAccessor.getAccessor(new SecretKeyGenerator <SecretKeysData>() {
                @Override
                public SecretKeysData generate() {
                    SecretKey secretKey = new SecretKey();
                    secretKey.setSecret("vsdvepcbsrwokvhgaqundycksywixhtq");
                    secretKey.setVersion(0);
                    secretKey.setIsKey(false);

                    List<SecretKey> secretKeyList = new ArrayList<>();
                    secretKeyList.add(secretKey);

                    SecretKeysData secretKeysData = new SecretKeysData();
                    secretKeysData.setSecrets(secretKeyList);
                    secretKeysData.setCurrentVersion(0);
                    return secretKeysData;
                }
            });
        }

        @Before
        public void initializeStorage() throws StorageServerException {
            storage = new Storage(
                    "envId",
                    "apiKey",
                    secretKeyAccessor

            );

            crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");

        }

        @Test
        public void writeTest() throws StorageServerException, StorageCryptoException {
            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            storage.write(record);

            String encrypted = agent.getCallBody();
            String keyHash = crypto.createKeyHash(key);
            JSONObject response = new JSONObject(encrypted);
            String key = response.getString("key");
            String encryptedBody = response.getString("body");
            String actualBodyStr = crypto.decrypt(encryptedBody, 0);
            JSONObject bodyJsonObj = new JSONObject(actualBodyStr);
            String actualBody = body != null ? bodyJsonObj.getString("payload") : null;
            assertEquals(keyHash, key);
            assertEquals(body, actualBody);
        }

        @Test
        public void readTest() throws StorageServerException, StorageCryptoException {
            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            FakeHttpAgent agent = new FakeHttpAgent(record.toJsonString(crypto));
            storage.setHttpAgent(agent);
            Record incomingRecord = storage.read(country, key);
            assertEquals(key, incomingRecord.getKey());
            assertEquals(body, incomingRecord.getBody());
            assertEquals(profileKey, incomingRecord.getProfileKey());
            assertEquals(key2, incomingRecord.getKey2());
            assertEquals(key3, incomingRecord.getKey3());
            assertEquals(rangeKey, incomingRecord.getRangeKey());
        }


        @Test
        public void deleteTest() throws StorageServerException, MalformedURLException {
            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            storage.delete(country, key);

            String keyHash = crypto.createKeyHash(key);
            String expectedPath = "/v2/storage/records/"+ country + "/" + keyHash;

            String callPath = new URL(agent.getCallEndpoint()).getPath();

            assertEquals(expectedPath, callPath);
        }

        @Test
        public void batchWriteTest() throws StorageServerException, StorageCryptoException {
            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            List<Record> records = new ArrayList<>();
            records.add(new Record(country, key, body, profileKey, rangeKey, key2, key3));
            storage.batchWrite(country, records);

            String encrypted = agent.getCallBody();
            String keyHash = crypto.createKeyHash(key);
            ArrayList<LinkedTreeMap> responseList = (ArrayList<LinkedTreeMap>) new Gson().fromJson(encrypted, HashMap.class).get("records");
            for (LinkedTreeMap response : responseList) {
                String key = (String) response.get("key");
                String encryptedBody = (String) response.get("body");
                String actualBodyStr = crypto.decrypt(encryptedBody, 0);
                JSONObject bodyJsonObj = new JSONObject(actualBodyStr);
                String actualBody = body != null ? bodyJsonObj.getString("payload") : null;
                assertEquals(keyHash, key);
                assertEquals(body, actualBody);
            }


        }
    }

    public static class StorageSingleTests {
        private Storage storage;
        private Crypto crypto;

        private String country = "US";
        private String recordKey = "some_key";
        private String profileKey = "profileKey";
        private String key2 = "key2";
        private String key3 = "key3";
        private Integer rangeKey = 1;
        private String recordBody = "{\"name\":\"last\"}";

        @Before
        public void initializeStorage() throws StorageServerException {
            SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretKeyGenerator <String>() {
                @Override
                public String generate() {
                    return "{\n" +
                            "  \"secrets\": [\n" +
                            "    {\n" +
                            "      \"secret\": \"123\",\n" +
                            "      \"version\": 0\n" +
                            "    }\n" +
                            "  ],\n" +
                            "  \"currentVersion\": 0\n" +
                            "}";
                }
            });
            storage = new Storage(
                    "envId",
                    "apiKey",
                    secretKeyAccessor

            );

            crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");
        }

        @Test
        public void migrateTest() throws StorageServerException, StorageCryptoException {
            Record rec = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
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

        @Test
        public void findTest() throws StorageServerException, StorageCryptoException {
            FindOptions options = new FindOptions(1,0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));


            Record rec = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encrypted = rec.toJsonString(crypto);
            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
            storage.setHttpAgent(agent);

            BatchRecord batchRecord = storage.find(country, filter, options);

            String callBody = agent.getCallBody();
            assertEquals("{\"filter\":{\"profile_key\":[\"ee597d2e9e8ed19fd1b891af76495586da223cdbd6251fdac201531451b3329d\"]},\"options\":{\"offset\":0,\"limit\":1}}", callBody);

            assertEquals(1, batchRecord.getCount());
            assertEquals(1, batchRecord.getRecords().size());
            assertEquals(recordKey, batchRecord.getRecords().get(0).getKey());
            assertEquals(recordBody, batchRecord.getRecords().get(0).getBody());
        }
    }
}