package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.filters.Filter;
import com.incountry.residence.sdk.dto.search.filters.StringFilter;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.incountry.residence.sdk.search.FindFilterTest.getGson4Records;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringFilterTest {

    @Test
    void oneStringValueTest() throws StorageClientException {
        String value = "someStringValue";
        List<String> values = new ArrayList<>();
        values.add(value);
        Filter filter = new StringFilter(values);
        assertNotNull(filter);
        assertEquals("[\"someStringValue\"]", getGson4Records().toJson(filter.toTransferObject()));
    }

    @Test
    void manyStringValuesTest() throws StorageClientException {
        String value1 = "someStringValue1";
        String value2 = "someStringValue2";
        String value3 = "someStringValue3";
        List<String> values = new ArrayList<>();
        values.add(value1);
        values.add(value2);
        values.add(value3);
        Filter filter = new StringFilter(values);
        assertNotNull(filter);
        assertEquals("[\"someStringValue1\",\"someStringValue2\",\"someStringValue3\"]",
                getGson4Records().toJson(filter.toTransferObject()));
    }

    @Test
    void notConditionWithStringTest() throws StorageClientException {
        String value1 = "someStringValue1";
        String value2 = "someStringValue2";
        String value3 = "someStringValue3";
        List<String> values = new ArrayList<>();
        values.add(value1);
        values.add(value2);
        values.add(value3);
        Filter filter = new StringFilter(values, true);
        assertNotNull(filter);
        assertEquals("{\"$not\":[\"someStringValue1\",\"someStringValue2\",\"someStringValue3\"]}",
                getGson4Records().toJson(filter.toTransferObject()));
    }

    @Test
    void stringFilterCreationNegativeTest() {
        String expectedError = "String filter or it's values can't be null";
        StorageClientException ex = assertThrows(StorageClientException.class, () ->  new StringFilter(null, true));
        assertEquals(expectedError, ex.getMessage());

        List<String> values = new ArrayList<>();
        values.add(null);
        values.add("");
        ex = assertThrows(StorageClientException.class, () ->  new StringFilter(values, true));
        assertEquals(expectedError, ex.getMessage());
    }
}
