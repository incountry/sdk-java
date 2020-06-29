package com.incountry.residence.sdk.crypto;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AllSdkEncryptionTest {

    private static final String ENV_ID = "InCountry";
    private static final String PASSWORD = "password";
    private static final String RESPONSE = "{\n" +
            "    \"key\": \"976143aa1fd12b9ad7449fd9d3a6d25347d71b890b49d4fb5c738e798238865f\",\n" +
            "    \"body\": \"2:IGJNCmV+RXZydaPxDjjhZ80/6aZ2vcEUZ2GuOzKgVSSdM6gYf5RPgFbyLqv+7ihz0CpYFQQWf9xkIyD/u3VYky8dWLq+NXcE2xYL4/U7LqUZmJPQzgcQCABYQ/8vOvUEcrfOAwzGjR6etTp1ki+79JmCEZFSNqcDP1GZXNLFdLoSUp1X2wVlH9ukhJ4jrE0cKDrpJllswRSOz0BhS8PA/73KNKwo718t7fPWpUm7RkyILwYTd/LpPvpXMS6JypEns8fviOpbCLQPpZNBe6zpwbFf3C0qElHlbCyPbDyUiMzVKOwWlYFpozFcRyWegjJ42T8v52+GuRY5\",\n" +
            "    \"key2\": \"abcb2ad9e9e0b1787f262b014f517ad1136f868e7a015b1d5aa545b2f575640d\",\n" +
            "    \"key3\": \"1102ae53e55f0ce1d802cc8bb66397e7ea749fd8d05bd2d4d0f697cedaf138e3\",\n" +
            "    \"profile_key\": \"f5b5ae4914972ace070fa51b410789324abe063dbe2bb09801410d9ab54bf833\",\n" +
            "    \"range_key\": 6275438399,\n" +
            "    \"version\": 0\n" +
            "}";

    private final CryptoManager cryptoManager;
    private final Record originalRecord;

    AllSdkEncryptionTest() throws StorageClientException {
        cryptoManager = new CryptoManager(() -> SecretsDataGenerator.fromPassword(PASSWORD), ENV_ID, null, false);
        originalRecord = new Record("InCountryKey", "{\"data\": \"InCountryBody\"}", "InCountryPK", 6275438399L, "InCountryKey2", "InCountryKey3");
    }

    @Test
    void testDecryptionFromOtherSDK() throws StorageServerException, StorageClientException, StorageCryptoException {
        Record record = JsonUtils.recordFromString(RESPONSE, cryptoManager);
        assertEquals(originalRecord, record);
    }

    @Test
    void testEncryptionFromOtherSDK() throws StorageClientException, StorageCryptoException {
        String recordJsonString = JsonUtils.toJsonString(originalRecord, cryptoManager);
        JsonObject jsonObject = getGson().fromJson(recordJsonString, JsonObject.class);
        JsonObject originalJsonObject = getGson().fromJson(RESPONSE, JsonObject.class);
        assertEquals(originalJsonObject.get("key"), jsonObject.get("key"));
        assertEquals(originalJsonObject.get("key2"), jsonObject.get("key2"));
        assertEquals(originalJsonObject.get("key3"), jsonObject.get("key3"));
        assertEquals(originalJsonObject.get("rangeKey"), jsonObject.get("rangeKey"));
        assertEquals(originalJsonObject.get("profileKey"), jsonObject.get("profileKey"));
        assertEquals(originalJsonObject.get("version"), jsonObject.get("version"));
        assertNotEquals(originalJsonObject.get("body"), jsonObject.get("body"));
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .disableHtmlEscaping()
                .create();
    }
}
