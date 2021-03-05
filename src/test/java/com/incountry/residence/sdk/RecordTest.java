package com.incountry.residence.sdk;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordTest {
    @Expose
    @SerializedName("record_key")
    public String recordKey;
    @SerializedName("parent_key")
    public String parentKey;
    @Expose
    public String body;
    @Expose
    @SerializedName("profile_key")
    public String profileKey;
    @Expose
    @SerializedName("range_key1")
    public long rangeKey1;
    public long rangeKey2;
    public long rangeKey3;
    public long rangeKey4;
    public long rangeKey5;
    public long rangeKey6;
    public long rangeKey7;
    public long rangeKey8;
    public long rangeKey9;
    public long rangeKey10;
    public String key1;
    @Expose
    public String key2;
    @Expose
    public String key3;
    public String key4;
    public String key5;
    public String key6;
    public String key7;
    public String key8;
    public String key9;
    public String key10;
    public String key11;
    public String key12;
    public String key13;
    public String key14;
    public String key15;
    public String key16;
    public String key17;
    public String key18;
    public String key19;
    public String key20;
    public String errorCorrectionKey1;
    public String errorCorrectionKey2;
    public String precommit;

    @BeforeEach
    public void init() {
        body = "body";
        recordKey = "recordKey1";
        parentKey = "parentKey";
        key1 = "key1";
        key2 = "key2";
        key3 = "key3";
        key4 = "key4";
        key5 = "key5";
        key6 = "key6";
        key7 = "key7";
        key8 = "key8";
        key9 = "key9";
        key10 = "key10";
        key11 = "key11";
        key12 = "key12";
        key13 = "key13";
        key14 = "key14";
        key15 = "key15";
        key16 = "key16";
        key17 = "key17";
        key18 = "key18";
        key19 = "key19";
        key20 = "key20";
        profileKey = "profileKey";
        rangeKey1 = 1;
        rangeKey2 = 2;
        rangeKey3 = 3;
        rangeKey4 = 4;
        rangeKey5 = 5;
        rangeKey6 = 6;
        rangeKey7 = 7;
        rangeKey8 = 8;
        rangeKey9 = 9;
        rangeKey10 = 10;
        precommit = "precommit";
        errorCorrectionKey1 = "errorCorrectionKey1";
        errorCorrectionKey2 = "errorCorrectionKey2";
    }


    @Test
    void findResultToStringPositive() {
        Record record1 = new Record(recordKey + 1, body + 1)
                .setProfileKey(profileKey + 1)
                .setRangeKey1(rangeKey1 + 1)
                .setKey2(key2 + 1)
                .setKey3(key3 + 1);
        Record record2 = new Record(recordKey + 2, body + 2)
                .setProfileKey(profileKey + 2)
                .setRangeKey1(rangeKey1 + 2)
                .setKey2(key2 + 2)
                .setKey3(key3 + 2);
        FindResult findResult = new FindResult(Arrays.asList(record1, record2), 2, 2, 0, 2, new ArrayList<>());
        String str = findResult.toString();
        assertTrue(str.contains(String.valueOf(record1.hashCode())));
        assertTrue(str.contains(String.valueOf(record2.hashCode())));
    }

    @SuppressWarnings("java:S3415")
    @Test
    void equalsPositive() {
        Record record1 = new Record(recordKey)
                .setParentKey(parentKey)
                .setKey1(key1)
                .setKey2(key2)
                .setKey3(key3)
                .setKey4(key4)
                .setKey5(key5)
                .setKey6(key6)
                .setKey7(key7)
                .setKey8(key8)
                .setKey9(key9)
                .setKey10(key10)
                .setKey11(key11)
                .setKey12(key12)
                .setKey13(key13)
                .setKey14(key14)
                .setKey15(key15)
                .setKey16(key16)
                .setKey17(key17)
                .setKey18(key18)
                .setKey19(key19)
                .setKey20(key20)
                .setProfileKey(profileKey)
                .setRangeKey1(rangeKey1)
                .setRangeKey2(rangeKey2)
                .setRangeKey3(rangeKey3)
                .setRangeKey4(rangeKey4)
                .setRangeKey5(rangeKey5)
                .setRangeKey6(rangeKey6)
                .setRangeKey7(rangeKey7)
                .setRangeKey8(rangeKey8)
                .setRangeKey9(rangeKey9)
                .setRangeKey10(rangeKey10)
                .setBody(body)
                .setServiceKey1(errorCorrectionKey1)
                .setServiceKey2(errorCorrectionKey2)
                .setPrecommitBody(precommit);

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

        Gson gson= new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        TransferRecord transferRecord=gson.fromJson(json,TransferRecord.class);

        CryptoProvider cryptoProvider = new CryptoProvider(null);
        SecretsData secretsData = SecretsDataGenerator.fromPassword("password");
        DtoTransformer transformer = new DtoTransformer(cryptoProvider,
                new HashUtils("InCountry", false),
                true,
                () -> secretsData);
        Record record=transformer.getRecord(transferRecord);
        assertNotNull(record.getCreatedAt());
        assertNotNull(record.getUpdatedAt());
    }
}
