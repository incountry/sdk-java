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
            "    \"record_key\": \"976143aa1fd12b9ad7449fd9d3a6d25347d71b890b49d4fb5c738e798238865f\",\n" +
            "    \"body\": \"2:vJdEK6XMEmvstvlz6DyRzl4yS6UG1tVG61uIoNaHurh6v8bIeb1o1LBBtVLWBTpvMzsV8RE7s2g9hQcjv7lZP/UqDKNNK61du+nOV6X4IuFGLTi5zVJo3kqJ7fK0Kv9iHzTx/PxIZUjsVw/wf/7XRMTZ+u/xNgj7uiIu5Adt8ky390T/h29dketkK6l+7Nn2QIe6ilPuyw9S3kzM5+AcCssigdp2yHAhbWVwDs4Vr33pqtTfsqzMfyOvaDfmn2S1fv2AK5jdvXR/okeRRRH263j68GUkGuHtZmAcynQBhilAyfK5qKVE4sqIxMk1j0W+vYNN8p2l\",\n" +
            "    \"key2\": \"abcb2ad9e9e0b1787f262b014f517ad1136f868e7a015b1d5aa545b2f575640d\",\n" +
            "    \"key3\": \"1102ae53e55f0ce1d802cc8bb66397e7ea749fd8d05bd2d4d0f697cedaf138e3\",\n" +
            "    \"profile_key\": \"f5b5ae4914972ace070fa51b410789324abe063dbe2bb09801410d9ab54bf833\",\n" +
            "    \"range_key1\": 6275438399,\n" +
            "    \"version\": 0\n" +
            "}";

    private final CryptoManager cryptoManager;
    private final Record originalRecord;

    AllSdkEncryptionTest() throws StorageClientException {
        cryptoManager = new CryptoManager(() -> SecretsDataGenerator.fromPassword(PASSWORD), ENV_ID, null, false);
        originalRecord = new Record()
                .setRecordKey("InCountryKey")
                .setBody("{\"data\": \"InCountryBody\"}")
                .setProfileKey("InCountryPK")
                .setRangeKey1(6275438399L)
                .setKey2("InCountryKey2")
                .setKey3("InCountryKey3");
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
        assertEquals(originalJsonObject.get("record_key"), jsonObject.get("record_key"));
        assertEquals(originalJsonObject.get("key2"), jsonObject.get("key2"));
        assertEquals(originalJsonObject.get("key3"), jsonObject.get("key3"));
        assertEquals(originalJsonObject.get("range_key1"), jsonObject.get("range_key1"));
        assertEquals(originalJsonObject.get("profile_key"), jsonObject.get("profile_key"));
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
