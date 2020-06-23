package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.transfer.TransferPopList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferPopListTest {

    @Test
    void negativePopListCheckTest() {
        StorageServerException ex = assertThrows(StorageServerException.class, () -> TransferPopList.validatePopList(null));
        assertEquals("Response error: country list is empty", ex.getMessage());
        ex = assertThrows(StorageServerException.class, () -> TransferPopList.validatePopList(new TransferPopList()));
        assertEquals("Response error: country list is empty", ex.getMessage());
    }
}
