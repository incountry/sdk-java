package com.incountry;

import com.incountry.crypto.Crypto;
import com.incountry.exceptions.FindOptionsException;
import com.incountry.exceptions.RecordException;
import com.incountry.exceptions.StorageException;
import com.incountry.exceptions.StorageServerException;
import com.incountry.key_accessor.SecretKeyAccessor;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(Enclosed.class)
public class StorageTest {

    @RunWith(Parameterized.class)
    public static class StorageTests {
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
        public Boolean encrypt;

        @Parameterized.Parameters(name = "{index} : withParams({0}, {1}, {2}, {3}, {4}, {5}, {6}) and encryption={7}")
        public static Iterable<Object[]> dataForTest() {
            return Arrays.asList(new Object[][]{
                    {"us", "key1", null, null, null, null, null, false},
                    {"us", "key1", "body", null, null, null, null, false},
                    {"us", "key1", "body", "key2", null, null, null, false},
                    {"us", "key1", "body", "key2", "key3", null, null, false},
                    {"us", "key1", "body", "key2", "key3", "profileKey", null, false},
                    {"us", "key1", "body", "key2", "key3", "profileKey", 1, false},
                    {"us", "key1", null, null, null, null, null, true},
                    {"us", "key1", "body", null, null, null, null, true},
                    {"us", "key1", "body", "key2", null, null, null, true},
                    {"us", "key1", "body", "key2", "key3", null, null, true},
                    {"us", "key1", "body", "key2", "key3", "profileKey", null, true},
                    {"us", "key1", "body", "key2", "key3", "profileKey", 1, true},
            });
        }

        @Test
        public void testWrite() throws GeneralSecurityException, StorageException, IOException, RecordException {
            if (encrypt) {
                SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor("password");
                storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", true, secretKeyAccessor);
                crypto = new Crypto("password", "envId");
            } else {
                storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", false, null);
                crypto = new Crypto("envId");
            }

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
        public void testRead() throws GeneralSecurityException, IOException, StorageException, RecordException {
            if (encrypt) {
                SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor("password");
                storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", true, secretKeyAccessor);
                crypto = new Crypto("password", "envId");
            } else {
                storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", false, null);
                crypto = new Crypto("envId");
            }

            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            String keyHash = crypto.createKeyHash(key);
            String expectedPath = "/v2/storage/records/"+ country + "/" + keyHash;

            FakeHttpAgent agent = new FakeHttpAgent(record.toString(crypto));
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
        public void testDelete() throws GeneralSecurityException, StorageException, IOException {
            if (encrypt) {
                SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor("password");
                storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", true, secretKeyAccessor);
                crypto = new Crypto("password", "envId");
            } else {
                storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", false, null);
                crypto = new Crypto("envId");
            }

            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            storage.delete(country, key);

            String keyHash = crypto.createKeyHash(key);
            String expectedPath = "/v2/storage/records/"+ country + "/" + keyHash;

            String callPath = new URL(agent.getCallEndpoint()).getPath();

            assertEquals(expectedPath, callPath);
        }
    }

    public static class StorageSingleTests {
        private String country = "us";
        private String recordKey = "some_key";
        private String profileKey = "profileKey";
        private String key2 = "key2";
        private String key3 = "key3";
        private Integer rangeKey = 1;
        private String recordBody = "{\"name\":\"last\"}";

        @Test
        public void testCustomEndpoint() throws GeneralSecurityException, StorageException, IOException {
            SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor("password");
            String endpoint = "https://custom.endpoint.io";

            Storage storage = new Storage("envId", "apiKey", endpoint, true, secretKeyAccessor);
            Crypto crypto = new Crypto("password", "envId");


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
            SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor("password");
            Storage storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", true, secretKeyAccessor);
            Crypto crypto = new Crypto("password", "envId");

            FindOptions options = new FindOptions(1,0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));


            Record rec = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encrypted = rec.toString(crypto);
            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
            storage.setHttpAgent(agent);

            BatchRecord d = storage.find(country, filter, options);

            String callBody = agent.getCallBody();
//            assertEquals("{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}", callBody);

            assertEquals(1, d.getCount());
            assertEquals(1, d.getRecords().length);
            assertEquals(recordKey, d.getRecords()[0].getKey());
            assertEquals(recordBody, d.getRecords()[0].getBody());
            assertEquals(key2, d.getRecords()[0].getKey2());
            assertEquals(key3, d.getRecords()[0].getKey3());
            assertEquals(profileKey, d.getRecords()[0].getProfileKey());
            assertEquals(rangeKey, d.getRecords()[0].getRangeKey());
        }

        @Test
        public void testFindOne() throws GeneralSecurityException, StorageException, IOException, FindOptionsException {
            String secret = "password";
            SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor(secret);
            Crypto crypto = new Crypto(secret, "envId");
            Storage storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", true, secretKeyAccessor);


            FindOptions options = new FindOptions(1,0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));


            Record rec = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encrypted = rec.toString(crypto);
            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":["+ encrypted +"],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}");
            storage.setHttpAgent(agent);

            Record foundRecord = storage.findOne(country, filter, options);

            String callBody = agent.getCallBody();
//            assertEquals("{\"filter\":{\"profile_key\":[\"" + crypto.createKeyHash(profileKey) + "\"]},\"options\":{\"limit\":1,\"offset\":0}}", callBody);

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
            Crypto crypto = new Crypto(secret, "envId");
            SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor(secret);
            Crypto cryptoOther = new Crypto("otherpassword", "envId");
            Storage storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", true, secretKeyAccessor);

            FindOptions options = new FindOptions(2, 0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));

            Record recOtherEnc = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            Record recEnc = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encryptedRecOther = recOtherEnc.toString(cryptoOther);
            String encryptedRec = recEnc.toString(crypto);

            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedRecOther + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
            storage.setHttpAgent(agent);

            BatchRecord d = storage.find(country, filter, options);

            assertEquals(1, d.getErrors().size());
//            assertEquals(encryptedRecOther, d.getErrors().get(0).getRawData());
            assertEquals("Record Parse Exception", d.getErrors().get(0).getMessage());

            assertEquals(1, d.getRecords().length);
            assertEquals(recordKey, d.getRecords()[0].getKey());
            assertEquals(recordBody, d.getRecords()[0].getBody());
            assertEquals(key2, d.getRecords()[0].getKey2());
            assertEquals(key3, d.getRecords()[0].getKey3());
            assertEquals(profileKey, d.getRecords()[0].getProfileKey());
            assertEquals(rangeKey, d.getRecords()[0].getRangeKey());
        }

        @Test
        public void testFindWithoutEncWithEncryptedData() throws FindOptionsException, GeneralSecurityException, StorageException, IOException {
            Storage storage = new Storage("envId", "apiKey", "https://custom.endpoint.io", false, null);
            Crypto cryptoWithEnc = new Crypto("password", "envId");
            Crypto cryptoWithPT = new Crypto("envId");

            FindOptions options = new FindOptions(2, 0);
            FindFilter filter = new FindFilter();
            filter.setProfileKeyParam(new FilterStringParam(profileKey));

            Record reсWithEnc = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            Record recWithPTEnc = new Record(country, recordKey, recordBody, profileKey, rangeKey, key2, key3);
            String encryptedRec = reсWithEnc.toString(cryptoWithEnc);
            String encryptedPTRec = recWithPTEnc.toString(cryptoWithPT);

            FakeHttpAgent agent = new FakeHttpAgent("{\"data\":[" + encryptedRec + "," + encryptedPTRec + "],\"meta\":{\"count\":2,\"limit\":10,\"offset\":0,\"total\":2}}");
            storage.setHttpAgent(agent);

            BatchRecord d = storage.find(country, filter, options);

            assertEquals(1, d.getErrors().size());
//            assertEquals(encryptedRec, d.getErrors().get(0).getRawData());
            assertEquals("Record Parse Exception", d.getErrors().get(0).getMessage());

            assertEquals(1, d.getRecords().length);
            assertEquals(recordKey, d.getRecords()[0].getKey());
            assertEquals(recordBody, d.getRecords()[0].getBody());
            assertEquals(key2, d.getRecords()[0].getKey2());
            assertEquals(key3, d.getRecords()[0].getKey3());
            assertEquals(profileKey, d.getRecords()[0].getProfileKey());
            assertEquals(rangeKey, d.getRecords()[0].getRangeKey());
        }
    }
}