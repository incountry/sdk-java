package com.incountry.residence.sdk;

import com.incountry.residence.sdk.crypto.testimpl.FernetCrypto;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StorageIntegrationTest {

    private static final String INTEGR_ENV_KEY_COUNTRY = "INT_INC_COUNTRY";
    private static final String INTEGR_ENV_KEY_ENVID = "INT_INC_ENVIRONMENT_ID";
    private static final String INTEGR_ENV_KEY_ENDPOINT = "INT_INC_ENDPOINT";
    private static final String INTEGR_ENV_KEY_APIKEY = "INT_INC_API_KEY";

    private static final String TEMP = new StringBuilder("-javasdk-")
            .append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
            .append("-")
            .append(UUID.randomUUID().toString().replace("-", ""))
            .toString();

    private Storage storage;
    private String country = loadFromEnv(INTEGR_ENV_KEY_COUNTRY);
    private String batchWriteKey = "BatchWriteKey" + TEMP;
    private String writeKey = "Write_Key" + TEMP;
    private String profileKey = "ProfileKey" + TEMP;
    private String key2 = "Key2" + TEMP;
    private String key3 = "Key3" + TEMP;
    private Integer batchWriteRangeKey = 2;
    private Integer writeRangeKey = 1;
    private String recordBody = "test";

    private String secret = "passwordpasswordpasswordpassword";
    private int version = 0;
    private boolean isCustom = false;
    private int currentVersion = 0;

    private static String loadFromEnv(String key) {
        return System.getenv(key);
    }


    public StorageIntegrationTest() throws StorageServerException, StorageClientException {
        SecretKey secretKey = new SecretKey(secret, version, isCustom);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, currentVersion);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        storage = StorageImpl.getInstance(loadFromEnv(INTEGR_ENV_KEY_ENVID),
                loadFromEnv(INTEGR_ENV_KEY_APIKEY),
                loadFromEnv(INTEGR_ENV_KEY_ENDPOINT),
                secretKeyAccessor);
    }

    @Test
    @Order(100)
    public void batchWriteTest() throws StorageException {
        List<Record> records = new ArrayList<>();
        records.add(new Record(batchWriteKey, recordBody, profileKey, batchWriteRangeKey, key2, key3));
        storage.batchWrite(country, records);
    }

    @Test
    @Order(200)
    public void writeTest() throws StorageException {
        Record record = new Record(writeKey, recordBody, profileKey, writeRangeKey, key2, key3);
        storage.write(country, record);
    }

    @Test
    @Order(300)
    public void readTest() throws StorageException {
        Record incomingRecord = storage.read(country, writeKey);
        assertEquals(writeKey, incomingRecord.getKey());
        assertEquals(recordBody, incomingRecord.getBody());
        assertEquals(profileKey, incomingRecord.getProfileKey());
        assertEquals(key2, incomingRecord.getKey2());
        assertEquals(key3, incomingRecord.getKey3());
    }

    @Test
    @Order(400)
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(writeKey)
                .key2Eq(key2)
                .key3Eq(key3)
                .profileKeyEq(profileKey)
                .rangeKeyEq(writeRangeKey);
        BatchRecord batchRecord = storage.find(country, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(writeKey, batchRecord.getRecords().get(0).getKey());

        builder.clear()
                .keyEq(batchWriteKey)
                .key2Eq(key2)
                .key3Eq(key3)
                .profileKeyEq(profileKey)
                .rangeKeyEq(batchWriteRangeKey);
        batchRecord = storage.find(country, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(batchWriteKey, batchRecord.getRecords().get(0).getKey());
    }

    @Test
    @Order(401)
    public void findAdvancedTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(key2)
                .rangeKeyEq(writeRangeKey, batchWriteRangeKey, writeRangeKey + batchWriteRangeKey + 1);
        BatchRecord batchRecord = storage.find(country, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        List<String> resultIdList = new ArrayList<>();
        resultIdList.add(batchRecord.getRecords().get(0).getKey());
        resultIdList.add(batchRecord.getRecords().get(1).getKey());
        assertTrue(resultIdList.contains(writeKey));
        assertTrue(resultIdList.contains(batchWriteKey));
    }

    @Test
    @Order(500)
    public void findOneTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(key2)
                .rangeKeyEq(writeRangeKey);
        Record record = storage.findOne(country, builder);
        assertEquals(writeKey, record.getKey());
        assertEquals(recordBody, record.getBody());
    }

    @Test
    @Order(600)
    public void customEncryptionTest() throws StorageException {
        SecretKey defaultSecretKey = new SecretKey(secret, version, false);
        SecretKey customSecretKey = new SecretKey(secret, version + 1, true);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(defaultSecretKey);
        secretKeyList.add(customSecretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, customSecretKey.getVersion());
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        List<Crypto> cryptoList = new ArrayList<>();
        cryptoList.add(new FernetCrypto(true));
        StorageConfig config = new StorageConfig()
                .setEnvId(loadFromEnv(INTEGR_ENV_KEY_ENVID))
                .setApiKey(loadFromEnv(INTEGR_ENV_KEY_APIKEY))
                .setEndPoint(loadFromEnv(INTEGR_ENV_KEY_ENDPOINT))
                .setSecretKeyAccessor(secretKeyAccessor)
                .setCustomCryptoList(cryptoList);
        storage = StorageImpl.getInstance(config);
        //write record with custom enc
        String customRecordKey = writeKey + "_custom";
        Record record = new Record(customRecordKey, recordBody, profileKey, writeRangeKey, key2, key3);
        storage.write(country, record);
        //read record with custom enc
        Record record1 = storage.read(country, customRecordKey);
        assertEquals(record, record1);
        //read recorded record with default encryption
        Record record2 = storage.read(country, writeKey);
        assertEquals(recordBody, record2.getBody());
        //find record with custom enc
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(customRecordKey)
                .rangeKeyEq(writeRangeKey);
        Record record3 = storage.findOne(country, builder);
        assertEquals(record, record3);
        //delete record with custom enc
        storage.delete(country, customRecordKey);
        Record record4 = storage.read(country, customRecordKey);
        assertNull(record4);
    }

    @Test
    @Order(700)
    public void deleteTest() throws StorageException {
        storage.delete(country, writeKey);
        storage.delete(country, batchWriteKey);
        // Cannot read deleted record
        Record writeMethodRecord = storage.read(country, writeKey);
        Record batchWriteMethodRecord = storage.read(country, batchWriteKey);
        assertNull(writeMethodRecord);
        assertNull(batchWriteMethodRecord);
    }
}