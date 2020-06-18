package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonUtilsTest {

    @Test
    void testBetweenFilter() throws StorageClientException {
        String expected = "{\"filter\":{\"range_key\":{\"$gte\":2,\"$lte\":9}},\"options\":{\"limit\":100,\"offset\":0}}";
        String fact = JsonUtils.toJsonString(FindFilterBuilder.create().rangeKeyBetween(2, 9).build(), null);
        assertEquals(expected, fact);

        expected = "{\"filter\":{\"range_key\":{\"$gte\":2,\"$lt\":9}},\"options\":{\"limit\":100,\"offset\":0}}";
        fact = JsonUtils.toJsonString(FindFilterBuilder.create().rangeKeyBetween(2, true, 9, false).build(), null);
        assertEquals(expected, fact);

        expected = "{\"filter\":{\"range_key\":{\"$gt\":2,\"$lte\":9}},\"options\":{\"limit\":100,\"offset\":0}}";
        fact = JsonUtils.toJsonString(FindFilterBuilder.create().rangeKeyBetween(2, false, 9, true).build(), null);
        assertEquals(expected, fact);

        expected = "{\"filter\":{\"range_key\":{\"$gt\":2,\"$lt\":9}},\"options\":{\"limit\":100,\"offset\":0}}";
        fact = JsonUtils.toJsonString(FindFilterBuilder.create().rangeKeyBetween(2, false, 9, false).build(), null);
        assertEquals(expected, fact);
    }
}
