package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

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
                .stringKeyEq(StringField.RECORD_KEY, "3", "4")
                .stringKeyEq(StringField.KEY2, "5", "6")
                .stringKeyEq(StringField.KEY3, "7", "8")
                .stringKeyEq(StringField.PROFILE_KEY, "9", "10")
                .stringKeyNotEq(StringField.VERSION, "11", "12")
                .numberKeyEq(NumberField.RANGE_KEY1, 13L, 14L)
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
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().stringKeyEq(StringField.RECORD_KEY, nullString));
        assertEquals("FilterStringParam values can't be null", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().stringKeyEq(StringField.RECORD_KEY, nullArrayString));
        assertEquals("FilterStringParam values can't be null", ex2.getMessage());
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().stringKeyEq(StringField.RECORD_KEY, new String[]{}));
        assertEquals("FilterStringParam values can't be null", ex3.getMessage());
        //rangeKey1
        Long nullLong = null;
        Long[] nullArrayLong = null;
        StorageClientException ex19 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().numberKeyEq(NumberField.RANGE_KEY1, nullLong));
        assertEquals("FilterNumberParam values can't be null", ex19.getMessage());
        StorageClientException ex20 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().numberKeyEq(NumberField.RANGE_KEY1, nullArrayLong));
        assertEquals("FilterNumberParam values can't be null", ex20.getMessage());
        StorageClientException ex21 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().numberKeyEq(NumberField.RANGE_KEY1, new Long[]{}));
        assertEquals("FilterNumberParam values can't be null", ex21.getMessage());
        //limit & offset
        StorageClientException ex22 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().limitAndOffset(-1, 0));
        assertEquals("Limit must be more than 1", ex22.getMessage());
        StorageClientException ex23 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().limitAndOffset(1, -1));
        assertEquals("Offset must be more than 0", ex23.getMessage());
        //between
        StorageClientException ex24 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().numberKeyBetween(NumberField.RANGE_KEY1, 1, -1));
        assertEquals("Value1 in range filter can by only less or equals value2", ex24.getMessage());
    }

    @Test
    void positiveStringKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        for (StringField field : StringField.values()) {
            assertEquals("1", builder.stringKeyEq(field, "1").build().getStringFilterMap().get(field).getValues().get(0));
            assertEquals("2", builder.stringKeyEq(field, "1", "2").build().getStringFilterMap().get(field).getValues().get(1));
            assertFalse(builder.stringKeyEq(field, "4").build().getStringFilterMap().get(field).isNotCondition());
            assertEquals("6", builder.stringKeyNotEq(field, "5", "6").build().getStringFilterMap().get(field).getValues().get(1));
            assertTrue(builder.stringKeyNotEq(field, "7", "8").build().getStringFilterMap().get(field).isNotCondition());
            assertFalse(builder.stringKeyEq(field, "9", "10").build().getStringFilterMap().get(field).isNotCondition());
        }
    }

    @Test
    void positiveRangeKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        for (NumberField field : NumberField.values()) {
            assertEquals(1L, builder.numberKeyEq(field, 1L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(2L, builder.numberKeyEq(field, 1L, 2L).build().getNumberFilterMap().get(field).getValues()[1]);
            assertEquals(9L, builder.numberKeyGT(field, 9L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(FindFilterBuilder.OPER_GT, builder.numberKeyGT(field, 10L).build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(11L, builder.numberKeyGTE(field, 11L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(FindFilterBuilder.OPER_GTE, builder.numberKeyGTE(field, 12L).build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(13L, builder.numberKeyLT(field, 13L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(FindFilterBuilder.OPER_LT, builder.numberKeyLT(field, 14L).build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(15L, builder.numberKeyLTE(field, 15L).build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(FindFilterBuilder.OPER_LTE, builder.numberKeyLTE(field, 16L).build().getNumberFilterMap().get(field).getOperator1());
            builder.numberKeyBetween(field, 17L, 18L);
            assertEquals(FindFilterBuilder.OPER_GTE, builder.build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(FindFilterBuilder.OPER_LTE, builder.build().getNumberFilterMap().get(field).getOperator2());
            assertEquals(17L, builder.build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(18L, builder.build().getNumberFilterMap().get(field).getValues()[1]);
            builder.numberKeyBetween(field, 19L, true, 20L, false);
            assertEquals(FindFilterBuilder.OPER_GTE, builder.build().getNumberFilterMap().get(field).getOperator1());
            assertEquals(FindFilterBuilder.OPER_LT, builder.build().getNumberFilterMap().get(field).getOperator2());
            assertEquals(19L, builder.build().getNumberFilterMap().get(field).getValues()[0]);
            assertEquals(20L, builder.build().getNumberFilterMap().get(field).getValues()[1]);
        }
    }
}
