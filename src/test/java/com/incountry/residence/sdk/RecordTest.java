package com.incountry.residence.sdk;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.incountry.residence.sdk.crypto.SecretsData;
import com.incountry.residence.sdk.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.DtoTransformer;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordTest {
    private static final String BODY = "body";
    private static final String RECORD_KEY = "recordKey1";
    private static final String PARENT_KEY = "parentKey";
    private static final String KEY_1 = "key1";
    private static final String KEY_2 = "key2";
    private static final String KEY_3 = "key3";
    private static final String KEY_4 = "key4";
    private static final String KEY_5 = "key5";
    private static final String KEY_6 = "key6";
    private static final String KEY_7 = "key7";
    private static final String KEY_8 = "key8";
    private static final String KEY_9 = "key9";
    private static final String KEY_10 = "key10";
    private static final String KEY_11 = "key11";
    private static final String KEY_12 = "key12";
    private static final String KEY_13 = "key13";
    private static final String KEY_14 = "key14";
    private static final String KEY_15 = "key15";
    private static final String KEY_16 = "key16";
    private static final String KEY_17 = "key17";
    private static final String KEY_18 = "key18";
    private static final String KEY_19 = "key19";
    private static final String KEY_20 = "key20";
    private static final String PROFILE_KEY = "profileKey";
    private static final Long RANGE_KEY_1 = 1L;
    private static final Long RANGE_KEY_2 = 2L;
    private static final Long RANGE_KEY_3 = 3L;
    private static final Long RANGE_KEY_4 = 4L;
    private static final Long RANGE_KEY_5 = 5L;
    private static final Long RANGE_KEY_6 = 6L;
    private static final Long RANGE_KEY_7 = 7L;
    private static final Long RANGE_KEY_8 = 8L;
    private static final Long RANGE_KEY_9 = 9L;
    private static final Long RANGE_KEY_10 = 10L;
    private static final String PRECOMMIT_BODY = "precommit";
    private static final String SERVICE_KEY_1 = "serviceKey1";
    private static final String SERVICE_KEY_2 = "serviceKey2";
    private static final String SERVICE_KEY_3 = "serviceKey3";
    private static final String SERVICE_KEY_4 = "serviceKey4";
    private static final String SERVICE_KEY_5 = "serviceKey5";
    private static final Date EXPIRES_AT = new Date();

    @Test
    void findResultToStringPositive() {
        Record record1 = new Record(RECORD_KEY + 1, BODY + 1)
                .setProfileKey(PROFILE_KEY + 1)
                .setRangeKey1(RANGE_KEY_1 + 1)
                .setKey2(KEY_2 + 1)
                .setKey3(KEY_3 + 1);
        Record record2 = new Record(RECORD_KEY + 2, BODY + 2)
                .setProfileKey(PROFILE_KEY + 2)
                .setRangeKey1(RANGE_KEY_1 + 2)
                .setKey2(KEY_2 + 2)
                .setKey3(KEY_3 + 2);
        FindResult findResult = new FindResult(Arrays.asList(record1, record2), 2, 2, 0, 2, new ArrayList<>());
        String str = findResult.toString();
        assertTrue(str.contains(String.valueOf(record1.hashCode())));
        assertTrue(str.contains(String.valueOf(record2.hashCode())));
    }

    @SuppressWarnings("java:S3415")
    @Test
    void equalsPositive() {
        Record record1 = new Record(RECORD_KEY)
                .setParentKey(PARENT_KEY)
                .setKey1(KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3)
                .setKey4(KEY_4)
                .setKey5(KEY_5)
                .setKey6(KEY_6)
                .setKey7(KEY_7)
                .setKey8(KEY_8)
                .setKey9(KEY_9)
                .setKey10(KEY_10)
                .setKey11(KEY_11)
                .setKey12(KEY_12)
                .setKey13(KEY_13)
                .setKey14(KEY_14)
                .setKey15(KEY_15)
                .setKey16(KEY_16)
                .setKey17(KEY_17)
                .setKey18(KEY_18)
                .setKey19(KEY_19)
                .setKey20(KEY_20)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(RANGE_KEY_1)
                .setRangeKey2(RANGE_KEY_2)
                .setRangeKey3(RANGE_KEY_3)
                .setRangeKey4(RANGE_KEY_4)
                .setRangeKey5(RANGE_KEY_5)
                .setRangeKey6(RANGE_KEY_6)
                .setRangeKey7(RANGE_KEY_7)
                .setRangeKey8(RANGE_KEY_8)
                .setRangeKey9(RANGE_KEY_9)
                .setRangeKey10(RANGE_KEY_10)
                .setBody(BODY)
                .setServiceKey1(SERVICE_KEY_1)
                .setServiceKey2(SERVICE_KEY_2)
                .setServiceKey3(SERVICE_KEY_3)
                .setServiceKey4(SERVICE_KEY_4)
                .setServiceKey5(SERVICE_KEY_5)
                .setExpiresAt(EXPIRES_AT)
                .setPrecommitBody(PRECOMMIT_BODY);

        String attachmentMetaJson = "{\n" +
                "   \"downloadLink\":\"123456\",\n" +
                "   \"fileId\":\"some_link\",\n" +
                "   \"fileName\":\"test_file\",\n" +
                "   \"hash\":\"1234567890\",\n" +
                "   \"mimeType\":\"text/plain\",\n" +
                "   \"size\":1000\n" +
                "}";

        AttachmentMeta meta = new Gson().fromJson(attachmentMetaJson, AttachmentMeta.class);
        record1.getAttachments().add(meta);

        Record record2 = record1.copy();
        assertEquals(record1, record1);
        assertEquals(record1, record2);
        assertEquals(record2, record1);
        assertNotEquals(null, record1);
        assertNotEquals(record1, null);
        assertNotEquals(record1, UUID.randomUUID());
        record2.getAttachments().clear();
        assertNotEquals(record1, record2);

        checkRangeKeys(record1);
        checkStringKeys(record1);
        checkDateKeys(record1);
    }

    private void checkDateKeys(Record expectedRecord) {
        Record newRecord = expectedRecord.copy();
        newRecord.setExpiresAt(new Date(expectedRecord.getExpiresAt().getTime() + 1000));
        assertNotEquals(expectedRecord, newRecord);
        newRecord.setExpiresAt(null);
        assertNotEquals(expectedRecord, newRecord);
    }

    private void checkRangeKeys(Record expectedRecord) {
        Record newRecord = expectedRecord.copy();
        newRecord.setRangeKey1(newRecord.getRangeKey1() + 1);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey2(newRecord.getRangeKey2() + 2);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey3(newRecord.getRangeKey3() + 3);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey4(newRecord.getRangeKey4() + 4);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey5(newRecord.getRangeKey5() + 5);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey6(newRecord.getRangeKey6() + 6);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey7(newRecord.getRangeKey7() + 7);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey8(newRecord.getRangeKey8() + 8);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey9(newRecord.getRangeKey9() + 9);
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setRangeKey10(newRecord.getRangeKey10() + 10);
        assertNotEquals(expectedRecord, newRecord);
    }

    private void checkStringKeys(Record expectedRecord) {
        Record newRecord = expectedRecord.copy();
        newRecord.setRecordKey(newRecord.getRecordKey() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy();
        newRecord.setParentKey(newRecord.getParentKey() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey1(newRecord.getKey1() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey2(newRecord.getKey2() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey3(newRecord.getKey3() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey4(newRecord.getKey4() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey5(newRecord.getKey5() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey6(newRecord.getKey6() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey7(newRecord.getKey7() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey8(newRecord.getKey8() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey9(newRecord.getKey9() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey10(newRecord.getKey10() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey11(newRecord.getKey11() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey12(newRecord.getKey12() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey13(newRecord.getKey13() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey14(newRecord.getKey14() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey15(newRecord.getKey15() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey16(newRecord.getKey16() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey17(newRecord.getKey17() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey18(newRecord.getKey18() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey19(newRecord.getKey19() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setKey20(newRecord.getKey20() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setServiceKey1(newRecord.getServiceKey1() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setServiceKey2(newRecord.getServiceKey2() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setServiceKey3(newRecord.getServiceKey3() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setServiceKey4(newRecord.getServiceKey4() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setServiceKey5(newRecord.getServiceKey5() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setProfileKey(newRecord.getProfileKey() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setBody(newRecord.getBody() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);

        newRecord = expectedRecord.copy()
                .setPrecommitBody(newRecord.getPrecommitBody() + UUID.randomUUID());
        assertNotEquals(expectedRecord, newRecord);
    }

    @Test
    void dateFieldTest() throws StorageClientException, StorageServerException, StorageCryptoException {
        String json = "  {\n" +
                "      'record_key': '976143aa1fd12b9ad7449fd9d3a6d25347d71b890b49d4fb5c738e798238865f',\n" +
                "      'body': '2:0Xxd0QYOXstTmrA1Erqm6F/jxt83IHFFHqJPf+QuMpwOObh+OaJ1hCjLLGi2GVnBXENQ5sIt92ayemBXr5JEY2CNUI9lp18gOim+aXveWH1FN8yk5HYqoSyOb5CkJHvp73+AaFmpzTJA3Zxy7z7rfZE2ByCwGtX454iY35jQcUGr1Zpo3m4BX2Y8Rc+RYvAO0J+1y6iDnaNk228d0QwDK4VRISslct+vp7T+O/fnOuyTZzoy/2IoUuvHpkhGsKB2sA+elqCMHz64HGlbGL1OWMmChmQ4R3Ax+/ddzd3xorUQdyz0S1L0YoByE/vCAgGMCkXkQ7kSnqFsRLyJPK4tZWen+G7pt4SdLHoD60vh8QrGtPXVQe4P9HeNCwZXOyhpZbTKvHRXIzsmzGud7Z6rU4DGSBEoeWXcVKIgQ7H0sBCHFZ6ixsw0fb/ciw66HGS/06tyjrWyMsq7HsaOkL01bzaRM9SMeZZskHDGsi4fOvt498SvKF2VT28PMWH8h4Wj24q7o18Ms7NrhnkqDql11FsKLb/O6hcKo5c9GzsSkYN+7KoPwHcj+eWs0Odu4BL2xq7VJiIjCw+25pqlXSpyKV0QTUSXI31VTNoqRRMpBlM06n4SC6SidQfRiiWXqptJEhLA9g==',\n" +
                "      'version': 0,\n" +
                "      'created_at': '2030-01-01T23:59:00+03:00',\n" +
                "      'updated_at': '2030-01-01T23:59:00+03:00',\n" +
                "      'is_encrypted': true\n" +
                "    }";

        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        TransferRecord transferRecord = gson.fromJson(json, TransferRecord.class);

        CryptoProvider cryptoProvider = new CryptoProvider(null);
        SecretsData secretsData = SecretsDataGenerator.fromPassword("password");
        DtoTransformer transformer = new DtoTransformer(cryptoProvider,
                new HashUtils("InCountry", false),
                true,
                () -> secretsData);
        Record record = transformer.getRecord(transferRecord);
        assertNotNull(record.getCreatedAt());
        assertNotNull(record.getUpdatedAt());
    }
}
