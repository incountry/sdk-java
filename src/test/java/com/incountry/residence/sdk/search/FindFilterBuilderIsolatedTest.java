package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

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
                .rangeKeyEq(13L, 14L)
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
        Long nullLong = null;
        Long[] nullArrayLong = null;
        StorageClientException ex19 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyEq(nullLong));
        assertEquals("FilterNumberParam values can't be null", ex19.getMessage());
        StorageClientException ex20 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyEq(nullArrayLong));
        assertEquals("FilterNumberParam values can't be null", ex20.getMessage());
        StorageClientException ex21 = assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyEq(new Long[]{}));
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
        assertEquals(1L, builder.rangeKeyEq(1L).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(2L, builder.rangeKeyEq(1L, 2L).build().getRangeKeyFilter().getValues()[1]);
        assertEquals(9L, builder.rangeKeyGT(9L).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(FindFilterBuilder.OPER_GT, builder.rangeKeyGT(10L).build().getRangeKeyFilter().getOperator1());
        assertEquals(11L, builder.rangeKeyGTE(11L).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(FindFilterBuilder.OPER_GTE, builder.rangeKeyGTE(12L).build().getRangeKeyFilter().getOperator1());
        assertEquals(13L, builder.rangeKeyLT(13L).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(FindFilterBuilder.OPER_LT, builder.rangeKeyLT(14L).build().getRangeKeyFilter().getOperator1());
        assertEquals(15L, builder.rangeKeyLTE(15L).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(FindFilterBuilder.OPER_LTE, builder.rangeKeyLTE(16L).build().getRangeKeyFilter().getOperator1());
        builder.rangeKeyBetween(17L, 18L);
        assertEquals(FindFilterBuilder.OPER_GTE, builder.build().getRangeKeyFilter().getOperator1());
        assertEquals(FindFilterBuilder.OPER_LTE, builder.build().getRangeKeyFilter().getOperator2());
        assertEquals(17L, builder.build().getRangeKeyFilter().getValues()[0]);
        assertEquals(18L, builder.build().getRangeKeyFilter().getValues()[1]);
        builder.rangeKeyBetween(19L, true, 20L, false);
        assertEquals(FindFilterBuilder.OPER_GTE, builder.build().getRangeKeyFilter().getOperator1());
        assertEquals(FindFilterBuilder.OPER_LT, builder.build().getRangeKeyFilter().getOperator2());
        assertEquals(19L, builder.build().getRangeKeyFilter().getValues()[0]);
        assertEquals(20L, builder.build().getRangeKeyFilter().getValues()[1]);
    }

    @Test
    void positiveKeyFilterReflectionTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        for (int i = 1; i < 11; i++) {
            String postfix = i == 1 ? "" : String.valueOf(i);
            builder.clear();
            Method methodKeyEq = FindFilterBuilder.class.getMethod("key" + postfix + "Eq", String[].class);
            String randomValue = UUID.randomUUID().toString();
            String[] arr = new String[]{randomValue};
            methodKeyEq.invoke(builder, new Object[]{arr});
            FindFilter filter = builder.build();
            Method getMethod = filter.getClass().getMethod("getKey" + postfix + "Filter");
            assertEquals("FilterStringParam{value=[" + randomValue + "], notCondition=false}",
                    getMethod.invoke(filter).toString());

            builder.clear();
            Method methodKeyNotEq = FindFilterBuilder.class.getMethod("key" + postfix + "NotEq", String[].class);
            methodKeyNotEq.invoke(builder, new Object[]{arr});
            filter = builder.build();
            getMethod = filter.getClass().getMethod("getKey" + postfix + "Filter");
            assertEquals("FilterStringParam{value=[" + randomValue + "], notCondition=true}",
                    getMethod.invoke(filter).toString());
        }
    }

    @Test
    void positiveRangeKeyFilterReflectionTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        for (int i = 1; i < 11; i++) {
            String postfix = i == 1 ? "" : String.valueOf(i);
            builder.clear();
            Method methodRangeKeyEq = FindFilterBuilder.class.getMethod("rangeKey" + postfix + "Eq", Long[].class);
            Long[] arr = new Long[]{(long) i};
            methodRangeKeyEq.invoke(builder, new Object[]{arr});
            FindFilter filter = builder.build();
            Method getMethod = filter.getClass().getMethod("getRangeKey" + postfix + "Filter");
            assertEquals("FilterRangeParam{values=[" + i + "], operator1='null', operator2='null'}",
                    getMethod.invoke(filter).toString());

            builder.clear();
            Method methodRangeKeyGT = FindFilterBuilder.class.getMethod("rangeKey" + postfix + "GT", long.class);
            methodRangeKeyGT.invoke(builder, (long) i);
            filter = builder.build();
            //Method getMethod = filter.getClass().getMethod("getRangeKey" + postfix + "Filter");
            assertEquals("FilterRangeParam{values=[" + i + "], operator1='$gt', operator2='null'}",
                    getMethod.invoke(filter).toString());

            builder.clear();
            Method methodRangeKeyGTE = FindFilterBuilder.class.getMethod("rangeKey" + postfix + "GTE", long.class);
            methodRangeKeyGTE.invoke(builder, (long) i);
            filter = builder.build();
            //Method getMethod = filter.getClass().getMethod("getRangeKey" + postfix + "Filter");
            assertEquals("FilterRangeParam{values=[" + i + "], operator1='$gte', operator2='null'}",
                    getMethod.invoke(filter).toString());

            builder.clear();
            Method methodRangeKeyLT = FindFilterBuilder.class.getMethod("rangeKey" + postfix + "LT", long.class);
            methodRangeKeyLT.invoke(builder, (long) i);
            filter = builder.build();
            //Method getMethod = filter.getClass().getMethod("getRangeKey" + postfix + "Filter");
            assertEquals("FilterRangeParam{values=[" + i + "], operator1='$lt', operator2='null'}",
                    getMethod.invoke(filter).toString());

            builder.clear();
            Method methodRangeKeyLTE = FindFilterBuilder.class.getMethod("rangeKey" + postfix + "LTE", long.class);
            methodRangeKeyLTE.invoke(builder, (long) i);
            filter = builder.build();
            //Method getMethod = filter.getClass().getMethod("getRangeKey" + postfix + "Filter");
            assertEquals("FilterRangeParam{values=[" + i + "], operator1='$lte', operator2='null'}",
                    getMethod.invoke(filter).toString());

            builder.clear();
            Method methodRangeKeyBetween = FindFilterBuilder.class.getMethod("rangeKey" + postfix + "Between", long.class, long.class);
            methodRangeKeyBetween.invoke(builder, (long) i, (long) (i + 1));
            filter = builder.build();
            //Method getMethod = filter.getClass().getMethod("getRangeKey" + postfix + "Filter");
            assertEquals("FilterRangeParam{values=[" + i + ", " + (i + 1) + "], operator1='$gte', operator2='$lte'}",
                    getMethod.invoke(filter).toString());

            builder.clear();
            Method methodRangeKeyBetweenIncluding = FindFilterBuilder.class.getMethod("rangeKey" + postfix + "Between",
                    long.class, boolean.class, long.class, boolean.class);
            methodRangeKeyBetweenIncluding.invoke(builder, (long) i, false, (long) (i + 1), false);
            filter = builder.build();
            //Method getMethod = filter.getClass().getMethod("getRangeKey" + postfix + "Filter");
            assertEquals("FilterRangeParam{values=[" + i + ", " + (i + 1) + "], operator1='$gt', operator2='$lt'}",
                    getMethod.invoke(filter).toString());
        }
    }

    @Test
    void positiveErrorCorrectionKeyFilterTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .errorCorrectionKey1Eq("err1_test1", "err1_test2")
                .errorCorrectionKey2Eq("err2_test1", "err2_test2");
        assertEquals("FilterStringParam{value=[err1_test1, err1_test2], notCondition=false}",
                builder.build().getErrorCorrectionKey1Filter().toString());

        assertEquals("FilterStringParam{value=[err2_test1, err2_test2], notCondition=false}",
                builder.build().getErrorCorrectionKey2Filter().toString());

        builder = builder
                .clear()
                .errorCorrectionKey1NotEq("err3_test1", "err3_test2")
                .errorCorrectionKey2NotEq("err4_test1", "err4_test2");
        assertEquals("FilterStringParam{value=[err3_test1, err3_test2], notCondition=true}",
                builder.build().getErrorCorrectionKey1Filter().toString());

        assertEquals("FilterStringParam{value=[err4_test1, err4_test2], notCondition=true}",
                builder.build().getErrorCorrectionKey2Filter().toString());
    }
}
