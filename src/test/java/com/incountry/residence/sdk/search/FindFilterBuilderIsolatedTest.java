package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.FilterNumberParam;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FindFilterBuilderIsolatedTest {

    @Test
    void defaultPositiveTest() throws StorageClientException {
        FindFilter filter = FindFilterBuilder.create().build();
        assertEquals(100, filter.getLimit());
        assertEquals(0, filter.getOffset());
    }

    @Test
    void clearTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        FindFilter filter1 = builder.build();
        FindFilter filter2 = builder.clear().build();
        assertNotSame(filter1, filter2);
    }

    @Test
    void toStringPositiveTest() throws StorageClientException {
        String string = FindFilterBuilder.create()
                .limitAndOffset(1, 2)
                .keyEq(StringField.RECORD_KEY, "3", "4")
                .keyEq(StringField.KEY2, "5", "6")
                .keyEq(StringField.KEY3, "7", "8")
                .keyEq(StringField.PROFILE_KEY, "9", "10")
                .keyNotEq(StringField.VERSION, "11", "12")
                .keyEq(NumberField.RANGE_KEY1, 13L, 14L)
                .build().toString();
        assertNotNull(string);
        assertTrue(string.contains("limit=1"));
        assertTrue(string.contains("offset=2"));
        assertTrue(string.contains("RECORD_KEY=FilterStringParam{value=[3, 4], notCondition=false}"));
        assertTrue(string.contains("KEY2=FilterStringParam{value=[5, 6], notCondition=false"));
        assertTrue(string.contains("KEY3=FilterStringParam{value=[7, 8], notCondition=false"));
        assertTrue(string.contains("PROFILE_KEY=FilterStringParam{value=[9, 10], notCondition=false}"));
        assertTrue(string.contains("VERSION=FilterStringParam{value=[11, 12], notCondition=true}"));
        assertTrue(string.contains("RANGE_KEY1=FilterRangeParam{values=[13, 14], operator1='null', operator2='null'}"));
    }


    @Test
    void negativeTestIllegalArgs() {
        String nullString = null;
        String[] nullArrayString = null;
        //recordKey
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(StringField.RECORD_KEY, nullString));
        assertEquals("FilterStringParam values can't be null", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(StringField.RECORD_KEY, nullArrayString));
        assertEquals("FilterStringParam values can't be null", ex2.getMessage());
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(StringField.RECORD_KEY, new String[]{}));
        assertEquals("FilterStringParam values can't be null", ex3.getMessage());
        //rangeKey1
        Long nullLong = null;
        Long[] nullArrayLong = null;
        StorageClientException ex19 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(NumberField.RANGE_KEY1, nullLong));
        assertEquals("FilterNumberParam values can't be null", ex19.getMessage());
        StorageClientException ex20 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(NumberField.RANGE_KEY1, nullArrayLong));
        assertEquals("FilterNumberParam values can't be null", ex20.getMessage());
        StorageClientException ex21 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(NumberField.RANGE_KEY1, new Long[]{}));
        assertEquals("FilterNumberParam values can't be null", ex21.getMessage());
        //limit & offset
        StorageClientException ex22 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().limitAndOffset(-1, 0));
        assertEquals("Limit must be more than 1", ex22.getMessage());
        StorageClientException ex23 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().limitAndOffset(1, -1));
        assertEquals("Offset must be more than 0", ex23.getMessage());
        //between
        StorageClientException ex24 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyBetween(NumberField.RANGE_KEY1, 1, -1));
        assertEquals("Value1 in range filter can by only less or equals value2", ex24.getMessage());
    }

    @Test
    void positiveStringKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        for (StringField field : StringField.values()) {
            assertEquals("1", builder.keyEq(field, "1").build().getStringFilterMap().get(field).getValues().get(0));
            assertEquals("2", builder.keyEq(field, "1", "2").build().getStringFilterMap().get(field).getValues().get(1));
            assertFalse(builder.keyEq(field, "4").build().getStringFilterMap().get(field).isNotCondition());
            assertEquals("6", builder.keyNotEq(field, "5", "6").build().getStringFilterMap().get(field).getValues().get(1));
            assertTrue(builder.keyNotEq(field, "7", "8").build().getStringFilterMap().get(field).isNotCondition());
            assertFalse(builder.keyEq(field, "9", "10").build().getStringFilterMap().get(field).isNotCondition());
        }
    }

    @Test
    void positiveRangeKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        for (NumberField field : NumberField.values()) {
            assertEquals(1L, builder.keyEq(field, 1L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(2L, builder.keyEq(field, 1L, 2L).build().getNumberFilterMap().get(field).getValues()[1]);
            assertEquals(9L, builder.keyGT(field, 9L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(FindFilterBuilder.OPER_GT, builder.keyGT(field, 10L).build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(11L, builder.keyGTE(field, 11L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(FindFilterBuilder.OPER_GTE, builder.keyGTE(field, 12L).build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(13L, builder.keyLT(field, 13L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(FindFilterBuilder.OPER_LT, builder.keyLT(field, 14L).build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(15L, builder.keyLTE(field, 15L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(FindFilterBuilder.OPER_LTE, builder.keyLTE(field, 16L).build().getNumberFilterMap().get(field).getOperator1());
            builder.keyBetween(field, 17L, 18L);
            assertEquals(FindFilterBuilder.OPER_GTE, builder.build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(FindFilterBuilder.OPER_LTE, builder.build().getNumberFilterMap().get(field).getOperator2());
            assertEquals(17L, builder.build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(18L, builder.build().getNumberFilterMap().get(field).getValues()[1]);
            builder.keyBetween(field, 19L, true, 20L, false);
            assertEquals(FindFilterBuilder.OPER_GTE, builder.build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(FindFilterBuilder.OPER_LT, builder.build().getNumberFilterMap().get(field).getOperator2());
            assertEquals(19L, builder.build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(20L, builder.build().getNumberFilterMap().get(field).getValues()[1]);
        }
    }

    @Test
    void testFiltersForSchema2point2() throws StorageClientException {
        FindFilter filter = FindFilterBuilder.create()
                .keyEq(StringField.RECORD_KEY, "1a", "2b")
                .keyEq(StringField.KEY1, "3c", "4d")
                .keyEq(StringField.KEY4, "5e", "6f")
                .keyEq(StringField.KEY5, "7g", "8h")
                .keyEq(StringField.KEY6, "9i", "10j")
                .keyEq(StringField.KEY7, "11k", "12l")
                .keyEq(StringField.KEY8, "13m", "14n")
                .keyEq(StringField.KEY9, "15o", "16p")
                .keyEq(StringField.KEY10, "17q", "18r")
                .keyEq(StringField.SERVICE_KEY1, "19s", "20t")
                .keyEq(StringField.SERVICE_KEY2, "21u", "22v")
                .keyEq(NumberField.RANGE_KEY1, 23L, 24L)
                .keyEq(NumberField.RANGE_KEY2, 25L, 26L)
                .keyEq(NumberField.RANGE_KEY3, 27L, 28L)
                .keyEq(NumberField.RANGE_KEY4, 29L, 30L)
                .keyEq(NumberField.RANGE_KEY5, 31L, 32L)
                .keyEq(NumberField.RANGE_KEY6, 33L, 34L)
                .keyEq(NumberField.RANGE_KEY7, 35L, 36L)
                .keyEq(NumberField.RANGE_KEY8, 37L, 38L)
                .keyEq(NumberField.RANGE_KEY9, 39L, 40L)
                .keyEq(NumberField.RANGE_KEY10, 41L, 42L)
                .build();

        checkStringFilters(filter);
        checkNumberFilters(filter);
        checkJsonFilter(filter);
    }

    private void checkJsonFilter(FindFilter filter) throws StorageClientException {
        String jsonFilter = JsonUtils.toJsonString(filter, new CryptoManager(null, "<envId>", null, false, true));
        assertTrue(jsonFilter.contains("\"record_key\":["));
        assertTrue(jsonFilter.contains("\"key1\":["));
        assertTrue(jsonFilter.contains("\"key4\":["));
        assertTrue(jsonFilter.contains("\"key5\":["));
        assertTrue(jsonFilter.contains("\"key6\":["));
        assertTrue(jsonFilter.contains("\"key7\":["));
        assertTrue(jsonFilter.contains("\"key8\":["));
        assertTrue(jsonFilter.contains("\"key9\":["));
        assertTrue(jsonFilter.contains("\"key10\":["));
        assertTrue(jsonFilter.contains("\"service_key1\":["));
        assertTrue(jsonFilter.contains("\"service_key2\":["));

        assertTrue(jsonFilter.contains("range_key1\":[23,24]"));
        assertTrue(jsonFilter.contains("range_key2\":[25,26]"));
        assertTrue(jsonFilter.contains("range_key3\":[27,28]"));
        assertTrue(jsonFilter.contains("range_key4\":[29,30]"));
        assertTrue(jsonFilter.contains("range_key5\":[31,32]"));
        assertTrue(jsonFilter.contains("range_key6\":[33,34]"));
        assertTrue(jsonFilter.contains("range_key7\":[35,36]"));
        assertTrue(jsonFilter.contains("range_key8\":[37,38]"));
        assertTrue(jsonFilter.contains("range_key9\":[39,40]"));
        assertTrue(jsonFilter.contains("range_key10\":[41,42]"));
    }

    private void checkNumberFilters(FindFilter filter) {
        Map<NumberField, FilterNumberParam> filterMap = filter.getNumberFilterMap();
        assertEquals("FilterRangeParam{values=[23, 24], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY1).toString());
        assertEquals("FilterRangeParam{values=[25, 26], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY2).toString());
        assertEquals("FilterRangeParam{values=[27, 28], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY3).toString());
        assertEquals("FilterRangeParam{values=[29, 30], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY4).toString());
        assertEquals("FilterRangeParam{values=[31, 32], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY5).toString());
        assertEquals("FilterRangeParam{values=[33, 34], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY6).toString());
        assertEquals("FilterRangeParam{values=[35, 36], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY7).toString());
        assertEquals("FilterRangeParam{values=[37, 38], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY8).toString());
        assertEquals("FilterRangeParam{values=[39, 40], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY9).toString());
        assertEquals("FilterRangeParam{values=[41, 42], operator1='null', operator2='null'}", filterMap.get(NumberField.RANGE_KEY10).toString());
    }

    private void checkStringFilters(FindFilter filter) {
        Map<StringField, FilterStringParam> filterMap = filter.getStringFilterMap();
        assertEquals("FilterStringParam{value=[1a, 2b], notCondition=false}", filterMap.get(StringField.RECORD_KEY).toString());
        assertEquals("FilterStringParam{value=[3c, 4d], notCondition=false}", filterMap.get(StringField.KEY1).toString());
        assertEquals("FilterStringParam{value=[5e, 6f], notCondition=false}", filterMap.get(StringField.KEY4).toString());
        assertEquals("FilterStringParam{value=[7g, 8h], notCondition=false}", filterMap.get(StringField.KEY5).toString());
        assertEquals("FilterStringParam{value=[9i, 10j], notCondition=false}", filterMap.get(StringField.KEY6).toString());
        assertEquals("FilterStringParam{value=[11k, 12l], notCondition=false}", filterMap.get(StringField.KEY7).toString());
        assertEquals("FilterStringParam{value=[13m, 14n], notCondition=false}", filterMap.get(StringField.KEY8).toString());
        assertEquals("FilterStringParam{value=[15o, 16p], notCondition=false}", filterMap.get(StringField.KEY9).toString());
        assertEquals("FilterStringParam{value=[17q, 18r], notCondition=false}", filterMap.get(StringField.KEY10).toString());
        assertEquals("FilterStringParam{value=[19s, 20t], notCondition=false}", filterMap.get(StringField.SERVICE_KEY1).toString());
        assertEquals("FilterStringParam{value=[21u, 22v], notCondition=false}", filterMap.get(StringField.SERVICE_KEY2).toString());
    }
}
