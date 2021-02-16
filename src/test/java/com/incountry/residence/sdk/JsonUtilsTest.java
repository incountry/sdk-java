package com.incountry.residence.sdk;

import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.internal.FilterNumberParam;
import com.incountry.residence.sdk.dto.search.internal.FilterStringParam;
import com.incountry.residence.sdk.dto.search.internal.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.Test;

import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_GT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonUtilsTest {

    @Test
    void testBetweenFilter() throws StorageClientException {
        String expected = "{\"filter\":{\"range_key1\":{\"$gte\":2,\"$lte\":9}},\"options\":{\"limit\":100,\"offset\":0}}";
        String fact = JsonUtils.toJsonString(FindFilterBuilder.create().keyBetween(NumberField.RANGE_KEY1, 2, 9).build(), null);
        assertEquals(expected, fact);

        expected = "{\"filter\":{\"range_key1\":{\"$gte\":2,\"$lt\":9}},\"options\":{\"limit\":100,\"offset\":0}}";
        fact = JsonUtils.toJsonString(FindFilterBuilder.create().keyBetween(NumberField.RANGE_KEY1, 2, true, 9, false).build(), null);
        assertEquals(expected, fact);

        expected = "{\"filter\":{\"range_key1\":{\"$gt\":2,\"$lte\":9}},\"options\":{\"limit\":100,\"offset\":0}}";
        fact = JsonUtils.toJsonString(FindFilterBuilder.create().keyBetween(NumberField.RANGE_KEY1, 2, false, 9, true).build(), null);
        assertEquals(expected, fact);

        expected = "{\"filter\":{\"range_key1\":{\"$gt\":2,\"$lt\":9}},\"options\":{\"limit\":100,\"offset\":0}}";
        fact = JsonUtils.toJsonString(FindFilterBuilder.create().keyBetween(NumberField.RANGE_KEY1, 2, false, 9, false).build(), null);
        assertEquals(expected, fact);
    }

    @Test
    void testWithOneFilter() throws StorageClientException {
        String expected = "{\"filter\":{\"range_key1\":{\"$gt\":1}},\"options\":{\"limit\":100,\"offset\":0}}";
        FindFilter findFilter = new FindFilter();
        findFilter.setFilter(NumberField.RANGE_KEY1, new FilterNumberParam(OPER_GT, 1L));
        String fact = JsonUtils.toJsonString(findFilter, null);
        assertEquals(expected, fact);
    }

    @Test
    void testNullFilterToJson() {
        String jsonString = JsonUtils.toJsonString((FindFilter) null, null);
        assertEquals("{\"filter\":{},\"options\":{\"limit\":100,\"offset\":0}}", jsonString);
    }

    @Test
    void testFilterConditionVersion() throws StorageClientException {
        FindFilter filter = new FindFilter();
        filter.setFilter(StringField.VERSION, new FilterStringParam(new String[]{"1"}, true));
        String jsonString = JsonUtils.toJsonString(filter, new CryptoManager(null, "envId", null, false, true));
        assertEquals("{\"filter\":{\"version\":{\"$not\":[1]}},\"options\":{\"limit\":100,\"offset\":0}}", jsonString);

        filter = new FindFilter();
        filter.setFilter(StringField.VERSION, new FilterStringParam(new String[]{"1"}, false));
        jsonString = JsonUtils.toJsonString(filter, new CryptoManager(null, "envId", null, false, true));
        assertEquals("{\"filter\":{\"version\":[1]},\"options\":{\"limit\":100,\"offset\":0}}", jsonString);

        FindFilterBuilder builder = FindFilterBuilder.create()
                .searchKeysLike("search_keys");
        jsonString = JsonUtils.toJsonString(builder.build(), new CryptoManager(null, "envId", null, false, true));
        assertEquals("{\"filter\":{\"search_keys\":\"search_keys\"},\"options\":{\"limit\":100,\"offset\":0}}", jsonString);
    }

    @Test
    void negativeTestIncorrectJson() {
        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getMidiPops("json", "https://app.start", "end.com"));
        assertEquals("Response parse error", ex.getMessage());
    }

    @Test
    void negativeTestEmptyStringJson() {
        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getMidiPops("", "https://app.start", "end.com"));
        assertEquals("Response error: country list is empty", ex.getMessage());
    }

    @Test
    void negativeTestNullStringJson() {
        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getMidiPops(null, "https://app.start", "end.com"));
        assertEquals("Response error: country list is empty", ex.getMessage());
    }

    @Test
    void testPassNullToJsonInt() {
        assertNull(JsonUtils.toJsonInt(null));
    }

    @Test
    void testPassNullToJsonArray() {
        assertNull(JsonUtils.toJsonArray(null, null, null));
    }

    @Test
    void testBatchRecordFromStringWithNullVersion() throws StorageException {
        String content = "{\"data\":[{\"version\":0,\"is_encrypted\":false,\"record_key\":\"cd59def71c1fc1c42bce810ee3e629c345f749cd988d28ab4639311de36ca867\",\"profile_key\":\"ee597d2e9e8ed19fd1b891af76495586da223cdbd6251fdac201531451b3329d\",\"body\":\"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsicmVjb3JkX2tleSI6InJlY29yZEtleSIsInByb2ZpbGVfa2V5IjoicHJvZmlsZUtleSJ9fQ==\"}],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        Record record = new Record("recordKey", "body").setProfileKey("profileKey");
        FindResult findResult = JsonUtils.batchRecordFromString(content, new CryptoManager(null, "envId", null, false, true));
        assertEquals(record, findResult.getRecords().get(0));

        String content1 = "{\"data\":[{\"is_encrypted\":false,\"record_key\":\"cd59def71c1fc1c42bce810ee3e629c345f749cd988d28ab4639311de36ca867\",\"profile_key\":\"ee597d2e9e8ed19fd1b891af76495586da223cdbd6251fdac201531451b3329d\",\"body\":\"pt:eyJwYXlsb2FkIjoiYm9keSIsIm1ldGEiOnsicmVjb3JkX2tleSI6InJlY29yZEtleSIsInByb2ZpbGVfa2V5IjoicHJvZmlsZUtleSJ9fQ==\"}],\"meta\":{\"count\":1,\"limit\":10,\"offset\":0,\"total\":1}}";
        Record record1 = new Record("recordKey", "body").setProfileKey("profileKey");
        FindResult findResult1 = JsonUtils.batchRecordFromString(content1, new CryptoManager(null, "envId", null, false, true));
        assertEquals(record1, findResult1.getRecords().get(0));
    }

    @Test
    void testCreateUpdatedMetaJson() {
        String fileNameProperty = "filename";
        String mimeTypeProperty = "mime_type";
        String fileName = "file.txt";
        String mimeType = "application/json";

        String json = JsonUtils.createUpdatedMetaJson(fileName, mimeType);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(fileNameProperty, fileName);
        jsonObject.addProperty(mimeTypeProperty, mimeType);
        assertEquals(json, jsonObject.toString());

        json = JsonUtils.createUpdatedMetaJson(fileName, null);
        jsonObject = new JsonObject();
        jsonObject.addProperty(fileNameProperty, fileName);
        assertEquals(json, jsonObject.toString());

        json = JsonUtils.createUpdatedMetaJson(fileName, "");
        jsonObject = new JsonObject();
        jsonObject.addProperty(fileNameProperty, fileName);
        assertEquals(json, jsonObject.toString());

        json = JsonUtils.createUpdatedMetaJson(null, mimeType);
        jsonObject = new JsonObject();
        jsonObject.addProperty(mimeTypeProperty, mimeType);
        assertEquals(json, jsonObject.toString());

        json = JsonUtils.createUpdatedMetaJson("", mimeType);
        jsonObject = new JsonObject();
        jsonObject.addProperty(mimeTypeProperty, mimeType);
        assertEquals(json, jsonObject.toString());
    }
}
