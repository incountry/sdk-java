package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.internal.Filter;
import com.incountry.residence.sdk.dto.search.internal.NumberFilter;
import com.incountry.residence.sdk.dto.search.internal.RangeFilter;
import com.incountry.residence.sdk.dto.search.internal.StringFilter;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiltersTest {
    @Test
    void stringFilterNegative() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new StringFilter(null, true));
        assertEquals("StringFilter values can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> new StringFilter(new String[]{}, true));
        assertEquals("StringFilter values can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () -> new StringFilter(new String[]{null}, true));
        assertEquals("StringFilter values can't be null", ex.getMessage());
    }

    @Test
    void numberFilterNegative() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new NumberFilter(null, Filter.OPERATOR_NOT));
        assertEquals("Number filter or it's values can't be null", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new NumberFilter(new Long[]{}, Filter.OPERATOR_NOT));
        assertEquals("Number filter or it's values can't be null", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new NumberFilter(new Long[]{1L}, UUID.randomUUID().toString()));
        assertEquals("Operator in non range number filter can by only in [NULL,$not,$lt,$lte,$gt,$gte]", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new NumberFilter(new Long[]{1L, 2L}, Filter.OPERATOR_LESS));
        assertEquals("Operator in list number filter can by only in [NULL,$not]", ex.getMessage());
    }

    @Test
    void rangeFilterNegative() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new RangeFilter(1L, "wrongOperator", 2L, null));
        assertEquals("Operator1 in range number filter can by only in [$gt,$gte]", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new RangeFilter(1L, Filter.OPERATOR_GREATER, 2L, "wrongOperator"));
        assertEquals("Operator2 in range number filter can by only in [$lt,$lte]", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new RangeFilter(2L, Filter.OPERATOR_GREATER, 1L, Filter.OPERATOR_LESS));
        assertEquals("The first value in range filter can by only less or equals the second value", ex.getMessage());
    }
}
