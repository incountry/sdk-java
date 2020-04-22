package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.junit.jupiter.api.BeforeEach;
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
    private String batchWriteRecordKey = "batch_write_key" + TEMP;
    private String writeRecordKey = "write_key" + TEMP;
    private String profileKey = "profileKey" + TEMP;
    private String key2 = "key2" + TEMP;
    private String key3 = "key3" + TEMP;
    private Integer batchWriteRangeKey = 2;
    private Integer writeRangeKey = 1;
    private String recordBody = "test";

    private String secret = "passwordpasswordpasswordpassword";
    private int version = 0;
    private boolean isKey = true;
    private int currentVersion = 0;

    private static String loadFromEnv(String key) {
        return System.getenv(key);
    }

    @BeforeEach
    public void init() throws StorageServerException, StorageClientException {
        SecretKey secretKey = new SecretKey(secret, version, isKey);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, currentVersion);
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(() -> secretsData);
        storage = StorageImpl.getInstance(loadFromEnv(INTEGR_ENV_KEY_ENVID),
                loadFromEnv(INTEGR_ENV_KEY_APIKEY),
                loadFromEnv(INTEGR_ENV_KEY_ENDPOINT),
                secretKeyAccessor);
    }

    @Test
    @Order(1)
    public void batchWriteTest() throws StorageException {
        List<Record> records = new ArrayList<>();
        records.add(new Record(batchWriteRecordKey, recordBody, profileKey, batchWriteRangeKey, key2, key3));
        storage.batchWrite(country, records);
    }

    @Test
    @Order(2)
    public void writeTest() throws StorageException {
        Record record = new Record(writeRecordKey, recordBody, profileKey, writeRangeKey, key2, key3);
        storage.write(country, record);
    }

    @Test
    @Order(3)
    public void readTest() throws StorageException {
        Record incomingRecord = storage.read(country, writeRecordKey);
        assertEquals(writeRecordKey, incomingRecord.getKey());
        assertEquals(recordBody, incomingRecord.getBody());
        assertEquals(profileKey, incomingRecord.getProfileKey());
        assertEquals(key2, incomingRecord.getKey2());
        assertEquals(key3, incomingRecord.getKey3());
    }

    @Test
    @Order(4)
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(key2)
                .rangeKeyEq(writeRangeKey);
        BatchRecord batchRecord = storage.find(country, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(writeRecordKey, batchRecord.getRecords().get(0).getKey());
    }

    @Test
    @Order(5)
    public void findOneTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq(key2)
                .rangeKeyEq(writeRangeKey);
        Record record = storage.findOne(country, builder);
        assertEquals(writeRecordKey, record.getKey());
        assertEquals(recordBody, record.getBody());
    }

    @Test
    @Order(6)
    public void deleteTest() throws StorageException {
        storage.delete(country, writeRecordKey);
        storage.delete(country, batchWriteRecordKey);
        // Cannot read deleted record
        Record writeMethodRecord = storage.read(country, writeRecordKey);
        Record batchWriteMethodRecord = storage.read(country, batchWriteRecordKey);
        assertNull(writeMethodRecord);
        assertNull(batchWriteMethodRecord);
    }
}