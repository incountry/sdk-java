package com.incountry;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.incountry.crypto.Crypto;
import com.incountry.crypto.impl.CryptoImpl;
import com.incountry.exceptions.FindOptionsException;
import com.incountry.exceptions.StorageCryptoException;
import com.incountry.exceptions.StorageException;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
        @Parameterized.Parameter(8)
        public Boolean encrypt;

        @Parameterized.Parameters(name = "{index}:withParams({0}, {1}, {2}")
        public static Iterable<Object[]> dataForTest() {
            return Arrays.asList(new Object[][]{
                {"us", "key1", null, null, null, null, null, initializeSecretKeyAccessorWithString(), false},
                {"us", "key1", "body", null, null, null, null, initializeSecretKeyAccessorWithString(), false},
                {"us", "key1", "body", "key2", null, null, null, initializeSecretKeyAccessorWithString(), false},
                {"us", "key1", "body", "key2", "key3", null, null, initializeSecretKeyAccessorWithString(), true},
                {"us", "key1", "body", "key2", "key3", "profileKey", null, initializeSecretKeyAccessorWithString(), true},
                {"us", "key1", "body", "key2", "key3", "profileKey", 1, initializeSecretKeyAccessorWithString(), true},
                {"us", "key1", null, null, null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue(), true},
                {"us", "key1", "body", null, null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue(), true},
                {"us", "key1", "body", "key2", null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue(), true},
                {"us", "key1", "body", "key2", "key3", null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue(), false},
                {"us", "key1", "body", "key2", "key3", "profileKey", null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue(), false},
                {"us", "key1", "body", "key2", "key3", "profileKey", 1, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyTrue(), false},
                {"us", "key1", null, null, null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse(), false},
                {"us", "key1", "body", null, null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse(), false},
                {"us", "key1", "body", "key2", null, null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse(), false},
                {"us", "key1", "body", "key2", "key3", null, null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse(), true},
                {"us", "key1", "body", "key2", "key3", "profileKey", null, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse(), true},
                {"us", "key1", "body", "key2", "key3", "profileKey", 1, initializeSecretKeyAccessorWithSecretKeyGeneratorWithIsKeyFalse(), true},
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
        }

        @Test
        public void writeTest() throws StorageServerException, StorageCryptoException, MalformedURLException {
            String expectedPath = "/v2/storage/records/"+ country;

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

        @Test
        public void readTest() throws StorageServerException, StorageCryptoException, MalformedURLException {
            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            String keyHash = crypto.createKeyHash(key);
            String expectedPath = "/v2/storage/records/"+ country + "/" + keyHash;

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

        private String country = "us";
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
        public void migrateTest() throws StorageException {
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

        @Test
        public void testCustomEndpoint() throws GeneralSecurityException, StorageException, IOException {
            SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor("password");
            String endpoint = "https://custom.endpoint.io";


            Storage storage = new Storage("envId", "apiKey", endpoint, true, secretKeyAccessor);
            Crypto crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");
//            Crypto crypto = new CryptoImpl("password", "envId");


            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            Record record = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            storage.write(record);

            String expectedURL = endpoint + "/v2/storage/records/"+ country;

            String realURL = new URL(agent.getCallEndpoint()).toString();

            assertEquals(expectedURL, realURL);
        }

        @Test
        public void testFindWithEnc() throws GeneralSecurityException, StorageException, IOException, FindOptionsException {
            SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor("password");
            Storage storage = new Storage("envId", "apiKey", secretKeyAccessor);
            Crypto crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");
//            Crypto crypto = new Crypto("password", "envId");

            FindOptions options = new FindOptions(1,0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));


            Record rec = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encrypted = rec.toJsonString(crypto);
            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
            storage.setHttpAgent(agent);

            BatchRecord d = storage.find(country, filter, options);

            String callBody = agent.getCallBody();
            assertEquals("{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"offset\":0,\"limit\":1}}", callBody);

            assertEquals(1, d.getCount());
            assertEquals(1, d.getRecords().size());
            assertEquals(recordKey, d.getRecords().get(0).getKey());
            assertEquals(recordBody, d.getRecords().get(0).getBody());
            assertEquals(key2, d.getRecords().get(0).getKey2());
            assertEquals(key3, d.getRecords().get(0).getKey3());
            assertEquals(profileKey, d.getRecords().get(0).getProfileKey());
            assertEquals(rangeKey, d.getRecords().get(0).getRangeKey());
        }

        @Test
        public void testFindOne() throws GeneralSecurityException, StorageException, IOException, FindOptionsException {
            String secret = "password";
            SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(secret);
            Crypto crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");
            Storage storage = new Storage("envId", "apiKey", secretKeyAccessor);


            FindOptions options = new FindOptions(1,0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));


            Record rec = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encrypted = rec.toJsonString(crypto);
            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
            storage.setHttpAgent(agent);

            Record foundRecord = storage.findOne(country, filter, options);

            String callBody = agent.getCallBody();
            assertEquals("{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"offset\":0,\"limit\":1}}", callBody);

            assertEquals(recordKey, foundRecord.getKey());
            assertEquals(recordBody, foundRecord.getBody());
            assertEquals(key2, foundRecord.getKey2());
            assertEquals(key3, foundRecord.getKey3());
            assertEquals(profileKey, foundRecord.getProfileKey());
            assertEquals(rangeKey, foundRecord.getRangeKey());
        }

        @Test
        public void testFindWithEncByMultipleSecrets() throws GeneralSecurityException, StorageException, IOException, FindOptionsException {
            String secret = "password";
            SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(secret);
            Crypto crypto = new CryptoImpl(secretKeyAccessor.getKey(), "envId");
            Crypto cryptoOther = new CryptoImpl(SecretKeyAccessor.getAccessor("otherpassword").getKey(), "envId");
            Storage storage = new Storage("envId", "apiKey", secretKeyAccessor);

            FindOptions options = new FindOptions(2, 0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));

            Record recOtherEnc = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            Record recEnc = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encryptedRecOther = recOtherEnc.toJsonString(cryptoOther);
            String encryptedRec = recEnc.toJsonString(crypto);

            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
            storage.setHttpAgent(agent);

            BatchRecord d = storage.find(country, filter, options);

            assertEquals(1, d.getErrors().size());
            assertEquals(encryptedRecOther, d.getErrors().get(0).getRawData());
            assertEquals("Record Parse Exception", d.getErrors().get(0).getMessage());

            assertEquals(1, d.getRecords().size());
            assertEquals(recordKey, d.getRecords().get(0).getKey());
            assertEquals(recordBody, d.getRecords().get(0).getBody());
            assertEquals(key2, d.getRecords().get(0).getKey2());
            assertEquals(key3, d.getRecords().get(0).getKey3());
            assertEquals(profileKey, d.getRecords().get(0).getProfileKey());
            assertEquals(rangeKey, d.getRecords().get(0).getRangeKey());
        }

        @Test
        public void testFindWithoutEncWithEncryptedData() throws FindOptionsException, GeneralSecurityException, StorageException, IOException {
            Storage storage = new Storage("envId", "apiKey", null);
            Crypto cryptoWithEnc = new CryptoImpl(SecretKeyAccessor.getAccessor("password").getKey(), "envId");
            Crypto cryptoWithPT = new CryptoImpl("envId");

            FindOptions options = new FindOptions(2, 0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));

            Record reсWithEnc = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            Record recWithPTEnc = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encryptedRec = reсWithEnc.toJsonString(cryptoWithEnc);
            String encryptedPTRec = recWithPTEnc.toJsonString(cryptoWithPT);

            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
            storage.setHttpAgent(agent);

            BatchRecord d = storage.find(country, filter, options);

            assertEquals(1, d.getErrors().size());
            assertEquals(encryptedRec, d.getErrors().get(0).getRawData());
            assertEquals("Record Parse Exception", d.getErrors().get(0).getMessage());

            assertEquals(1, d.getRecords().size());
            assertEquals(recordKey, d.getRecords().get(0).getKey());
            assertEquals(recordBody, d.getRecords().get(0).getBody());
            assertEquals(key2, d.getRecords().get(0).getKey2());
            assertEquals(key3, d.getRecords().get(0).getKey3());
            assertEquals(profileKey, d.getRecords().get(0).getProfileKey());
            assertEquals(rangeKey, d.getRecords().get(0).getRangeKey());
        }

    }
}