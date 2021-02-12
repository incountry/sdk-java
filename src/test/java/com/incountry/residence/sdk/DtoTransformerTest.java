package com.incountry.residence.sdk;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.fields.NumberField;
import com.incountry.residence.sdk.dto.search.fields.StringField;
import com.incountry.residence.sdk.dto.search.filters.FindFilter;
import com.incountry.residence.sdk.search.FindFilterTest;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.transfer.DtoTransformer;
import com.incountry.residence.sdk.tools.transfer.TransferFilterContainer;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoTransformerTest {

    private static final String ENV_ID = "envId";

    private static Gson getGson4Records() {
        return new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Test
    void getTransferRecordTest() throws StorageException {
        String recordKey = "someRecordKey";
        String body = "someBody";
        Record record = new Record();
        record.setRecordKey(recordKey);
        record.setBody(body);
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider);
        TransferRecord transferRecord = transformer.getTransferRecord(record);
        assertNotNull(transferRecord);
        assertEquals("6717f3b00ef569e5604566335ff0b10a58a236a07a066372ede034ed5b732690",
                transferRecord.getRecordKey());
        assertNotNull(transferRecord.getBody());
        Record restoredRecord = transformer.getRecord(transferRecord);

        assertNotNull(restoredRecord);
        assertEquals(recordKey, restoredRecord.getRecordKey());
        assertEquals(body, restoredRecord.getBody());
    }

    @Test
    void getTransferRecordWithNullBodyTest() throws StorageException {
        String recordKey = "someRecordKey";
        Record record = new Record();
        record.setRecordKey(recordKey);
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider);
        TransferRecord transferRecord = transformer.getTransferRecord(record);
        assertNotNull(transferRecord);
        assertEquals("6717f3b00ef569e5604566335ff0b10a58a236a07a066372ede034ed5b732690",
                transferRecord.getRecordKey());
        assertNotNull(transferRecord.getBody());
        Record restoredRecord = transformer.getRecord(transferRecord);
        assertNotNull(restoredRecord);
        assertEquals(recordKey, restoredRecord.getRecordKey());
        assertNull(restoredRecord.getBody());
    }

    @Test
    void getTransferRecordFromNullTest() throws StorageClientException {
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> transformer.getTransferRecord(null));
        assertEquals("Record is null", ex.getMessage());
    }

    @Test
    void getRecordFromNullTest() throws StorageException {
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider);
        Record record = transformer.getRecord(null);
        assertNull(record);
    }

    @Test
    void getTransferRecordWithNullRequiredFieldsTest() throws StorageClientException {
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider);
        TransferRecord transferRecord = new TransferRecord();
        transferRecord.setRecordKey(null);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> transformer.getRecord(transferRecord));
        assertEquals("Null required record fields: recordKey", ex.getMessage());
        transferRecord.setRecordKey("recordKey");
        ex = assertThrows(StorageClientException.class, () -> transformer.getRecord(transferRecord));
        assertEquals("Null required record fields: body", ex.getMessage());
    }

    @Test
    void transformFindFilterTest() throws StorageClientException {
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider);

        FindFilter filter = FindFilter.create()
                .keyEq(StringField.KEY1, "value1")
                .keyEq(NumberField.VERSION, 0l);
        TransferFilterContainer transformFilter = transformer.transformFilter(filter);
        assertTrue(FindFilterTest.jsonEquals(
                "{\"filter\":{\"key1\":[\"327c84457a2ff2c6da36314dc0ffb3216a283570a5c4654d5f51947e74742cf0\"]," +
                        "\"version\":[0]},\"options\":{\"limit\":100,\"offset\":0}}",
                getGson4Records().toJson(transformFilter)));
    }

    @Test
    void getFindResultNegativeTest() throws StorageClientException {
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider);

        StorageServerException ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(null));
        assertEquals("Response error: Find response is null", ex.getMessage());

        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(new TransferFindResult(null, null)));
        assertEquals("Response error: Find result metadata is null", ex.getMessage());

        TransferFindResult.FindMeta findMeta1 = new TransferFindResult(null, null)
                .new FindMeta(1, 1, 1, -1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(new TransferFindResult(null, findMeta1)));
        assertEquals("Response error: Negative values in find result metadata", ex.getMessage());

        TransferFindResult.FindMeta findMeta2 = new TransferFindResult(null, null)
                .new FindMeta(1, 1, -1, 1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(new TransferFindResult(null, findMeta2)));
        assertEquals("Response error: Negative values in find result metadata", ex.getMessage());

        TransferFindResult.FindMeta findMeta3 = new TransferFindResult(null, null)
                .new FindMeta(1, -1, 1, 1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(new TransferFindResult(null, findMeta3)));
        assertEquals("Response error: Negative values in find result metadata", ex.getMessage());

        TransferFindResult.FindMeta findMeta4 = new TransferFindResult(null, null)
                .new FindMeta(-1, 1, 1, 1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(new TransferFindResult(null, findMeta4)));
        assertEquals("Response error: Negative values in find result metadata", ex.getMessage());

        TransferFindResult.FindMeta findMeta5 = new TransferFindResult(null, null)
                .new FindMeta(1, 1, 1, 1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(new TransferFindResult(null, findMeta5)));
        assertEquals("Response error: count in find results metadata differs from data size", ex.getMessage());

        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(new TransferFindResult(new ArrayList<>(), findMeta5)));
        assertEquals("Response error: count in find results metadata differs from data size", ex.getMessage());

        TransferFindResult.FindMeta findMeta6 = new TransferFindResult(null, null)
                .new FindMeta(1, 1, 1, 2);
        List<TransferRecord> transferRecordList = new ArrayList<>();
        TransferRecord transferRecord1 = new TransferRecord();
        transferRecord1.setRecordKey("1");
        TransferRecord transferRecord2 = new TransferRecord();
        transferRecord2.setRecordKey("2");
        transferRecordList.add(transferRecord1);
        transferRecordList.add(transferRecord2);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(new TransferFindResult(transferRecordList, findMeta6)));
        assertEquals("Response error: incorrect total in find results metadata, less then received", ex.getMessage());
    }

    @Test
    void getSecretsDataNegativeTest() throws StorageClientException {
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        String errorMessage = "testError";

        DtoTransformer transformer1 = new DtoTransformer(cryptoManager, hashProvider, true, () -> {
            throw new StorageClientException(errorMessage);
        });
        TransferRecord transferRecord = new TransferRecord();
        transferRecord.setRecordKey("recordKey");
        transferRecord.setBody("recordBody");

        StorageClientException ex = assertThrows(StorageClientException.class, () -> transformer1.getRecord(transferRecord));
        assertEquals(errorMessage, ex.getMessage());

        DtoTransformer transformer2 = new DtoTransformer(cryptoManager, hashProvider, true,
                () -> null);
        ex = assertThrows(StorageClientException.class, () -> transformer2.getRecord(transferRecord));
        assertEquals("Secret accessor returns null secret", ex.getMessage());
    }

    @Test
    void getRecordWithNonHashingModeTest() throws StorageException {
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider, false);

        Record record = new Record();
        record.setRecordKey("hashed_RecordKey");
        record.setKey1("non_hashed_key1");

        TransferRecord transformRecord = transformer.getTransferRecord(record);
        assertEquals("f09b57bd0932fd2ec1f4ba63f1cbc77a2f02efac25c31a9a0d1ed3a078330524",
                transformRecord.getRecordKey());
        assertEquals("non_hashed_key1", transformRecord.getKey1());
    }

    @Test
    void getFilterWithNonHashingModeTest() throws StorageClientException {
        CryptoProvider cryptoManager = new CryptoProvider();
        HashUtils hashProvider = new HashUtils(ENV_ID, false);
        DtoTransformer transformer = new DtoTransformer(cryptoManager, hashProvider, false);

        FindFilter filter = FindFilter.create()
                .keyEq(StringField.RECORD_KEY, "some record key")
                .keyEq(StringField.KEY1, "some value of key1");
        TransferFilterContainer filterContainer = transformer.transformFilter(filter);

        assertTrue(FindFilterTest.jsonEquals(
                "{\"filter\":{\"record_key\":[\"2ab632ee5ebf3af90be1ae6accea46d99843fdc4676bb29a376919fa9c530364\"]," +
                        "\"key1\":[\"some value of key1\"]}," +
                        "\"options\":{\"limit\":100,\"offset\":0}}",
                getGson4Records().toJson(filterContainer)));
    }

}
