package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FindFilterBuilderIsolatedTest {

    @Test
    public void defaultPositiveTest() throws StorageClientException {
        FindFilter filter = FindFilterBuilder.create().build();
        assertEquals(100, filter.getLimit());
        assertEquals(0, filter.getOffset());
    }

    @Test
    public void clearTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        FindFilter filter1 = builder.build();
        FindFilter filter2 = builder.clear().build();
        assertNotSame(filter1, filter2);
    }

    @Test
    public void toStringPositiveTest() throws StorageClientException {
        String string = FindFilterBuilder.create()
                .limitAndOffset(1, 2)
                .keyIn(Arrays.asList("3", "4"))
                .key2In(Arrays.asList("5", "6"))
                .key3In(Arrays.asList("7", "8"))
                .profileKeyIn(Arrays.asList("9", "10"))
                .versionNotIn(Arrays.asList("11", "12"))
                .rangeKeyIn(new int[]{13, 14})
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
    public void negativeTestIllegalArgs() {
        //key
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyEq(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyIn(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().keyIn(new ArrayList<>()));
        //key2
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key2Eq(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key2In(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key2In(new ArrayList<>()));
        //key3
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key3Eq(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key3In(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().key3In(new ArrayList<>()));
        //version
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionEq(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionIn(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionIn(new ArrayList<>()));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionNotEq(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionNotIn(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().versionNotIn(new ArrayList<>()));
        //profileKey
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().profileKeyEq(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().profileKeyIn(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().profileKeyIn(new ArrayList<>()));
        //rangeKey
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyIn(null));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyIn(new int[]{}));
        //limit & offset
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().limitAndOffset(-1, 0));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().limitAndOffset(1, -1));
        assertThrows(StorageClientException.class, () -> FindFilterBuilder.create().rangeKeyBetween(1, -1));
    }

    @Test
    public void positiveKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.keyEq("1").build().getKeyFilter().getValue().get(0));
        assertEquals("2", builder.keyIn(Arrays.asList("1", "2")).build().getKeyFilter().getValue().get(1));
        assertFalse(builder.keyEq("4").build().getKeyFilter().isNotCondition());
        assertFalse(builder.keyIn(Arrays.asList("7", "8")).build().getKeyFilter().isNotCondition());
    }

    @Test
    public void positiveKey2Test() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.key2Eq("1").build().getKey2Filter().getValue().get(0));
        assertEquals("2", builder.key2In(Arrays.asList("1", "2")).build().getKey2Filter().getValue().get(1));
        assertFalse(builder.key2Eq("4").build().getKey2Filter().isNotCondition());
        assertFalse(builder.key2In(Arrays.asList("7", "8")).build().getKey2Filter().isNotCondition());
    }

    @Test
    public void positiveKey3Test() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.key3Eq("1").build().getKey3Filter().getValue().get(0));
        assertEquals("2", builder.key3In(Arrays.asList("1", "2")).build().getKey3Filter().getValue().get(1));
        assertFalse(builder.key3Eq("4").build().getKey3Filter().isNotCondition());
        assertFalse(builder.key3In(Arrays.asList("7", "8")).build().getKey3Filter().isNotCondition());
    }

    @Test
    public void positiveProfileTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.profileKeyEq("1").build().getProfileKeyFilter().getValue().get(0));
        assertEquals("2", builder.profileKeyIn(Arrays.asList("1", "2")).build().getProfileKeyFilter().getValue().get(1));
        assertFalse(builder.profileKeyEq("4").build().getProfileKeyFilter().isNotCondition());
        assertFalse(builder.profileKeyIn(Arrays.asList("7", "8")).build().getProfileKeyFilter().isNotCondition());
    }

    @Test
    public void positiveVersionTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals("1", builder.versionEq("1").build().getVersionFilter().getValue().get(0));
        assertEquals("2", builder.versionIn(Arrays.asList("1", "2")).build().getVersionFilter().getValue().get(1));
        assertEquals("3", builder.versionNotEq("3").build().getVersionFilter().getValue().get(0));
        assertTrue(builder.versionNotEq("4").build().getVersionFilter().isNotCondition());
        assertEquals("6", builder.versionNotIn(Arrays.asList("5", "6")).build().getVersionFilter().getValue().get(1));
        assertTrue(builder.versionNotIn(Arrays.asList("7", "8")).build().getVersionFilter().isNotCondition());
    }

    @Test
    public void positiveRangeKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertEquals(1, builder.rangeKeyEq(1).build().getRangeKeyFilter().getValues()[0]);
        assertEquals(2, builder.rangeKeyIn(new int[]{1, 2}).build().getRangeKeyFilter().getValues()[1]);

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
