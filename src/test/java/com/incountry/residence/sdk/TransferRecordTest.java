//todo
//package com.incountry.residence.sdk;
//
//import com.google.gson.FieldNamingPolicy;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonSyntaxException;
//import com.incountry.residence.sdk.dto.Record;
//import com.incountry.residence.sdk.tools.JsonUtils;
//import com.incountry.residence.sdk.tools.crypto.CryptoManager;
//import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
//import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
//import com.incountry.residence.sdk.tools.exceptions.StorageException;
//import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
//import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
//import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
//import com.incountry.residence.sdk.crypto.SecretsData;
//import com.incountry.residence.sdk.tools.transfer.TransferRecord;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.nio.charset.StandardCharsets;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class TransferRecordTest {
//
//    private static final byte[] SECRET = "secret".getBytes(StandardCharsets.UTF_8);
//    private static final String ENVIRONMENT_ID = "envId";
//
//    private CryptoManager cryptoManager;
//    private SecretKeyAccessor secretKeyAccessor;
//    private Gson gson;
//
//    @BeforeEach
//    public void initializeAccessorAndCrypto() throws StorageClientException {
//        int version = 0;
//        SecretKey secretKey = new SecretKey(SECRET, version, false);
//        List<SecretKey> secretKeyList = new ArrayList<>();
//        secretKeyList.add(secretKey);
//        SecretsData secretsData = new SecretsData(secretKeyList, version);
//        secretKeyAccessor = () -> secretsData;
//        cryptoManager = new CryptoManager(secretKeyAccessor, ENVIRONMENT_ID, null, false, true);
//
//        gson = new GsonBuilder()
//                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//                .disableHtmlEscaping()
//                .create();
//    }
//
//    @Test
//    void testValidateNullKey() throws StorageException {
//        Record record = new Record(null);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
//        StorageServerException ex = assertThrows(StorageServerException.class, () -> TransferRecord.validate(transferRecord));
//        assertEquals("Null required record fields: recordKey", ex.getMessage());
//
//        transferRecord.setRecordKey("");
//        StorageServerException ex1 = assertThrows(StorageServerException.class, () -> TransferRecord.validate(transferRecord));
//        assertEquals("Null required record fields: recordKey", ex1.getMessage());
//    }
//
//    @Test
//    void testValidateNullBody() throws StorageException {
//        Record record = new Record("some_key", null);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
//        transferRecord.setBody(null);
//        StorageServerException ex = assertThrows(StorageServerException.class, () -> TransferRecord.validate(transferRecord));
//        assertEquals("Null required record fields: body", ex.getMessage());
//        transferRecord.setBody("");
//        StorageServerException ex1 = assertThrows(StorageServerException.class, () -> TransferRecord.validate(transferRecord));
//        assertEquals("Null required record fields: body", ex1.getMessage());
//    }
//
//    @Test
//    void testValidateNullKeyAndBody() throws StorageException {
//        Record record = new Record(null);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
//        transferRecord.setBody(null);
//        StorageServerException ex = assertThrows(StorageServerException.class, () -> TransferRecord.validate(transferRecord));
//        assertEquals("Null required record fields: recordKey, body", ex.getMessage());
//    }
//
//    @Test
//    void positiveTestEqualsWithSameObjects() throws StorageException {
//        Record record = new Record(null);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
//        assertEquals(transferRecord, transferRecord);
//    }
//
//    @SuppressWarnings("java:S3415")
//    @Test
//    void negativeTestEqualsDifferentClassObjects() throws StorageException {
//        Record record = new Record(null);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
//        assertNotEquals(transferRecord, null);
//        assertNotEquals(transferRecord, UUID.randomUUID());
//        assertNotEquals(null, transferRecord);
//    }
//
//    @Test
//    void negativeTestEqualsWithDifferentObjects() throws StorageException {
//        Record record = new Record(null);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
//        TransferRecord transferRecord1 = new TransferRecord(record, cryptoManager, "");
//        assertNotEquals(transferRecord, transferRecord1);
//    }
//
//    @Test
//    void positiveTestWithEqualTransferRecords() throws StorageException {
//        Record record = new Record(null);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
//        TransferRecord transferRecord1 = new TransferRecord(record, cryptoManager, "");
//        transferRecord.setBody("");
//        transferRecord1.setBody("");
//        assertEquals(transferRecord, transferRecord1);
//    }
//
//    @Test
//    void testHashCode() throws StorageException {
//        Record record = new Record(null);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
//        assertEquals(transferRecord.hashCode(), transferRecord.hashCode());
//    }
//
//    @Test
//    void negativeTestDecrypt() throws StorageException {
//        Record record = new Record("recordKey")
//                .setProfileKey("profileKey")
//                .setRangeKey1(1L)
//                .setKey2("key2")
//                .setKey3("key3");
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
//        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferRecord.decrypt(cryptoManager, gson));
//        assertEquals("Response error", ex.getMessage());
//    }
//
//    @Test
//    void testDecryptWithCryptoManagerAndBodyNull() throws StorageException {
//        String cryptData = "0ffcf2aa9f2e874e824a98d60621649dd5b594bdde303a20c150ff64fa60ccef";
//        Record record = new Record("")
//                .setProfileKey("")
//                .setRangeKey1(1L)
//                .setKey2("")
//                .setKey3("");
//        Record recordForComparison = new Record(cryptData)
//                .setProfileKey(cryptData)
//                .setRangeKey1(1L)
//                .setKey2(cryptData)
//                .setKey3(cryptData);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
//        transferRecord.setBody(null);
//        assertEquals(recordForComparison, transferRecord.decrypt(null, gson));
//    }
//
//    @Test
//    void testDecryptWithCryptoManagerNull() throws StorageException {
//        String cryptData = "0ffcf2aa9f2e874e824a98d60621649dd5b594bdde303a20c150ff64fa60ccef";
//        Record record = new Record("")
//                .setProfileKey("")
//                .setRangeKey1(1L)
//                .setKey2("")
//                .setKey3("");
//        Record recordForComparison = new Record(cryptData)
//                .setBody("")
//                .setProfileKey(cryptData)
//                .setRangeKey1(1L)
//                .setKey2(cryptData)
//                .setKey3(cryptData);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
//        transferRecord.setBody("");
//        assertEquals(recordForComparison, transferRecord.decrypt(null, gson));
//    }
//
//    @Test
//    void testDecryptWithBodyNull() throws StorageException {
//        String cryptData = "0ffcf2aa9f2e874e824a98d60621649dd5b594bdde303a20c150ff64fa60ccef";
//        Record record = new Record("")
//                .setProfileKey("")
//                .setRangeKey1(1L)
//                .setKey2("")
//                .setKey3("");
//        Record recordForComparison = new Record(cryptData)
//                .setProfileKey(cryptData)
//                .setRangeKey1(1L)
//                .setKey2(cryptData)
//                .setKey3(cryptData);
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
//        transferRecord.setBody(null);
//        assertEquals(recordForComparison, transferRecord.decrypt(cryptoManager, gson));
//    }
//
//    @Test
//    void testIsEncrypted() throws StorageClientException, StorageCryptoException {
//        Record record = new Record("someKey", "someBody");
//        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
//        assertTrue(transferRecord.isEncrypted());
//
//        CryptoManager cryptoManagerWithoutEnc = new CryptoManager(null, ENVIRONMENT_ID, null, false, true);
//        transferRecord = new TransferRecord(record, cryptoManagerWithoutEnc, "{\"test\":}");
//        assertFalse(transferRecord.isEncrypted());
//
//        transferRecord.setEncrypted(true);
//        assertTrue(transferRecord.isEncrypted());
//
//        transferRecord.setEncrypted(false);
//        assertFalse(transferRecord.isEncrypted());
//    }
//
//    /**
//     *
//     */
//    @Test
//    void testBackwardCompatibility() throws StorageServerException, StorageClientException, StorageCryptoException {
//        //There is no 'record_key' in encrypted body, but there is deprecated 'key'
//        String legacyRecordJson = "{\"version\":0," +
//                "\"record_key\":\"a301d702fd5942dfdee1cb0c255bee864113ca8c3298c74e19ac3ae067972029\"," +
//                "\"body\":\"2:r0J6eOsdx1RMJfMHBod/xkOQ6+xmpjPSWO4o61nAGG6Ud8S4RL8ug04Jw+7aD98lUw4vSRM7tj62tPIZmuULFOiYz+odDBNjVg5nmylbI/FHuytflyAMRImDoFmtlZTqSsUG0fP5RVdrLyURp3M6BHzcDLFBDiXEOzVkRqx18k2GbsFgJEfv\"}";
//        Record record = JsonUtils.recordFromString(legacyRecordJson, cryptoManager);
//        assertNotNull(record);
//        assertEquals("<key>", record.getRecordKey());
//        assertEquals("<body>", record.getBody());
//        assertNull(record.getKey1());
//
//        //There are no 'record_key' and 'key' in encrypted body
//        legacyRecordJson = "{\"version\":0," +
//                "\"record_key\":\"a301d702fd5942dfdee1cb0c255bee864113ca8c3298c74e19ac3ae067972029\"," +
//                "\"body\":\"2:1BgO5xgjDXZu759eZZZzvRZJmPVDhir2O6Od7bX36r0s3goMKEGk9O6pKs8X4jZJcP+Evndn7GFl4MD2XNRXFKuGOnLieMunU0+gG0sei5iIMw0PWXZVE1IdqT52EulE6a1BD0BLDA==\"}";
//        record = JsonUtils.recordFromString(legacyRecordJson, cryptoManager);
//        assertNotNull(record);
//        assertNull(record.getRecordKey());
//        assertNull(record.getKey1());
//    }
//
//    @Test
//    void createAndUpdateDateTest() throws StorageClientException, StorageCryptoException, StorageServerException, ParseException {
//        String responseJson = "{\n" +
//                "  \"version\": 0,\n" +
//                "  \"created_at\": \"2020-01-01T09:30:45Z\",\n" +
//                "  \"updated_at\": \"2020-02-28T13:50:30+0000\",\n" +
//                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
//                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
//                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
//                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
//                "}";
//        CryptoManager cryptoManager = new CryptoManager(null, "envId", null, false, true);
//        Record record1 = JsonUtils.recordFromString(responseJson, cryptoManager);
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");
//        assertEquals(dateFormat.parse("2020-01-01 09:30:45 +0000"), record1.getCreatedAt());
//        assertEquals(dateFormat.parse("2020-02-28 13:50:30 +0000"), record1.getUpdatedAt());
//
//        Record record2 = JsonUtils.recordFromString(responseJson, cryptoManager);
//        assertEquals(record1, record2);
//
//        String responseJsonWithoutCreatedAt = "{\n" +
//                "  \"version\": 0,\n" +
//                "  \"updated_at\": \"2020-02-28T13:50:30+0000\",\n" +
//                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
//                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
//                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
//                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
//                "}";
//        Record record3 = JsonUtils.recordFromString(responseJsonWithoutCreatedAt, cryptoManager);
//        assertNotEquals(record1, record3);
//
//        String responseJsonWithoutUpdatedAt = "{\n" +
//                "  \"version\": 0,\n" +
//                "  \"created_at\": \"2020-01-01T09:30:45Z\",\n" +
//                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
//                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
//                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
//                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
//                "}";
//        Record record4 = JsonUtils.recordFromString(responseJsonWithoutCreatedAt, cryptoManager);
//        assertNotEquals(record1, record4);
//
//        String responseJsonAnotherCreatedAt = "{\n" +
//                "  \"version\": 0,\n" +
//                "  \"created_at\": \"2019-01-01T09:30:45Z\",\n" +
//                "  \"updated_at\": \"2020-02-28T13:50:30+0000\",\n" +
//                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
//                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
//                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
//                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
//                "}";
//        Record record5 = JsonUtils.recordFromString(responseJsonAnotherCreatedAt, cryptoManager);
//        assertNotEquals(record1, record5);
//
//        String responseJsonAnotherUpdatedAt = "{\n" +
//                "  \"version\": 0,\n" +
//                "  \"created_at\": \"2020-01-01T09:30:45Z\",\n" +
//                "  \"updated_at\": \"2020-03-28T13:50:30+0000\",\n" +
//                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
//                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
//                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
//                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
//                "}";
//        Record record6 = JsonUtils.recordFromString(responseJsonAnotherUpdatedAt, cryptoManager);
//        assertNotEquals(record1, record6);
//    }
//
//    @Test
//    void createAndUpdateDateNegativeTest() throws StorageClientException {
//        String responseJson1 = "{\n" +
//                "  \"version\": 0,\n" +
//                "  \"created_at\": \"SOME_ILLEGAL_DATE\",\n" +
//                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
//                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
//                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
//                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
//                "}";
//        CryptoManager cryptoManager = new CryptoManager(null, "envId", null, false, true);
//        StorageServerException exception = assertThrows(StorageServerException.class, () -> JsonUtils.recordFromString(responseJson1, cryptoManager));
//        assertEquals("Response parse error", exception.getMessage());
//        assertEquals(JsonSyntaxException.class, exception.getCause().getClass());
//        assertEquals(ParseException.class, exception.getCause().getCause().getClass());
//        assertEquals("Failed to parse date [\"SOME_ILLEGAL_DATE\"]: Invalid number: SOME", exception.getCause().getCause().getMessage());
//
//        String responseJson2 = "{\n" +
//                "  \"version\": 0,\n" +
//                "  \"created_at\": \"123321\",\n" +
//                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
//                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
//                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
//                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
//                "}";
//
//        exception = assertThrows(StorageServerException.class, () -> JsonUtils.recordFromString(responseJson2, cryptoManager));
//        assertEquals("Response parse error", exception.getMessage());
//        assertEquals(JsonSyntaxException.class, exception.getCause().getClass());
//        assertEquals(ParseException.class, exception.getCause().getCause().getClass());
//        assertEquals("Failed to parse date [\"123321\"]: 123321", exception.getCause().getCause().getMessage());
//
//        String responseJson3 = "{\n" +
//                "  \"version\": 0,\n" +
//                "  \"created_at\": \"\",\n" +
//                "  \"record_key\": \"f80969b9ad88774bcfca0512ed523b97bdc1fb87ba1c0d6297bdaf84d2666e68\",\n" +
//                "  \"key2\": \"409e11fd44de5fdb33bdfcc0e6584b8b64bb9b27f325d5d7ec3ce3d521f5aca8\",\n" +
//                "  \"key3\": \"eecb9d4b64b2bb6ada38bbfb2100e9267cf6ec944880ad6045f4516adf9c56d6\",\n" +
//                "  \"body\": \"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5MSIsImtleTIiOiJrZXkyIiwia2V5MyI6ImtleTMifX0=\"\n" +
//                "}";
//
//        exception = assertThrows(StorageServerException.class, () -> JsonUtils.recordFromString(responseJson3, cryptoManager));
//        assertEquals("Response parse error", exception.getMessage());
//        assertEquals(JsonSyntaxException.class, exception.getCause().getClass());
//        assertEquals(ParseException.class, exception.getCause().getCause().getClass());
//        assertEquals("Failed to parse date [\"\"]: (java.lang.NumberFormatException)", exception.getCause().getCause().getMessage());
//    }
//}
