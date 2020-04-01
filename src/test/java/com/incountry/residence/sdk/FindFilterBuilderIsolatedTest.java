package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FindFilterBuilderIsolatedTest {

    @Test
    public void defaultPositiveTest() {
        FindFilter filter = FindFilterBuilder.create().build();
        assertEquals(100, filter.getLimit());
        assertEquals(0, filter.getOffset());
    }

    @Test
    public void clearTest() {
        FindFilterBuilder builder = FindFilterBuilder.create();
        FindFilter filter1 = builder.build();
        FindFilter filter2 = builder.clear().build();
        assertFalse(filter1 == filter2);
    }

    @Test
    public void toStrirngPositiveTest() {
        String string = FindFilterBuilder.create()
                .limitAndOffset(1, 2)
                .keyNotIn(Arrays.asList("3", "4"))
                .key2NotIn(Arrays.asList("5", "6"))
                .key3NotIn(Arrays.asList("7", "8"))
                .profileKeyNotIn(Arrays.asList("9", "10"))
                .versionNotIn(Arrays.asList("11", "12"))
                .rangeKeyNotIn(new int[]{13, 14})
                .build().toString();
        assertNotNull(string);
        assertTrue(string.contains("limit=1"));
        assertTrue(string.contains("offset=2"));
        assertTrue(string.contains("[3, 4]"));
        assertTrue(string.contains("[5, 6]"));
        assertTrue(string.contains("[7, 8]"));
        assertTrue(string.contains("[9, 10]"));
        assertTrue(string.contains("[11, 12]"));
        assertTrue(string.contains("[13, 14]"));
    }


    @Test
    public void negativeTestIllegalArgs() {
        //key
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().keyEq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().keyIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().keyIn(new ArrayList<>());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().keyNotEq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().keyNotIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().keyNotIn(new ArrayList<>());
        });
        //key2
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key2Eq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key2In(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key2In(new ArrayList<>());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key2NotEq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key2NotIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key2NotIn(new ArrayList<>());
        });
        //key3
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key3Eq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key3In(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key3In(new ArrayList<>());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key3NotEq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key3NotIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().key3NotIn(new ArrayList<>());
        });
        //version
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().versionEq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().versionIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().versionIn(new ArrayList<>());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().versionNotEq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().versionNotIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().versionNotIn(new ArrayList<>());
        });
        //profileKey
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().profileKeyEq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().profileKeyIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().profileKeyIn(new ArrayList<>());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().profileKeyNotEq(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().profileKeyNotIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().profileKeyNotIn(new ArrayList<>());
        });
        //rangeKey
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().rangeKeyIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().rangeKeyIn(new int[]{});
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().rangeKeyNotIn(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().rangeKeyNotIn(new int[]{});
        });

        //limit & offset
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().limitAndOffset(-1, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            FindFilterBuilder.create().limitAndOffset(1, -1);
        });
    }

    @Test
    public void positiveKeyTest() {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertTrue(builder.keyEq("1").build().getKeyFilter().getValue().get(0).equals("1"));
        assertTrue(builder.keyIn(Arrays.asList("1", "2")).build().getKeyFilter().getValue().get(1).equals("2"));
        assertTrue(builder.keyNotEq("3").build().getKeyFilter().getValue().get(0).equals("3"));
        assertTrue(builder.keyNotEq("4").build().getKeyFilter().isNotCondition());
        assertTrue(builder.keyNotIn(Arrays.asList("5", "6")).build().getKeyFilter().getValue().get(1).equals("6"));
        assertTrue(builder.keyNotIn(Arrays.asList("7", "8")).build().getKeyFilter().isNotCondition());
    }

    @Test
    public void positiveKey2Test() {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertTrue(builder.key2Eq("1").build().getKey2Filter().getValue().get(0).equals("1"));
        assertTrue(builder.key2In(Arrays.asList("1", "2")).build().getKey2Filter().getValue().get(1).equals("2"));
        assertTrue(builder.key2NotEq("3").build().getKey2Filter().getValue().get(0).equals("3"));
        assertTrue(builder.key2NotEq("4").build().getKey2Filter().isNotCondition());
        assertTrue(builder.key2NotIn(Arrays.asList("5", "6")).build().getKey2Filter().getValue().get(1).equals("6"));
        assertTrue(builder.key2NotIn(Arrays.asList("7", "8")).build().getKey2Filter().isNotCondition());
    }

    @Test
    public void positiveKey3Test() {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertTrue(builder.key3Eq("1").build().getKey3Filter().getValue().get(0).equals("1"));
        assertTrue(builder.key3In(Arrays.asList("1", "2")).build().getKey3Filter().getValue().get(1).equals("2"));
        assertTrue(builder.key3NotEq("3").build().getKey3Filter().getValue().get(0).equals("3"));
        assertTrue(builder.key3NotEq("4").build().getKey3Filter().isNotCondition());
        assertTrue(builder.key3NotIn(Arrays.asList("5", "6")).build().getKey3Filter().getValue().get(1).equals("6"));
        assertTrue(builder.key3NotIn(Arrays.asList("7", "8")).build().getKey3Filter().isNotCondition());
    }

    @Test
    public void positiveProfileTest() {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertTrue(builder.profileKeyEq("1").build().getProfileKeyFilter().getValue().get(0).equals("1"));
        assertTrue(builder.profileKeyIn(Arrays.asList("1", "2")).build().getProfileKeyFilter().getValue().get(1).equals("2"));
        assertTrue(builder.profileKeyNotEq("3").build().getProfileKeyFilter().getValue().get(0).equals("3"));
        assertTrue(builder.profileKeyNotEq("4").build().getProfileKeyFilter().isNotCondition());
        assertTrue(builder.profileKeyNotIn(Arrays.asList("5", "6")).build().getProfileKeyFilter().getValue().get(1).equals("6"));
        assertTrue(builder.profileKeyNotIn(Arrays.asList("7", "8")).build().getProfileKeyFilter().isNotCondition());
    }

    @Test
    public void positiveVersionTest() {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertTrue(builder.versionEq("1").build().getVersionFilter().getValue().get(0).equals("1"));
        assertTrue(builder.versionIn(Arrays.asList("1", "2")).build().getVersionFilter().getValue().get(1).equals("2"));
        assertTrue(builder.versionNotEq("3").build().getVersionFilter().getValue().get(0).equals("3"));
        assertTrue(builder.versionNotEq("4").build().getVersionFilter().isNotCondition());
        assertTrue(builder.versionNotIn(Arrays.asList("5", "6")).build().getVersionFilter().getValue().get(1).equals("6"));
        assertTrue(builder.versionNotIn(Arrays.asList("7", "8")).build().getVersionFilter().isNotCondition());
    }

    @Test
    public void positiveRangeKeyTest() {
        FindFilterBuilder builder = FindFilterBuilder.create();
        assertTrue(builder.rangeKeyEq(1).build().getRangeKeyFilter().getValues()[0] == 1);
        assertTrue(builder.rangeKeyIn(new int[]{1, 2}).build().getRangeKeyFilter().getValues()[1] == 2);
        assertTrue(builder.rangeKeyNotEq(3).build().getRangeKeyFilter().getValue() == 3);
        assertTrue(builder.rangeKeyNotEq(4).build().getRangeKeyFilter().getOperator().equals("not"));
        assertTrue(builder.rangeKeyNotIn(new int[]{5, 6}).build().getRangeKeyFilter().getValues()[1] == 6);
        assertTrue(builder.rangeKeyNotIn(new int[]{7, 8}).build().getRangeKeyFilter().getOperator().equals("not"));
        assertTrue(builder.rangeKeyGT(9).build().getRangeKeyFilter().getValue() == 9);
        assertTrue(builder.rangeKeyGT(10).build().getRangeKeyFilter().getOperator().equals("gt"));
        assertTrue(builder.rangeKeyGTE(11).build().getRangeKeyFilter().getValue() == 11);
        assertTrue(builder.rangeKeyGTE(12).build().getRangeKeyFilter().getOperator().equals("gte"));
        assertTrue(builder.rangeKeyLT(13).build().getRangeKeyFilter().getValue() == 13);
        assertTrue(builder.rangeKeyLT(14).build().getRangeKeyFilter().getOperator().equals("lt"));
        assertTrue(builder.rangeKeyLTE(15).build().getRangeKeyFilter().getValue() == 15);
        assertTrue(builder.rangeKeyLTE(16).build().getRangeKeyFilter().getOperator().equals("lte"));
    }
}
