package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.FindResult;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferBatchTest {

    @Test
    void negativeValidateTestWithMetaNull() {
        TransferBatch transferBatch = new TransferBatch();
        transferBatch.setMeta(null);
        StorageServerException ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: Meta is null", ex.getMessage());
    }

    @Test
    void negativeValidateTestWithMetaCountNegativeNumber() {
        TransferBatch transferBatch = new TransferBatch();

        FindResult findResult = new FindResult(null, -1, 2, 3, 4, new ArrayList<>());
        transferBatch.setMeta(findResult);
        StorageServerException ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());

        findResult = new FindResult(null, 1, -2, 3, 4, new ArrayList<>());
        transferBatch.setMeta(findResult);
        ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());

        findResult = new FindResult(null, 1, 2, -3, 4, new ArrayList<>());
        transferBatch.setMeta(findResult);
        ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());

        findResult = new FindResult(null, 1, 2, 3, -4, new ArrayList<>());
        transferBatch.setMeta(findResult);
        ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());
    }

    @Test
    void negativeValidateTestWithMetaCountMoreThanZeroAndDataNull() {
        TransferBatch transferBatch = new TransferBatch();
        FindResult findResult = new FindResult(null, 2, 2, 0, 2, new ArrayList<>());
        transferBatch.setMeta(findResult);
        StorageServerException ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());
    }

    @Test
    void negativeValidateTestWithMetaCountMoreThenMetaTotal() throws StorageException {
        int version = 0;
        SecretKey secretKey = new SecretKey("secret".getBytes(StandardCharsets.UTF_8), version, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, version);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        CryptoManager cryptoManager = new CryptoManager(secretKeyAccessor, "envId", null, false, true);

        TransferBatch transferBatch = new TransferBatch();
        FindResult findResult = new FindResult(null, 2, 2, 0, 1, new ArrayList<>());
        transferBatch.setMeta(findResult);
        List<TransferRecord> data = new ArrayList<>();
        Record record = new Record(null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        TransferRecord transferRecord1 = new TransferRecord(record, cryptoManager, "");

        data.add(transferRecord);
        data.add(transferRecord1);
        transferBatch.setData(data);
        StorageServerException ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: incorrect total in batch metadata, less then received", ex.getMessage());

        transferBatch.setMeta(new FindResult(null, 0, 2, 0, 0, new ArrayList<>()));
        ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());

        transferBatch.setData(Collections.emptyList());
        transferBatch.validate();

        transferBatch.setData(null);
        transferBatch.validate();

        transferBatch.setMeta(new FindResult(null, 3, 3, 0, 3, new ArrayList<>()));
        ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());

        transferBatch.setData(null);
        ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());

        transferBatch.setData(Collections.emptyList());
        ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());

        transferBatch.setData(data);
        ex = assertThrows(StorageServerException.class, transferBatch::validate);
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());
    }
}
