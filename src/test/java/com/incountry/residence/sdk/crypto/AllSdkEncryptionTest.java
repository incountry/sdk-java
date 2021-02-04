//package com.incountry.residence.sdk.crypto;
//
//import com.google.gson.FieldNamingPolicy;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonObject;
//import com.incountry.residence.sdk.dto.Record;
//import com.incountry.residence.sdk.tools.JsonUtils;
//import com.incountry.residence.sdk.tools.crypto.Deprecated.CryptoManager;
//import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
//import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
//import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
//import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//
//class AllSdkEncryptionTest {
//
//    private static final Logger LOG = LogManager.getLogger(AllSdkEncryptionTest.class);
//    private static final String ENV_ID = "InCountry";
//    private static final String PASSWORD = "password";
//    private static final String RESPONSE = "{\n" +
//            "    \"record_key\": \"976143aa1fd12b9ad7449fd9d3a6d25347d71b890b49d4fb5c738e798238865f\",\n" +
//            "    \"body\": \"2:IGJNCmV+RXZydaPxDjjhZ80/6aZ2vcEUZ2GuOzKgVSSdM6gYf5RPgFbyLqv+7ihz0CpYFQQWf9xkIyD/u3VYky8dWLq+NXcE2xYL4/U7LqUZmJPQzgcQCABYQ/8vOvUEcrfOAwzGjR6etTp1ki+79JmCEZFSNqcDP1GZXNLFdLoSUp1X2wVlH9ukhJ4jrE0cKDrpJllswRSOz0BhS8PA/73KNKwo718t7fPWpUm7RkyILwYTd/LpPvpXMS6JypEns8fviOpbCLQPpZNBe6zpwbFf3C0qElHlbCyPbDyUiMzVKOwWlYFpozFcRyWegjJ42T8v52+GuRY5\",\n" +
//            "    \"key2\": \"abcb2ad9e9e0b1787f262b014f517ad1136f868e7a015b1d5aa545b2f575640d\",\n" +
//            "    \"key3\": \"1102ae53e55f0ce1d802cc8bb66397e7ea749fd8d05bd2d4d0f697cedaf138e3\",\n" +
//            "    \"profile_key\": \"f5b5ae4914972ace070fa51b410789324abe063dbe2bb09801410d9ab54bf833\",\n" +
//            "    \"range_key1\": 6275438399,\n" +
//            "    \"version\": 0,\n" +
//            "    \"attachments\": []\n" +
//            "}";
//
//    private static final String RESPONSE_NEW_FIELDS = "  {\n" +
//            "      'record_key': '976143aa1fd12b9ad7449fd9d3a6d25347d71b890b49d4fb5c738e798238865f',\n" +
//            "      'profile_key': 'f5b5ae4914972ace070fa51b410789324abe063dbe2bb09801410d9ab54bf833',\n" +
//            "      'range_key1': 100500,\n" +
//            "      'range_key2': 10050,\n" +
//            "      'range_key3': 1005,\n" +
//            "      'range_key4': 100,\n" +
//            "      'range_key5': 10,\n" +
//            "      'range_key6': 1,\n" +
//            "      'range_key7': 10,\n" +
//            "      'range_key8': 100,\n" +
//            "      'range_key9': 1005,\n" +
//            "      'range_key10': 10050,\n" +
//            "      'service_key1': 'b2d95d1ccfeb1a17c99b74685f7fd4c33647b97cb0559c267a4afcd6f649f3a8',\n" +
//            "      'service_key2': '9bbc39b2617cbd9fc0290f93c7bbd1772f1a2a45f48ae8dc1a9544d75159c7a2',\n" +
//            "      'key1': 'daf5914655dc36b7f6f31a97a05205106fdbd725e264235e9e8b31c66489e7ed',\n" +
//            "      'key2': 'abcb2ad9e9e0b1787f262b014f517ad1136f868e7a015b1d5aa545b2f575640d',\n" +
//            "      'key3': '1102ae53e55f0ce1d802cc8bb66397e7ea749fd8d05bd2d4d0f697cedaf138e3',\n" +
//            "      'key4': '08a46eb74e0621208a41cf982b9da83e564a1d448997c5c912477ff79ec4c0e3',\n" +
//            "      'key5': 'cb86e1358566c9f6c1a52106b32a085b5f02aa8330d3f538ddf55cd599a320f7',\n" +
//            "      'key6': '5048f7bae5308ca05006ef63025d4243beddbf431f7eff43ac927e471656d1ed',\n" +
//            "      'key7': 'aa9e0b00099734cafeda1b13393422a381596dc3fd189ee598791fa95f46bce4',\n" +
//            "      'key8': '54933d4eb2e2d2c1e7ab9344e23a233ee9c537876929d5e265d45ae789b03f6c',\n" +
//            "      'key9': 'c0e91efa56683cf7f1f0f99b2791e4719e7f70018c6e3938ebaff5735d3c275f',\n" +
//            "      'key10': '9f54258b7136a70f61891f162243e11930d5cedb3ca89682bab9f28fbedda9b6',\n" +
//            "      'precommit_body': '2:iqFsqhqby5rX5YAsFnboXoMwSBX7b8JSybs6INJTSMNBSZIulv44hyYw2XlENtOWTCV1Sn1uzM4H4ekTy3vXhTyzbndWBdSWNXcT8mLUDZcByyGJhKunvuvr9B1Bk5GghNzuEvriVsV08LEg',\n" +
//            "      'body': '2:0Xxd0QYOXstTmrA1Erqm6F/jxt83IHFFHqJPf+QuMpwOObh+OaJ1hCjLLGi2GVnBXENQ5sIt92ayemBXr5JEY2CNUI9lp18gOim+aXveWH1FN8yk5HYqoSyOb5CkJHvp73+AaFmpzTJA3Zxy7z7rfZE2ByCwGtX454iY35jQcUGr1Zpo3m4BX2Y8Rc+RYvAO0J+1y6iDnaNk228d0QwDK4VRISslct+vp7T+O/fnOuyTZzoy/2IoUuvHpkhGsKB2sA+elqCMHz64HGlbGL1OWMmChmQ4R3Ax+/ddzd3xorUQdyz0S1L0YoByE/vCAgGMCkXkQ7kSnqFsRLyJPK4tZWen+G7pt4SdLHoD60vh8QrGtPXVQe4P9HeNCwZXOyhpZbTKvHRXIzsmzGud7Z6rU4DGSBEoeWXcVKIgQ7H0sBCHFZ6ixsw0fb/ciw66HGS/06tyjrWyMsq7HsaOkL01bzaRM9SMeZZskHDGsi4fOvt498SvKF2VT28PMWH8h4Wj24q7o18Ms7NrhnkqDql11FsKLb/O6hcKo5c9GzsSkYN+7KoPwHcj+eWs0Odu4BL2xq7VJiIjCw+25pqlXSpyKV0QTUSXI31VTNoqRRMpBlM06n4SC6SidQfRiiWXqptJEhLA9g==',\n" +
//            "      'version': 0,\n" +
//            "      'is_encrypted': true,\n" +
//            "      'attachments': []\n" +
//            "    }";
//
//    private static CryptoManager cryptoManager;
//    private static final Record ORIGINAL_RECORD;
//    private static final Record ORIGINAL_RECORD_NEW_FIELDS;
//
//    static {
//        try {
//            cryptoManager = new CryptoManager(() -> SecretsDataGenerator.fromPassword(PASSWORD), ENV_ID, null, false, true);
//        } catch (StorageClientException ex) {
//            LOG.error(ex.getMessage());
//        }
//        ORIGINAL_RECORD = new Record("InCountryKey")
//                .setBody("{\"data\": \"InCountryBody\"}")
//                .setProfileKey("InCountryPK")
//                .setRangeKey1(6275438399L)
//                .setKey2("InCountryKey2")
//                .setKey3("InCountryKey3");
//
//        ORIGINAL_RECORD_NEW_FIELDS = new Record("InCountryKey")
//                .setBody("{\"data\": \"InCountryBody\"}")
//                .setPrecommitBody("{\"test\": \"test\"}")
//                .setKey1("InCountryKey1")
//                .setKey2("InCountryKey2")
//                .setKey3("InCountryKey3")
//                .setKey4("InCountryKey4")
//                .setKey5("InCountryKey5")
//                .setKey6("InCountryKey6")
//                .setKey7("InCountryKey7")
//                .setKey8("InCountryKey8")
//                .setKey9("InCountryKey9")
//                .setKey10("InCountryKey10")
//                .setProfileKey("InCountryPK")
//                .setServiceKey1("service1")
//                .setServiceKey2("service2")
//                .setRangeKey1(100500L)
//                .setRangeKey2(10050L)
//                .setRangeKey3(1005L)
//                .setRangeKey4(100L)
//                .setRangeKey5(10L)
//                .setRangeKey6(1L)
//                .setRangeKey7(10L)
//                .setRangeKey8(100L)
//                .setRangeKey9(1005L)
//                .setRangeKey10(10050L);
//    }
//
//    private static Stream<Arguments> getResponseAndRecord() {
//        return Stream.of(
//                Arguments.of(ORIGINAL_RECORD, RESPONSE),
//                Arguments.of(ORIGINAL_RECORD_NEW_FIELDS, RESPONSE_NEW_FIELDS));
//    }
//
//    @ParameterizedTest
//    @MethodSource("getResponseAndRecord")
//    void testDecryptionFromOtherSDK(Record record, String jsonResponse) throws StorageServerException, StorageClientException, StorageCryptoException {
//        Record decodedRecord = JsonUtils.recordFromString(jsonResponse, cryptoManager);
//        assertEquals(record, decodedRecord);
//    }
//
//    @ParameterizedTest
//    @MethodSource("getResponseAndRecord")
//    void testEncryptionFromOtherSDK(Record record, String jsonResponse) throws StorageClientException, StorageCryptoException {
//        String recordJsonString = JsonUtils.toJsonString(record, cryptoManager);
//        JsonObject jsonObject = getGson().fromJson(recordJsonString, JsonObject.class);
//        JsonObject originalJsonObject = getGson().fromJson(jsonResponse, JsonObject.class);
//        originalJsonObject.remove("attachments");
//
//        originalJsonObject.entrySet()
//                .stream()
//                .filter(entry -> !(entry.getKey().equals("body")
//                        || entry.getKey().equals("precommit_body")
//                        || entry.getKey().equals("is_encrypted")
//                ))
//                .forEach(entry -> assertEquals(entry.getValue(), jsonObject.get(entry.getKey())));
//
//        jsonObject.entrySet()
//                .stream()
//                .filter(entry -> !(entry.getKey().equals("body")
//                        || entry.getKey().equals("precommit_body")
//                        || entry.getKey().equals("is_encrypted")
//                ))
//                .forEach(entry -> assertEquals(entry.getValue(), originalJsonObject.get(entry.getKey())));
//
//        assertNotEquals(originalJsonObject.get("body"), jsonObject.get("body"));
//        if (originalJsonObject.get("precommit_body") != null) {
//            assertNotEquals(originalJsonObject.get("precommit_body"), jsonObject.get("precommit_body"));
//        }
//    }
//
//    private static Gson getGson() {
//        return new GsonBuilder()
//                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//                .disableHtmlEscaping()
//                .create();
//    }
//}
