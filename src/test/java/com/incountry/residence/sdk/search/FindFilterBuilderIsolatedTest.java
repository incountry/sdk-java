package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
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
                .keyEq("3", "4")
                .key2Eq("5", "6")
                .key3Eq("7", "8")
                .profileKeyEq("9", "10")
                .versionNotEq("11", "12")
                .rangeKeyEq(13, 14)
                .build().toString();
        assertNotNull(string);
        assertTrue(string.contains("limit=1"));
        assertTrue(string.contains("offset=2"));
        assertTrue(string.contains("keyFilter=FilterStringParam{value=[3, 4], notCondition=false}"));
        assertTrue(string.contains("key2Filter=FilterStringParam{value=[5, 6], notCondition=false"));
        assertTrue(string.contains("key3Filter=FilterStringParam{value=[7, 8], notCondition=false"));
        assertTrue(string.contains("profileKeyFilter=FilterStringParam{value=[9, 10], notCondition=false}"));
        assertTrue(string.contains("versionFilter=FilterStringParam{value=[11, 12], notCondition=true}"));
        assertTrue(string.contains("rangeKeyFilter=FilterRangeParam{values=[13, 14], operator1='null', operator2='null'}"));
    }


    @Test
    void negativeTestIllegalArgs() {
        String nullString = null;
        String[] nullArrayString = null;
        //key
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(nullString));
        assertEquals("FilterStringParam values can't be null", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(nullArrayString));
        assertEquals("FilterStringParam values can't be null", ex2.getMessage());
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(new String[]{}));
        assertEquals("FilterStringParam values can't be null", ex3.getMessage());
        //key2
        StorageClientException ex4 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key2Eq(nullString));
        assertEquals("FilterStringParam values can't be null", ex4.getMessage());
        StorageClientException ex5 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key2Eq(nullArrayString));
        assertEquals("FilterStringParam values can't be null", ex5.getMessage());
        StorageClientException ex6 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key2Eq(new String[]{}));
        assertEquals("FilterStringParam values can't be null", ex6.getMessage());
        //key3
        StorageClientException ex7 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key3Eq(nullString));
        assertEquals("FilterStringParam values can't be null", ex7.getMessage());
        StorageClientException ex8 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key3Eq(nullArrayString));
        assertEquals("FilterStringParam values can't be null", ex8.getMessage());
        StorageClientException ex9 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key3Eq(new String[]{}));
        assertEquals("FilterStringParam values can't be null", ex9.getMessage());
        //version
        StorageClientException ex10 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionEq(nullString));
        assertEquals("FilterStringParam values can't be null", ex10.getMessage());
        StorageClientException ex11 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionEq(nullArrayString));
        assertEquals("FilterStringParam values can't be null", ex11.getMessage());
        StorageClientException ex12 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionEq(new String[]{}));
        assertEquals("FilterStringParam values can't be null", ex12.getMessage());
        StorageClientException ex13 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionNotEq(nullString));
        assertEquals("FilterStringParam values can't be null", ex13.getMessage());
        StorageClientException ex14 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionNotEq(nullArrayString));
        assertEquals("FilterStringParam values can't be null", ex14.getMessage());
        StorageClientException ex15 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionNotEq(new String[]{}));
        assertEquals("FilterStringParam values can't be null", ex15.getMessage());
        //profileKey
        StorageClientException ex16 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().profileKeyEq(nullString));
        assertEquals("FilterStringParam values can't be null", ex16.getMessage());
        StorageClientException ex17 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().profileKeyEq(nullArrayString));
        assertEquals("FilterStringParam values can't be null", ex17.getMessage());
        StorageClientException ex18 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().profileKeyEq(new String[]{}));
        assertEquals("FilterStringParam values can't be null", ex18.getMessage());
        //rangeKey
        Integer nullInt = null;
        Integer[] nullArrayInt = null;
        StorageClientException ex19 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyEq(nullInt));
        assertEquals("FilterNumberParam values can't be null", ex19.getMessage());
        StorageClientException ex20 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyEq(nullArrayInt));
        assertEquals("FilterNumberParam values can't be null", ex20.getMessage());
        StorageClientException ex21 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyEq(new Integer[]{}));
        assertEquals("FilterNumberParam values can't be null", ex21.getMessage());
        //limit & offset
        StorageClientException ex22 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().limitAndOffset(-1, 0));
        assertEquals("Limit must be more than 1", ex22.getMessage());
        StorageClientException ex23 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().limitAndOffset(1, -1));
        assertEquals("Offset must be more than 0", ex23.getMessage());
        StorageClientException ex24 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyBetween(1, -1));
        assertEquals("Value1 in range filter can by only less or equals value2", ex24.getMessage());
    }

    @Test
    void positiveKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.keyEq("1").build().getKeyFilter().getValues().get(0));
        assertEquals("2", builder.keyEq("1", "2").build().getKeyFilter().getValues().get(1));
        assertFalse(builder.keyEq("4").build().getKeyFilter().isNotCondition());
        assertEquals("6", builder.keyNotEq("5", "6").build().getKeyFilter().getValues().get(1));
        assertTrue(builder.keyNotEq("7", "8").build().getKeyFilter().isNotCondition());
        assertFalse(builder.keyEq("9", "10").build().getKeyFilter().isNotCondition());
    }

    @Test
    void positiveKey2Test() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.key2Eq("1").build().getKey2Filter().getValues().get(0));
        assertEquals("2", builder.key2Eq("1", "2").build().getKey2Filter().getValues().get(1));
        assertFalse(builder.key2Eq("4").build().getKey2Filter().isNotCondition());
        assertEquals("6", builder.key2NotEq("5", "6").build().getKey2Filter().getValues().get(1));
        assertTrue(builder.key2NotEq("7", "8").build().getKey2Filter().isNotCondition());
        assertFalse(builder.key2Eq("9", "10").build().getKey2Filter().isNotCondition());
    }

    @Test
    void positiveKey3Test() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.key3Eq("1").build().getKey3Filter().getValues().get(0));
        assertEquals("2", builder.key3Eq("1", "2").build().getKey3Filter().getValues().get(1));
        assertFalse(builder.key3Eq("4").build().getKey3Filter().isNotCondition());
        assertEquals("6", builder.key3NotEq("5", "6").build().getKey3Filter().getValues().get(1));
        assertTrue(builder.key3NotEq("7", "8").build().getKey3Filter().isNotCondition());
        assertFalse(builder.key3Eq("9", "10").build().getKey3Filter().isNotCondition());
    }

    @Test
    void positiveProfileTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.profileKeyEq("1").build().getProfileKeyFilter().getValues().get(0));
        assertEquals("2", builder.profileKeyEq("1", "2").build().getProfileKeyFilter().getValues().get(1));
        assertFalse(builder.profileKeyEq("4").build().getProfileKeyFilter().isNotCondition());
        assertEquals("6", builder.profileKeyNotEq("5", "6").build().getProfileKeyFilter().getValues().get(1));
        assertTrue(builder.profileKeyNotEq("7", "8").build().getProfileKeyFilter().isNotCondition());
        assertFalse(builder.profileKeyEq("9", "10").build().getProfileKeyFilter().isNotCondition());
    }

    @Test
    void positiveVersionTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.versionEq("1").build().getVersionFilter().getValues().get(0));
        assertEquals("2", builder.versionEq("1", "2").build().getVersionFilter().getValues().get(1));
        assertEquals("3", builder.versionNotEq("3").build().getVersionFilter().getValues().get(0));
        assertTrue(builder.versionNotEq("4").build().getVersionFilter().isNotCondition());
        assertEquals("6", builder.versionNotEq("5", "6").build().getVersionFilter().getValues().get(1));
        assertTrue(builder.versionNotEq("7", "8").build().getVersionFilter().isNotCondition());
    }

    @Test
    void positiveRangeKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals(1, builder.rangeKeyEq(1).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(2, builder.rangeKeyEq(1, 2).build().getRangeKeyFilter().getValues()[1]);
        assertEquals(9, builder.rangeKeyGT(9).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(FindFilterBuilder.OPER_GT, builder.rangeKeyGT(10).build().getRangeKeyFilter().getOperator1());
        assertEquals(11, builder.rangeKeyGTE(11).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(FindFilterBuilder.OPER_GTE, builder.rangeKeyGTE(12).build().getRangeKeyFilter().getOperator1());
        assertEquals(13, builder.rangeKeyLT(13).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(FindFilterBuilder.OPER_LT, builder.rangeKeyLT(14).build().getRangeKeyFilter().getOperator1());
        assertEquals(15, builder.rangeKeyLTE(15).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(FindFilterBuilder.OPER_LTE, builder.rangeKeyLTE(16).build().getRangeKeyFilter().getOperator1());
        builder.rangeKeyBetween(17, 18);
        assertEquals(FindFilterBuilder.OPER_GTE, builder.build().getRangeKeyFilter().getOperator1());
        assertEquals(FindFilterBuilder.OPER_LTE, builder.build().getRangeKeyFilter().getOperator2());
        assertEquals(17, builder.build().getRangeKeyFilter().getValues()[0]);
        assertEquals(18, builder.build().getRangeKeyFilter().getValues()[1]);
        builder.rangeKeyBetween(19, true, 20, false);
        assertEquals(FindFilterBuilder.OPER_GTE, builder.build().getRangeKeyFilter().getOperator1());
        assertEquals(FindFilterBuilder.OPER_LT, builder.build().getRangeKeyFilter().getOperator2());
        assertEquals(19, builder.build().getRangeKeyFilter().getValues()[0]);
        assertEquals(20, builder.build().getRangeKeyFilter().getValues()[1]);
    }
}
