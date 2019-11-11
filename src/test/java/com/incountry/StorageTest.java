package com.incountry;

import com.incountry.crypto.Crypto;
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
import java.security.GeneralSecurityException;
import java.util.Arrays;

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

        @Parameterized.Parameters(name = "{index}:withParams({0}, {1}, {2}")
        public static Iterable<Object[]> dataForTest() {
            return Arrays.asList(new Object[][]{
                {"us", "key1", null, null, null, null, null},
                {"us", "key1", "body", null, null, null, null},
                {"us", "key1", "body", "key2", null, null, null},
                {"us", "key1", "body", "key2", "key3", null, null},
                {"us", "key1", "body", "key2", "key3", "profileKey", null},
                {"us", "key1", "body", "key2", "key3", "profileKey", 1},
            });
        }

        @Before
        public void initializeStorage() throws IOException, StorageServerException {
            SecretKeyAccessor secretKeyAccessor = new SecretKeyAccessor("password");
            storage = new Storage("envId", "apiKey", secretKeyAccessor);
            crypto = new Crypto("password", "envId");
        }

        @Test
        public void testWrite() throws GeneralSecurityException, StorageException, IOException {
            FakeHttpAgent agent = new FakeHttpAgent("");
            storage.setHttpAgent(agent);
            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            storage.write(record);

            String encrypted = agent.getCallBody();
            String keyHash = crypto.createKeyHash(key);
            JSONObject response = new JSONObject(encrypted);
            String key = response.getString("key");
            String encryptedBody = response.getString("body");
            String actualBodyStr = crypto.decrypt(encryptedBody);
            JSONObject bodyJsonObj = new JSONObject(actualBodyStr);
            String actualBody = body != null ? bodyJsonObj.getString("payload") : null;
            assertEquals(keyHash, key);
            assertEquals(body, actualBody);
        }

        @Test
        public void testRead() throws GeneralSecurityException, IOException, StorageException {
            Record record = new Record(country, key, body, profileKey, rangeKey, key2, key3);
            FakeHttpAgent agent = new FakeHttpAgent(record.toString(crypto));
            storage.setHttpAgent(agent);
            Record fetched = storage.read(country, key);
            assertEquals(key, fetched.getKey());
            assertEquals(body, fetched.getBody());
            assertEquals(profileKey, fetched.getProfileKey());
            assertEquals(key2, fetched.getKey2());
            assertEquals(key3, fetched.getKey3());
            assertEquals(rangeKey, fetched.getRangeKey());
        }
    }

    public static class StorageSingleTests {

        @Test
        public void testH1() {
            // U can use country, key and body here
            String hello = "world";
            assertEquals("world", hello);
        }
    }
}