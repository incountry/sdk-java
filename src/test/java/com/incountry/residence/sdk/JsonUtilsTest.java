package com.incountry.residence.sdk;

import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testGetCountriesFromJson() throws StorageServerException {
        String json = "{\n" +
                "   \"countries\":[\n" +
                "      {\n" +
                "         \"name\":\"US\",\n" +
                "         \"id\":134,\n" +
                "         \"status\":\"Ok\",\n" +
                "         \"direct\":true\n" +
                "      }\n" +
                "   ]\n" +
                "}";
        Map<String, POP> countries = JsonUtils.getCountries(json, "https://app.start", "end.com");
        assertEquals("https://app.start134end.com", countries.get("134").getHost());
        assertEquals("US", countries.get("134").getName());
    }

    @Test
    void testGetCountriesFromJsonWithDirectFalse() throws StorageServerException {
        String json = "{\n" +
                "   \"countries\":[\n" +
                "      {\n" +
                "         \"name\":\"US\",\n" +
                "         \"id\":134,\n" +
                "         \"status\":\"Ok\",\n" +
                "         \"direct\":false\n" +
                "      }\n" +
                "   ]\n" +
                "}";
        Map<String, POP> countries = JsonUtils.getCountries(json, "https://app.start", "end.com");
        assertTrue(countries.isEmpty());
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
    void negativeTestEmptyName() {
        String json = "{\n" +
                "   \"countries\":[\n" +
                "      {\n" +
                "         \"name\":\"\",\n" +
                "         \"id\":134,\n" +
                "         \"status\":\"Ok\",\n" +
                "         \"direct\":true\n" +
                "      }\n" +
                "   ]\n" +
                "}";

        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getCountries(json, "https://app.start", "end.com"));
        assertTrue(ex.getMessage().startsWith("Response error: country name is empty"));
    }

    @Test
    void negativeTestNullName() {
        String json = "{\n" +
                "   \"countries\":[\n" +
                "      {\n" +
                "         \"id\":134,\n" +
                "         \"status\":\"Ok\",\n" +
                "         \"direct\":true\n" +
                "      }\n" +
                "   ]\n" +
                "}";

        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getCountries(json, "https://app.start", "end.com"));
        assertTrue(ex.getMessage().startsWith("Response error: country name is empty"));
    }

    @Test
    void negativeTestEmptyId() {
        String json = "{\n" +
                "   \"countries\":[\n" +
                "      {\n" +
                "         \"name\":\"US\",\n" +
                "         \"id\":\"\",\n" +
                "         \"status\":\"Ok\",\n" +
                "         \"direct\":true\n" +
                "      }\n" +
                "   ]\n" +
                "}";
        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getCountries(json, "https://app.start", "end.com"));
        assertTrue(ex.getMessage().startsWith("Response error: country id is empty"));
    }

    @Test
    void negativeTestNullId() {
        String json = "{\n" +
                "   \"countries\":[\n" +
                "      {\n" +
                "         \"name\":\"US\",\n" +
                "         \"status\":\"Ok\",\n" +
                "         \"direct\":true\n" +
                "      }\n" +
                "   ]\n" +
                "}";
        StorageServerException ex = assertThrows(StorageServerException.class, () -> JsonUtils.getCountries(json, "https://app.start", "end.com"));
        assertTrue(ex.getMessage().startsWith("Response error: country id is empty"));
    }
}
