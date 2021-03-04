package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindFilterTest {

    @Test
    void testErrorArgs() {
        FindFilter findFilter = new FindFilter();
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> findFilter.limitAndOffset(0, 0));
        assertEquals("Limit must be more than 1", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> findFilter.limitAndOffset(Integer.MAX_VALUE, 0));
        assertEquals("Max limit is 100. Use offset to populate more", ex2.getMessage());
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> findFilter.limitAndOffset(1, -1));
        assertEquals("Offset must be more than 0", ex3.getMessage());
    }
}
