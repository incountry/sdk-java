package com.incountry.residence.sdk;

import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void testNullFilterToJson() {
        JsonObject json = JsonUtils.toJson((FindFilter) null, null);
        assertEquals("{}", json.toString());
    }

    @Test
    void testFilterConditionVersion() throws StorageClientException {
        FindFilter filter = new FindFilter();
        filter.setVersionFilter(new FilterStringParam(new String[]{"1"}, true));
        JsonObject json = JsonUtils.toJson(filter, new CryptoManager(null, "envId", null, false));
        assertEquals("{\"version\":{\"$not\":[1]}}", json.toString());

        filter = new FindFilter();
        filter.setVersionFilter(new FilterStringParam(new String[]{"1"}, false));
        json = JsonUtils.toJson(filter, new CryptoManager(null, "envId", null, false));
        assertEquals("{\"version\":[1]}", json.toString());
    }

    @Test
    void negativeTestIncorrectJson() throws StorageServerException {
        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getCountries("json", "https://app.start", "end.com"));
        assertEquals("Response error", ex.getMessage());
    }

    @Test
    void negativeTestEmptyStringJson() {
        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getCountries("", "https://app.start", "end.com"));
        assertEquals("Response error: country list is empty", ex.getMessage());
    }

    @Test
    void negativeTestNullStringJson() {
        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getCountries(null, "https://app.start", "end.com"));
        assertEquals("Response error: country list is empty", ex.getMessage());
    }

    @Test
    void testPassNullToJsonInt() {
        assertNull(JsonUtils.toJsonInt(null));
    }

    @Test
    void testPassNullToJsonArray() {
        assertNull(JsonUtils.toJsonArray(null, null));
    }

    @Test
    void testBatchRecordFromStringWithNullVersion() throws StorageException {
        String content = "{\"data\":[{\"key\":\"9ba998c3650c4150d54dd22f3f35f45081c8f5399da625677efde99550447966\",\"profile_key\":\"ee597d2e9e8ed19fd1b891af76495586da223cdbd6251fdac201531451b3329d\",\"body\":\"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsia2V5Ijoia2V5IiwicHJvZmlsZV9rZXkiOiJwcm9maWxlS2V5In19\"}],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        Record record = new Record("key", "body", "profileKey", null, null, null);
        BatchRecord batchRecord = JsonUtils.batchRecordFromString(content, new CryptoManager(null, "envId", null, false));
        assertEquals(record, batchRecord.getRecords().get(0));
    }
}
