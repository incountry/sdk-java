//package com.incountry;
//
//import com.incountry.crypto.impl.Crypto;
//import com.incountry.exceptions.StorageException;
//import com.incountry.exceptions.StorageServerException;
//import com.incountry.keyaccessor.impl.SecretKeyAccessorImpl;
//import org.json.JSONObject;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.experimental.runners.Enclosed;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//
//import java.io.IOException;
//import java.net.URL;
//import java.security.GeneralSecurityException;
//import java.util.Arrays;
//
//import static org.junit.Assert.assertEquals;
//
//@RunWith(Enclosed.class)
//public class StorageTest {
//
//    @RunWith(Parameterized.class)
//    public static class StorageParamTests {
//        Storage storage;
//        Crypto crypto;
//
//        @Parameterized.Parameter(0)
//        public String country;
//        @Parameterized.Parameter(1)
//        public String key;
//        @Parameterized.Parameter(2)
//        public String body;
//        @Parameterized.Parameter(3)
//        public String key2;
//        @Parameterized.Parameter(4)
//        public String key3;
//        @Parameterized.Parameter(5)
//        public String profileKey;
//        @Parameterized.Parameter(6)
//        public Integer rangeKey;
//
//        @Parameterized.Parameters(name = "{index}:withParams({0}, {1}, {2}")
//        public static Iterable<Object[]> dataForTest() {
//            return Arrays.asList(new Object[][]{
//                {"us", "key1", null, null, null, null, null},
//                {"us", "key1", "body", null, null, null, null},
//                {"us", "key1", "body", "key2", null, null, null},
//                {"us", "key1", "body", "key2", "key3", null, null},
//                {"us", "key1", "body", "key2", "key3", "profileKey", null},
//                {"us", "key1", "body", "key2", "key3", "profileKey", 1},
//            });
//        }
//
//        @Before
//        public void initializeStorage() throws IOException, StorageServerException {
//            SecretKeyAccessorImpl secretKeyAccessorImpl = new SecretKeyAccessorImpl("password");
//            storage = new Storage("envId", "apiKey", secretKeyAccessorImpl);
//            crypto = new Crypto("password", "envId");
//        }
//
//        @Test
//        public void testWrite() throws GeneralSecurityException, StorageException, IOException {
//            FakeHttpAgent agent = new FakeHttpAgent("");
//            storage.setHttpAgent(agent);
//            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
//            storage.write(record);
//
//            String encrypted = agent.getCallBody();
//            String keyHash = crypto.createKeyHash(key);
//            JSONObject response = new JSONObject(encrypted);
//            String key = response.getString("key");
//            String encryptedBody = response.getString("body");
//            String actualBodyStr = crypto.decrypt(encryptedBody);
//            JSONObject bodyJsonObj = new JSONObject(actualBodyStr);
//            String actualBody = body != null ? bodyJsonObj.getString("payload") : null;
//            assertEquals(keyHash, key);
//            assertEquals(body, actualBody);
//        }
//
//        @Test
//        public void testRead() throws GeneralSecurityException, IOException, StorageException {
//            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
//            FakeHttpAgent agent = new FakeHttpAgent(record.toString(crypto));
//            storage.setHttpAgent(agent);
//            Record fetched = storage.read(country, key);
//            assertEquals(key, fetched.getKey());
//            assertEquals(body, fetched.getBody());
//            assertEquals(profileKey, fetched.getProfileKey());
//            assertEquals(key2, fetched.getKey2());
//            assertEquals(key3, fetched.getKey3());
//            assertEquals(rangeKey, fetched.getRangeKey());
//        }
//
//
//        @Test
//        public void testDelete() throws GeneralSecurityException, StorageException, IOException {
//            FakeHttpAgent agent = new FakeHttpAgent("");
//            storage.setHttpAgent(agent);
//            storage.delete(country, key);
//
//            String keyHash = crypto.createKeyHash(key);
//            String expectedPath = "/v2/storage/records/"+ country + "/" + keyHash;
//
//            String callPath = new URL(agent.getCallEndpoint()).getPath();
//
//            assertEquals(expectedPath, callPath);
//        }
//    }
//
//    public static class StorageSingleTests {
//        Storage storage;
//        Crypto crypto;
//
//        private Storage store;
//        private String country = "US";
//        private String recordKey = "some_key";
//        private String profileKey = "profileKey";
//        private String key2 = "key2";
//        private String key3 = "key3";
//        private Integer rangeKey = 1;
//        private String recordBody = "{\"name\":\"last\"}";
//
//        @Before
//        public void initializeStorage() throws IOException, StorageServerException {
//            SecretKeyAccessorImpl secretKeyAccessorImpl = new SecretKeyAccessorImpl("password");
//            storage = new Storage("envId", "apiKey", secretKeyAccessorImpl);
//            crypto = new Crypto("password", "envId");
//        }
//
//        @Test
//        public void testFind() throws FindOptions.FindOptionsException, GeneralSecurityException, StorageException, IOException {
//            FindOptions options = new FindOptions(1,0);
//            FindFilter filter = new FindFilter();
//            filter.setProfileKeyParam(new FilterStringParam(profileKey));
//
//
//            Record rec = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
//            String encrypted = rec.toString(crypto);
//            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
//            storage.setHttpAgent(agent);
//
//            BatchRecord d = storage.find(country, filter, options);
//
//            String callBody = agent.getCallBody();
//            assertEquals("{\"filter\":{\"profile_key\":[\"ee597d2e9e8ed19fd1b891af76495586da223cdbd6251fdac201531451b3329d\"]},\"options\":{\"offset\":0,\"limit\":1}}", callBody);
//
//            assertEquals(1, d.getCount());
//            assertEquals(1, d.getRecords().length);
//            assertEquals(recordKey, d.getRecords()[0].getKey());
//            assertEquals(recordBody, d.getRecords()[0].getBody());
//        }
//    }
//}