package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.filters.Filter;
import com.incountry.residence.sdk.dto.search.filters.NumberFilter;
import com.incountry.residence.sdk.dto.search.filters.RangeFilter;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.incountry.residence.sdk.search.FindFilterTest.getGson4Records;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberFilterTest {

    @Test
    void oneNumberValueTest() throws StorageClientException {
        long value = 100500L;
        Filter filter = new NumberFilter(new ArrayList<Long>() {{
            add(value);
        }}
        );
        assertNotNull(filter);
        assertEquals("[100500]", getGson4Records().toJson(filter.toTransferObject()));
    }

    @Test
    void manyNumberValuesTest() throws StorageClientException {
        long value1 = 100500L;
        long value2 = 2020L;
        long value3 = 314L;
        Filter filter = new NumberFilter(new ArrayList<Long>() {{
            add(value1);
            add(value2);
            add(value3);
        }}
        );
        assertNotNull(filter);
        assertEquals("[100500,2020,314]", getGson4Records().toJson(filter.toTransferObject()));
    }

    @Test
    void notOperatorWithNumberTest() throws StorageClientException {
        long value1 = 100500L;
        long value2 = 2020L;
        long value3 = 314L;
        Filter filter = new NumberFilter(new ArrayList<Long>() {{
            add(value1);
            add(value2);
            add(value3);
        }},
                Filter.OPERATOR_NOT);
        assertNotNull(filter);
        assertEquals("{\"$not\":[100500,2020,314]}", getGson4Records().toJson(filter.toTransferObject()));
    }

    @Test
    void betweenNumberRangeTest() throws StorageClientException {
        long value1 = 123L;
        long value2 = 456L;
        Filter filter = new RangeFilter(value1, Filter.OPERATOR_GREATER_OR_EQUALS, value2, Filter.OPERATOR_LESS_OR_EQUALS);
        assertNotNull(filter);
        assertEquals("{\"$gte\":123,\"$lte\":456}", getGson4Records().toJson(filter.toTransferObject()));
    }

    @Test
    void validateNumberFilterTest() {
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new NumberFilter(null));
        assertEquals("Number filter or it's values can't be null", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new NumberFilter(new ArrayList<Long>() {{
            add(1l);
            add(2l);
        }},
                "illegal"));
        assertEquals("Operator in non range number filter can by only in [NULL,$not,$lt,$lte,$gt,$gte]", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new RangeFilter(1, "$gt", 2, "illegal"));
        assertEquals("Operator2 in range number filter can by only in [$lt,$lte]", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new RangeFilter(2, "$gt", 1, "$lt"));
        assertEquals("The first value in range filter can by only less or equals the second value", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new RangeFilter(2, null, 1, "$lt"));
        assertEquals("Operator1 in range number filter can by only in [$gt,$gte]", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> new NumberFilter(new ArrayList<Long>() {{
            add(2l);
            add(1l);
        }},
                "$gt"));
        assertEquals("Operator in list number filter can by only in [NULL,$not]", ex.getMessage());
    }
}
