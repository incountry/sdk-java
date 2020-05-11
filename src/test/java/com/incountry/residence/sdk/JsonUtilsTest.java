package com.incountry.residence.sdk;

import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonUtilsTest {

    @Test
    public void testBetweenFilter() throws StorageClientException {
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

    @Test
    public void testNullFilterToJson() {
        JsonObject json = JsonUtils.toJson((FindFilter) null, null);
        assertEquals("{}", json.toString());
    }

    @Test
    public void testFilterConditionVersion() throws StorageClientException {
        FindFilter filter = new FindFilter();
        filter.setVersionFilter(new FilterStringParam(new String[]{"1"}, true));
        JsonObject json = JsonUtils.toJson(filter, new CryptoManager(null, "envId", null, false));
        assertEquals("{\"version\":{\"$not\":[1]}}", json.toString());

        filter = new FindFilter();
        filter.setVersionFilter(new FilterStringParam(new String[]{"1"}, false));
        json = JsonUtils.toJson(filter, new CryptoManager(null, "envId", null, false));
        assertEquals("{\"version\":[1]}", json.toString());
    }
}
