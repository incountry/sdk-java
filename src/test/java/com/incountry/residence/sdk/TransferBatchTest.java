package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.transfer.TransferBatch;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferBatchTest {

    @Test
    void negativeValidateTestWithMetaNull() {
        TransferBatch transferBatch = new TransferBatch();
        transferBatch.setMeta(null);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferBatch.validate());
        assertEquals("Response error: Meta is null", ex.getMessage());
    }

    @Test
    void negativeValidateTestWithMetaCountNegativeNumber() {
        TransferBatch transferBatch = new TransferBatch();
        BatchRecord batchRecord = new BatchRecord(null, -2, 2, 0, 2, new ArrayList<>());
        transferBatch.setMeta(batchRecord);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferBatch.validate());
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());
    }

    @Test
    void negativeValidateTestWithMetaCountMoreThanZeroAndDataNull() {
        TransferBatch transferBatch = new TransferBatch();
        BatchRecord batchRecord = new BatchRecord(null, 2, 2, 0, 2, new ArrayList<>());
        transferBatch.setMeta(batchRecord);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferBatch.validate());
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());
    }

    @Test
    void negativeValidateTestWithMetaCountMoreThenMetaTotal() throws StorageException {
        int version = 0;
        SecretKey secretKey = new SecretKey("secret", version, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, version);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        CryptoManager cryptoManager = new CryptoManager(secretKeyAccessor, "envId", null, false);

        TransferBatch transferBatch = new TransferBatch();
        BatchRecord batchRecord = new BatchRecord(null, 2, 2, 0, 1, new ArrayList<>());
        transferBatch.setMeta(batchRecord);
        List<TransferRecord> data = new ArrayList<>();
        Record record = new Record(null, null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        TransferRecord transferRecord1 = new TransferRecord(record, cryptoManager, "");

        data.add(transferRecord);
        data.add(transferRecord1);
        transferBatch.setData(data);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferBatch.validate());
        assertEquals("Response error: incorrect total in batch metadata, less then received", ex.getMessage());
    }

}
