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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AllSdkEncryptionTest {

    private static final Logger LOG = LogManager.getLogger(AllSdkEncryptionTest.class);
    private static final String ENV_ID = "InCountry";
    private static final String PASSWORD = "password";
    private static final String RESPONSE = "{\n" +
            "    \"record_key\": \"976143aa1fd12b9ad7449fd9d3a6d25347d71b890b49d4fb5c738e798238865f\",\n" +
            "    \"body\": \"2:IGJNCmV+RXZydaPxDjjhZ80/6aZ2vcEUZ2GuOzKgVSSdM6gYf5RPgFbyLqv+7ihz0CpYFQQWf9xkIyD/u3VYky8dWLq+NXcE2xYL4/U7LqUZmJPQzgcQCABYQ/8vOvUEcrfOAwzGjR6etTp1ki+79JmCEZFSNqcDP1GZXNLFdLoSUp1X2wVlH9ukhJ4jrE0cKDrpJllswRSOz0BhS8PA/73KNKwo718t7fPWpUm7RkyILwYTd/LpPvpXMS6JypEns8fviOpbCLQPpZNBe6zpwbFf3C0qElHlbCyPbDyUiMzVKOwWlYFpozFcRyWegjJ42T8v52+GuRY5\",\n" +
            "    \"key2\": \"abcb2ad9e9e0b1787f262b014f517ad1136f868e7a015b1d5aa545b2f575640d\",\n" +
            "    \"key3\": \"1102ae53e55f0ce1d802cc8bb66397e7ea749fd8d05bd2d4d0f697cedaf138e3\",\n" +
            "    \"profile_key\": \"f5b5ae4914972ace070fa51b410789324abe063dbe2bb09801410d9ab54bf833\",\n" +
            "    \"range_key1\": 6275438399,\n" +
            "    \"version\": 0\n" +
            "}";

    private static final String RESPONSE_SCHEMA_2_2 = "  {\n" +
            "      'record_key': '976143aa1fd12b9ad7449fd9d3a6d25347d71b890b49d4fb5c738e798238865f',\n" +
            "      'profile_key': 'f5b5ae4914972ace070fa51b410789324abe063dbe2bb09801410d9ab54bf833',\n" +
            "      'range_key1': 100500,\n" +
            "      'range_key2': 10050,\n" +
            "      'range_key3': 1005,\n" +
            "      'range_key4': 100,\n" +
            "      'range_key5': 10,\n" +
            "      'range_key6': 1,\n" +
            "      'range_key7': 10,\n" +
            "      'range_key8': 100,\n" +
            "      'range_key9': 1005,\n" +
            "      'range_key10': 10050,\n" +
            "      'service_key1': 'b2d95d1ccfeb1a17c99b74685f7fd4c33647b97cb0559c267a4afcd6f649f3a8',\n" +
            "      'service_key2': '9bbc39b2617cbd9fc0290f93c7bbd1772f1a2a45f48ae8dc1a9544d75159c7a2',\n" +
            "      'key1': 'daf5914655dc36b7f6f31a97a05205106fdbd725e264235e9e8b31c66489e7ed',\n" +
            "      'key2': 'abcb2ad9e9e0b1787f262b014f517ad1136f868e7a015b1d5aa545b2f575640d',\n" +
            "      'key3': '1102ae53e55f0ce1d802cc8bb66397e7ea749fd8d05bd2d4d0f697cedaf138e3',\n" +
            "      'key4': '08a46eb74e0621208a41cf982b9da83e564a1d448997c5c912477ff79ec4c0e3',\n" +
            "      'key5': 'cb86e1358566c9f6c1a52106b32a085b5f02aa8330d3f538ddf55cd599a320f7',\n" +
            "      'key6': '5048f7bae5308ca05006ef63025d4243beddbf431f7eff43ac927e471656d1ed',\n" +
            "      'key7': 'aa9e0b00099734cafeda1b13393422a381596dc3fd189ee598791fa95f46bce4',\n" +
            "      'key8': '54933d4eb2e2d2c1e7ab9344e23a233ee9c537876929d5e265d45ae789b03f6c',\n" +
            "      'key9': 'c0e91efa56683cf7f1f0f99b2791e4719e7f70018c6e3938ebaff5735d3c275f',\n" +
            "      'key10': '9f54258b7136a70f61891f162243e11930d5cedb3ca89682bab9f28fbedda9b6',\n" +
            "      'precommit_body': '2:iqFsqhqby5rX5YAsFnboXoMwSBX7b8JSybs6INJTSMNBSZIulv44hyYw2XlENtOWTCV1Sn1uzM4H4ekTy3vXhTyzbndWBdSWNXcT8mLUDZcByyGJhKunvuvr9B1Bk5GghNzuEvriVsV08LEg',\n" +
            "      'body': '2:0Xxd0QYOXstTmrA1Erqm6F/jxt83IHFFHqJPf+QuMpwOObh+OaJ1hCjLLGi2GVnBXENQ5sIt92ayemBXr5JEY2CNUI9lp18gOim+aXveWH1FN8yk5HYqoSyOb5CkJHvp73+AaFmpzTJA3Zxy7z7rfZE2ByCwGtX454iY35jQcUGr1Zpo3m4BX2Y8Rc+RYvAO0J+1y6iDnaNk228d0QwDK4VRISslct+vp7T+O/fnOuyTZzoy/2IoUuvHpkhGsKB2sA+elqCMHz64HGlbGL1OWMmChmQ4R3Ax+/ddzd3xorUQdyz0S1L0YoByE/vCAgGMCkXkQ7kSnqFsRLyJPK4tZWen+G7pt4SdLHoD60vh8QrGtPXVQe4P9HeNCwZXOyhpZbTKvHRXIzsmzGud7Z6rU4DGSBEoeWXcVKIgQ7H0sBCHFZ6ixsw0fb/ciw66HGS/06tyjrWyMsq7HsaOkL01bzaRM9SMeZZskHDGsi4fOvt498SvKF2VT28PMWH8h4Wj24q7o18Ms7NrhnkqDql11FsKLb/O6hcKo5c9GzsSkYN+7KoPwHcj+eWs0Odu4BL2xq7VJiIjCw+25pqlXSpyKV0QTUSXI31VTNoqRRMpBlM06n4SC6SidQfRiiWXqptJEhLA9g==',\n" +
            "      'version': 0,\n" +
            "      'is_encrypted': true\n" +
            "    }";

    private static final String RESPONSE_SCHEMA_2_3 = "  {\n" +
            "      'record_key': '976143aa1fd12b9ad7449fd9d3a6d25347d71b890b49d4fb5c738e798238865f',\n" +
            "      'parent_key': '48734579d0358c2ec2f9dae81cf963f9848d0f3eebe0dd49fa5c5177a76d6e83',\n" +
            "      'profile_key': 'f5b5ae4914972ace070fa51b410789324abe063dbe2bb09801410d9ab54bf833',\n" +
            "      'range_key1': 100500,\n" +
            "      'range_key2': 10050,\n" +
            "      'range_key3': 1005,\n" +
            "      'range_key4': 100,\n" +
            "      'range_key5': 10,\n" +
            "      'range_key6': 1,\n" +
            "      'range_key7': 10,\n" +
            "      'range_key8': 100,\n" +
            "      'range_key9': 1005,\n" +
            "      'range_key10': 10050,\n" +
            "      'service_key1': 'b2d95d1ccfeb1a17c99b74685f7fd4c33647b97cb0559c267a4afcd6f649f3a8',\n" +
            "      'service_key2': '9bbc39b2617cbd9fc0290f93c7bbd1772f1a2a45f48ae8dc1a9544d75159c7a2',\n" +
            "      'key1': 'daf5914655dc36b7f6f31a97a05205106fdbd725e264235e9e8b31c66489e7ed',\n" +
            "      'key2': 'abcb2ad9e9e0b1787f262b014f517ad1136f868e7a015b1d5aa545b2f575640d',\n" +
            "      'key3': '1102ae53e55f0ce1d802cc8bb66397e7ea749fd8d05bd2d4d0f697cedaf138e3',\n" +
            "      'key4': '08a46eb74e0621208a41cf982b9da83e564a1d448997c5c912477ff79ec4c0e3',\n" +
            "      'key5': 'cb86e1358566c9f6c1a52106b32a085b5f02aa8330d3f538ddf55cd599a320f7',\n" +
            "      'key6': '5048f7bae5308ca05006ef63025d4243beddbf431f7eff43ac927e471656d1ed',\n" +
            "      'key7': 'aa9e0b00099734cafeda1b13393422a381596dc3fd189ee598791fa95f46bce4',\n" +
            "      'key8': '54933d4eb2e2d2c1e7ab9344e23a233ee9c537876929d5e265d45ae789b03f6c',\n" +
            "      'key9': 'c0e91efa56683cf7f1f0f99b2791e4719e7f70018c6e3938ebaff5735d3c275f',\n" +
            "      'key10': '9f54258b7136a70f61891f162243e11930d5cedb3ca89682bab9f28fbedda9b6',\n" +
            "      'key11': '7ff26763def85a1a89c0fdb19233e29fb56596678f62f6e41910b169f093ca67',\n" +
            "      'key12': '89976091da7295197e2308b75e44bc041dbbf168c24a8e966ad36fb7abfce2d5',\n" +
            "      'key13': '0f29faa621589f9697cea59702b2a004e0218f76f31481dec52db49399eda2c7',\n" +
            "      'key14': '1ac2ea997a372fbd0e151c4fc7afc8a691357199f084476f5d5be72ab34f95af',\n" +
            "      'key15': 'a24189e052ff879096e49b9c49a6ef89fbde0d9a7bedece0888d562ae7e30ba7',\n" +
            "      'key16': 'cdf7f7f7e636731cf84d4fced224a7fea12789affc80c3625d34c0b0225a95f1',\n" +
            "      'key17': 'e00d696f96834844980da61d99cc2f9e9986a4708e098232f38c3a6882c7a2bd',\n" +
            "      'key18': '84d49a44022203031f83598591301bddcb3086c300414cbec552ad22ba01abeb',\n" +
            "      'key19': '3eaec397977e9a6edbfb88de2463a05036d279157f15bc6f41dc1d8ea5ef91fb',\n" +
            "      'key20': '301597b144c6d48f8a3b20190487647461610e5e438a88fbc9c201c80a3dc039',\n" +
            "      'precommit_body': '2:iqFsqhqby5rX5YAsFnboXoMwSBX7b8JSybs6INJTSMNBSZIulv44hyYw2XlENtOWTCV1Sn1uzM4H4ekTy3vXhTyzbndWBdSWNXcT8mLUDZcByyGJhKunvuvr9B1Bk5GghNzuEvriVsV08LEg',\n" +
            "      'body': '2:pyZNurXdMOdaQAidLdcVV6sgnS9Ii6G2/4RuajJ61RCZoe3sQFz26NFEnWkzIgYcyCtrZWXLWvYRAFSHMW6Nx/z2+hXMttC9rTrx7J9mm8aOtfvd9w6Ca/T1o1VAmP+4ez42w5/gVPnrzV3LGmjI/5aHcZ2jTkmSEJC6UZAXP+Q+IkA+xZQS/yBF7ptZL8P3PVPH/PYJ7GOpJqJTUhi4NVZ6FOm6ELDMOf5t3113CFtjmiRt5JhdZiApETELe7OpwyuT1VxsoZSCgqJPy8ee/QWyREIF4+en3MKPg5a48jzYFANd4YDjRqiulj2/reFl6tN1lqXujHbkMKjA0aF6uzGFrx06igshtPyhqruA3IWbW2+0g+X7jhcNGYfGIV0BqdvyshH050e9szy8n02qoV6v6N30PvRXFcQDrRy3Vtj0ogYmL0uT8nH843Tt0zX0OyK/oNW9pmlp/990/vDYULhO8iZhm8PqYAeOKP6D1ql9B3syEg7FiCTU4b4FrofJC9cSG/kQ5XgVuLnsYI22BBSkr9mtmG7tFPudtpGEOHP5fUkAFKYw0BCMlERPRIWoSj5YG0KJ0mU+IfavL5KNdlIO5c/H6VM84u3+oVEWYoPjR2vxAuEyT0h61VPwsAnxrQEaMg8SeJijMGVgyuX0eF2xG2n2fh3ohmgDvedtYGutKwhlUnYtkzCgHBFXOTRTUr/+iteFuoRrGJZ/0cfWf915RI1M5v3vCo060MoL7xHXu8eoNMo2GkX5bVUyKioFxgEK5VbMB3ZPEp5/mQb4UPXBysDjEoTJX3EcZyJ9D3OMlsGQ+lDOtuEucy4SVqgA49fXA4ko4HRveerd3TTc09QJ63wXB8Zngass0MnzU40xZ0I+VuaK/5txBPs+zgHZEKGq+W2RGASxJ+IA9roatahtKzcduQ7qvb8UGIb4I9O4DYPQ63zdFGWYSHBVAKICo4+05LkNLNoSumH2NKUptZPShpVJ6BzOoXCF+4S36WM711+0qGhiIO9Ar9C7KgY0',\n" +
            "      'version': 0,\n" +
            "      'is_encrypted': true\n" +
            "    }";

    private static CryptoManager cryptoManager;
    private static final Record ORIGINAL_RECORD;
    private static final Record ORIGINAL_RECORD_SCHEMA_2_2;
    private static final Record ORIGINAL_RECORD_SCHEMA_2_3;

    static {
        try {
            cryptoManager = new CryptoManager(() -> SecretsDataGenerator.fromPassword(PASSWORD), ENV_ID, null, false, true);
        } catch (StorageClientException ex) {
            LOG.error(ex.getMessage());
        }
        ORIGINAL_RECORD = new Record("InCountryKey")
                .setBody("{\"data\": \"InCountryBody\"}")
                .setProfileKey("InCountryPK")
                .setRangeKey1(6275438399L)
                .setKey2("InCountryKey2")
                .setKey3("InCountryKey3");

        ORIGINAL_RECORD_SCHEMA_2_2 = createRecord();
        ORIGINAL_RECORD_SCHEMA_2_3 = createRecord()
                .setParentKey("InCountryParentKey")
                .setKey11("InCountryKey11")
                .setKey12("InCountryKey12")
                .setKey13("InCountryKey13")
                .setKey14("InCountryKey14")
                .setKey15("InCountryKey15")
                .setKey16("InCountryKey16")
                .setKey17("InCountryKey17")
                .setKey18("InCountryKey18")
                .setKey19("InCountryKey19")
                .setKey20("InCountryKey20");
    }

    private static Record createRecord() {
        return new Record("InCountryKey")
                .setBody("{\"data\": \"InCountryBody\"}")
                .setPrecommitBody("{\"test\": \"test\"}")
                .setKey1("InCountryKey1")
                .setKey2("InCountryKey2")
                .setKey3("InCountryKey3")
                .setKey4("InCountryKey4")
                .setKey5("InCountryKey5")
                .setKey6("InCountryKey6")
                .setKey7("InCountryKey7")
                .setKey8("InCountryKey8")
                .setKey9("InCountryKey9")
                .setKey10("InCountryKey10")
                .setProfileKey("InCountryPK")
                .setServiceKey1("service1")
                .setServiceKey2("service2")
                .setRangeKey1(100500L)
                .setRangeKey2(10050L)
                .setRangeKey3(1005L)
                .setRangeKey4(100L)
                .setRangeKey5(10L)
                .setRangeKey6(1L)
                .setRangeKey7(10L)
                .setRangeKey8(100L)
                .setRangeKey9(1005L)
                .setRangeKey10(10050L);
    }

    private static Stream<Arguments> getResponseAndRecord() {
        return Stream.of(
                Arguments.of(ORIGINAL_RECORD, RESPONSE),
                Arguments.of(ORIGINAL_RECORD_SCHEMA_2_2, RESPONSE_SCHEMA_2_2),
                Arguments.of(ORIGINAL_RECORD_SCHEMA_2_3, RESPONSE_SCHEMA_2_3));
    }

    @ParameterizedTest
    @MethodSource("getResponseAndRecord")
    void testDecryptionFromOtherSDK(Record record, String jsonResponse) throws StorageServerException, StorageClientException, StorageCryptoException {
        Record decodedRecord = JsonUtils.recordFromString(jsonResponse, cryptoManager);
        assertEquals(record, decodedRecord);
    }

    @ParameterizedTest
    @MethodSource("getResponseAndRecord")
    void testEncryptionFromOtherSDK(Record record, String jsonResponse) throws StorageClientException, StorageCryptoException {
        String recordJsonString = JsonUtils.toJsonString(record, cryptoManager);
        JsonObject jsonObject = getGson().fromJson(recordJsonString, JsonObject.class);
        JsonObject originalJsonObject = getGson().fromJson(jsonResponse, JsonObject.class);

        originalJsonObject.entrySet()
                .stream()
                .filter(entry -> !(entry.getKey().equals("body")
                        || entry.getKey().equals("precommit_body")
                        || entry.getKey().equals("is_encrypted")
                ))
                .forEach(entry -> assertEquals(entry.getValue(), jsonObject.get(entry.getKey())));

        jsonObject.entrySet()
                .stream()
                .filter(entry -> !(entry.getKey().equals("body")
                        || entry.getKey().equals("precommit_body")
                        || entry.getKey().equals("is_encrypted")
                ))
                .forEach(entry -> assertEquals(entry.getValue(), originalJsonObject.get(entry.getKey())));

        assertNotEquals(originalJsonObject.get("body"), jsonObject.get("body"));
        if (originalJsonObject.get("precommit_body") != null) {
            assertNotEquals(originalJsonObject.get("precommit_body"), jsonObject.get("precommit_body"));
        }
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .disableHtmlEscaping()
                .create();
    }
}
