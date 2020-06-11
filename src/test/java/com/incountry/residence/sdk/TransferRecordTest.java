package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TransferRecordTest {

    private static final String SECRET = "secret";
    private static final String ENVIRONMENT_ID = "envId";

    private CryptoManager cryptoManager;
    private SecretKeyAccessor secretKeyAccessor;


    @BeforeEach
    public void initializeAccessorAndCrypto() throws StorageClientException {
        int version = 0;
        SecretKey secretKey = new SecretKey(SECRET, version, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, version);
        secretKeyAccessor = () -> secretsData;
        cryptoManager = new CryptoManager(secretKeyAccessor, ENVIRONMENT_ID, null, false);
    }

    @Test
    void testValidateNullKey() throws StorageException {
        Record record = new Record(null, null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferRecord.validate());
        assertEquals("Null required record fields: key", ex.getMessage());

        transferRecord.setKey("");
        StorageServerException ex1 = assertThrows(StorageServerException.class, () -> transferRecord.validate());
        assertEquals("Null required record fields: key", ex1.getMessage());
    }

    @Test
    void testValidateNullBody() throws StorageException {
        Record record = new Record("some_key", null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        transferRecord.setBody(null);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferRecord.validate());
        assertEquals("Null required record fields: body", ex.getMessage());
        transferRecord.setBody("");
        StorageServerException ex1 = assertThrows(StorageServerException.class, () -> transferRecord.validate());
        assertEquals("Null required record fields: body", ex1.getMessage());
    }

    @Test
    void testValidateNullKeyAndBody() throws StorageException {
        Record record = new Record(null, null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        transferRecord.setBody(null);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferRecord.validate());
        assertEquals("Null required record fields: key, body", ex.getMessage());
    }

    @Test
    void positiveTestEqualsWithSameObjects() throws StorageException {
        Record record = new Record(null, null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        assertEquals(transferRecord, transferRecord);
    }

    @Test
    void negativeTestEqualsDifferentClassObjects() throws StorageException {
        Record record = new Record(null, null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        assertNotEquals(transferRecord, (""));
    }

    @Test
    void negativeTestEqualsWithDifferentObjects() throws StorageException {
        Record record = new Record(null, null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        TransferRecord transferRecord1 = new TransferRecord(record, cryptoManager, "");
        assertNotEquals(transferRecord, transferRecord1);
    }

    @Test
    void positiveTestWithEqualTransferRecords() throws StorageException {
        Record record = new Record(null, null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        TransferRecord transferRecord1 = new TransferRecord(record, cryptoManager, "");
        transferRecord.setBody("");
        transferRecord1.setBody("");
        assertEquals(transferRecord, transferRecord1);
    }

    @Test
    void testHashCode() throws StorageException {
        Record record = new Record(null, null, null, null, null, null);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "");
        assertEquals(transferRecord.hashCode(), transferRecord.hashCode());
    }

    @Test
    void negativeTestDecrypt() throws StorageException {
        Record record = new Record("key", null, "profileKay", 1, "key2", "key3");
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transferRecord.decrypt(cryptoManager));
        assertEquals("Response error", ex.getMessage());
    }

    @Test
    void testDecryptWithCryptoManagerAndBodyNull() throws StorageException {
        String cryptData = "0ffcf2aa9f2e874e824a98d60621649dd5b594bdde303a20c150ff64fa60ccef";
        Record record = new Record("", null, "", 1, "", "");
        Record recordForComparison = new Record(cryptData, null, cryptData, 1, cryptData, cryptData);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
        transferRecord.setBody(null);
        assertEquals(recordForComparison, transferRecord.decrypt(null));
    }

    @Test
    void testDecryptWithCryptoManagerNull() throws StorageException {
        String cryptData = "0ffcf2aa9f2e874e824a98d60621649dd5b594bdde303a20c150ff64fa60ccef";
        Record record = new Record("", null, "", 1, "", "");
        Record recordForComparison = new Record(cryptData, "", cryptData, 1, cryptData, cryptData);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
        transferRecord.setBody("");
        assertEquals(recordForComparison, transferRecord.decrypt(null));
    }

    @Test
    void testDecryptWithBodyNull() throws StorageException {
        String cryptData = "0ffcf2aa9f2e874e824a98d60621649dd5b594bdde303a20c150ff64fa60ccef";
        Record record = new Record("", null, "", 1, "", "");
        Record recordForComparison = new Record(cryptData, null, cryptData, 1, cryptData, cryptData);
        TransferRecord transferRecord = new TransferRecord(record, cryptoManager, "{\"test\":}");
        transferRecord.setBody(null);
        assertEquals(recordForComparison, transferRecord.decrypt(cryptoManager));
    }

}
