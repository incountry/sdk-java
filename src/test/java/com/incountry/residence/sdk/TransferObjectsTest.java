package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.transfer.TransferFilterContainer;
import com.incountry.residence.sdk.tools.transfer.TransferPopList;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferObjectsTest {

    @Test
    void negativePopListCheckPositive() {
        StorageServerException ex = assertThrows(StorageServerException.class, () -> TransferPopList.validatePopList(null));
        assertEquals("Response error: country list is empty", ex.getMessage());
        ex = assertThrows(StorageServerException.class, () -> TransferPopList.validatePopList(new TransferPopList()));
        assertEquals("Response error: country list is empty", ex.getMessage());
    }

    @SuppressWarnings("java:S5785")
    @Test
    void transferRecordEqualsPositive() {
        TransferRecord record = new TransferRecord("recordKey");
        assertEquals(record, new TransferRecord("recordKey"));
        assertNotEquals(record, new TransferRecord("recordKey1"));
        record.setKey("key");
        record.setEncrypted(true);
        assertEquals(record, record);
        assertFalse(record.equals(UUID.randomUUID()));
        assertFalse(record.equals(null));
        assertEquals(record.hashCode(), record.hashCode());
        TransferRecord record2 = new TransferRecord("recordKey");
        assertNotEquals(record, record2);
        record2.setEncrypted(true);
        assertNotEquals(record, record2);
        record2.setKey("key");
        assertEquals(record, record2);
    }

    @Test
    void transferFilterContainerPositive() {
        TransferFilterContainer container = new TransferFilterContainer(null, 1, 2, null);
        assertNotNull(container);
        container = new TransferFilterContainer(null, 1, 2, new ArrayList<>());
        assertNotNull(container);
    }
}
