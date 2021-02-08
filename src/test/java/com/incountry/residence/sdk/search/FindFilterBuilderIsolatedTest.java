package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.RecordField;
import com.incountry.residence.sdk.dto.search.SortOrder;
import com.incountry.residence.sdk.dto.search.internal.FilterNumberParam;
import com.incountry.residence.sdk.dto.search.internal.FilterStringParam;
import com.incountry.residence.sdk.dto.search.internal.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.SortField;
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
            if (field.equals(StringField.SEARCH_KEYS)) {
                continue;
            }
            FilterStringParam param = (FilterStringParam) builder.keyEq(field, "1").build().getFilterMap().get(field);
            assertEquals("1", param.getValues().get(0));
            param = (FilterStringParam) builder.keyEq(field, "1", "2").build().getFilterMap().get(field);
            assertEquals("2", param.getValues().get(1));
            param = (FilterStringParam) builder.keyEq(field, "4").build().getFilterMap().get(field);
            assertFalse(param.isNotCondition());
            param = (FilterStringParam) builder.keyNotEq(field, "5", "6").build().getFilterMap().get(field);
            assertEquals("6", param.getValues().get(1));
            param = (FilterStringParam) builder.keyNotEq(field, "7", "8").build().getFilterMap().get(field);
            assertTrue(param.isNotCondition());
            param = (FilterStringParam) builder.keyEq(field, "9", "10").build().getFilterMap().get(field);
            assertFalse(param.isNotCondition());
        }
    }

    @Test
    void positiveRangeKeyTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create();
        for (NumberField field : NumberField.values()) {
            FilterNumberParam param = (FilterNumberParam) builder.keyEq(field, 1L).build().getFilterMap().get(field);
            assertEquals(1L, param.getValues()[0]);
            param = (FilterNumberParam) builder.keyEq(field, 1L, 2L).build().getFilterMap().get(field);
            assertEquals(2L, param.getValues()[1]);
            param = (FilterNumberParam) builder.keyGT(field, 9L).build().getFilterMap().get(field);
            assertEquals(9L, param.getValues()[0]);
            param = (FilterNumberParam) builder.keyGT(field, 10L).build().getFilterMap().get(field);
            assertEquals(FindFilterBuilder.OPER_GT, param.getOperator1());
            param = (FilterNumberParam) builder.keyGTE(field, 11L).build().getFilterMap().get(field);
            assertEquals(11L, param.getValues()[0]);
            param = (FilterNumberParam) builder.keyGTE(field, 12L).build().getFilterMap().get(field);
            assertEquals(FindFilterBuilder.OPER_GTE, param.getOperator1());
            param = (FilterNumberParam) builder.keyLT(field, 13L).build().getFilterMap().get(field);
            assertEquals(13L, param.getValues()[0]);
            param = (FilterNumberParam) builder.keyLT(field, 14L).build().getFilterMap().get(field);
            assertEquals(FindFilterBuilder.OPER_LT, param.getOperator1());
            param = (FilterNumberParam) builder.keyLTE(field, 15L).build().getFilterMap().get(field);
            assertEquals(15L, param.getValues()[0]);
            param = (FilterNumberParam) builder.keyLTE(field, 16L).build().getFilterMap().get(field);
            assertEquals(FindFilterBuilder.OPER_LTE, param.getOperator1());
            builder.keyBetween(field, 17L, 18L);
            param = (FilterNumberParam) builder.build().getFilterMap().get(field);
            assertEquals(FindFilterBuilder.OPER_GTE, param.getOperator1());
            param = (FilterNumberParam) builder.build().getFilterMap().get(field);
            assertEquals(FindFilterBuilder.OPER_LTE, param.getOperator2());
            param = (FilterNumberParam) builder.build().getFilterMap().get(field);
            assertEquals(17L, param.getValues()[0]);
            param = (FilterNumberParam) builder.build().getFilterMap().get(field);
            assertEquals(18L, param.getValues()[1]);
            builder.keyBetween(field, 19L, true, 20L, false);
            param = (FilterNumberParam) builder.build().getFilterMap().get(field);
            assertEquals(FindFilterBuilder.OPER_GTE, param.getOperator1());
            param = (FilterNumberParam) builder.build().getFilterMap().get(field);
            assertEquals(FindFilterBuilder.OPER_LT, param.getOperator2());
            param = (FilterNumberParam) builder.build().getFilterMap().get(field);
            assertEquals(19L, param.getValues()[0]);
            param = (FilterNumberParam) builder.build().getFilterMap().get(field);
            assertEquals(20L, param.getValues()[1]);
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
                .keyEq(StringField.KEY11, "19", "20")
                .keyEq(StringField.KEY12, "21", "22")
                .keyEq(StringField.KEY13, "23", "24")
                .keyEq(StringField.KEY14, "25", "26")
                .keyEq(StringField.KEY15, "27", "28")
                .keyEq(StringField.KEY16, "29", "30")
                .keyEq(StringField.KEY17, "31", "32")
                .keyEq(StringField.KEY18, "33", "34")
                .keyEq(StringField.KEY19, "35", "36")
                .keyEq(StringField.KEY20, "37", "38")
                .keyEq(StringField.SERVICE_KEY1, "39", "40")
                .keyEq(StringField.SERVICE_KEY2, "41", "42")
                .keyEq(StringField.PARENT_KEY, "43", "44")
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
        assertTrue(jsonFilter.contains("\"key11\":["));
        assertTrue(jsonFilter.contains("\"key12\":["));
        assertTrue(jsonFilter.contains("\"key13\":["));
        assertTrue(jsonFilter.contains("\"key14\":["));
        assertTrue(jsonFilter.contains("\"key15\":["));
        assertTrue(jsonFilter.contains("\"key16\":["));
        assertTrue(jsonFilter.contains("\"key17\":["));
        assertTrue(jsonFilter.contains("\"key18\":["));
        assertTrue(jsonFilter.contains("\"key19\":["));
        assertTrue(jsonFilter.contains("\"key20\":["));
        assertTrue(jsonFilter.contains("\"parent_key\":["));
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
        Map<RecordField, Object> filterMap = filter.getFilterMap();
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
        Map<RecordField, Object> filterMap = filter.getFilterMap();
        assertEquals("FilterStringParam{value=[1a, 2b], notCondition=false}", filterMap.get(StringField.RECORD_KEY).toString());
        assertEquals("FilterStringParam{value=[3c, 4d], notCondition=false}", filterMap.get(StringField.KEY1).toString());
        assertEquals("FilterStringParam{value=[5e, 6f], notCondition=false}", filterMap.get(StringField.KEY4).toString());
        assertEquals("FilterStringParam{value=[7g, 8h], notCondition=false}", filterMap.get(StringField.KEY5).toString());
        assertEquals("FilterStringParam{value=[9i, 10j], notCondition=false}", filterMap.get(StringField.KEY6).toString());
        assertEquals("FilterStringParam{value=[11k, 12l], notCondition=false}", filterMap.get(StringField.KEY7).toString());
        assertEquals("FilterStringParam{value=[13m, 14n], notCondition=false}", filterMap.get(StringField.KEY8).toString());
        assertEquals("FilterStringParam{value=[15o, 16p], notCondition=false}", filterMap.get(StringField.KEY9).toString());
        assertEquals("FilterStringParam{value=[17q, 18r], notCondition=false}", filterMap.get(StringField.KEY10).toString());

        assertEquals("FilterStringParam{value=[19, 20], notCondition=false}", filterMap.get(StringField.KEY11).toString());
        assertEquals("FilterStringParam{value=[21, 22], notCondition=false}", filterMap.get(StringField.KEY12).toString());
        assertEquals("FilterStringParam{value=[23, 24], notCondition=false}", filterMap.get(StringField.KEY13).toString());
        assertEquals("FilterStringParam{value=[25, 26], notCondition=false}", filterMap.get(StringField.KEY14).toString());
        assertEquals("FilterStringParam{value=[27, 28], notCondition=false}", filterMap.get(StringField.KEY15).toString());
        assertEquals("FilterStringParam{value=[29, 30], notCondition=false}", filterMap.get(StringField.KEY16).toString());
        assertEquals("FilterStringParam{value=[31, 32], notCondition=false}", filterMap.get(StringField.KEY17).toString());
        assertEquals("FilterStringParam{value=[33, 34], notCondition=false}", filterMap.get(StringField.KEY18).toString());
        assertEquals("FilterStringParam{value=[35, 36], notCondition=false}", filterMap.get(StringField.KEY19).toString());
        assertEquals("FilterStringParam{value=[37, 38], notCondition=false}", filterMap.get(StringField.KEY20).toString());

        assertEquals("FilterStringParam{value=[39, 40], notCondition=false}", filterMap.get(StringField.SERVICE_KEY1).toString());
        assertEquals("FilterStringParam{value=[41, 42], notCondition=false}", filterMap.get(StringField.SERVICE_KEY2).toString());
        assertEquals("FilterStringParam{value=[43, 44], notCondition=false}", filterMap.get(StringField.PARENT_KEY).toString());
    }

    @Test
    void sortingTest() throws StorageClientException {
        FindFilter filter = FindFilterBuilder.create()
                .keyEq(StringField.KEY1, "<RecordKey>")
                .sortBy(SortField.KEY1, SortOrder.DESC)
                .sortBy(SortField.KEY2, SortOrder.DESC)
                .sortBy(SortField.KEY3, SortOrder.DESC)
                .sortBy(SortField.KEY4, SortOrder.DESC)
                .sortBy(SortField.KEY5, SortOrder.DESC)
                .sortBy(SortField.KEY6, SortOrder.DESC)
                .sortBy(SortField.KEY7, SortOrder.DESC)
                .sortBy(SortField.KEY8, SortOrder.DESC)
                .sortBy(SortField.KEY9, SortOrder.DESC)
                .sortBy(SortField.KEY10, SortOrder.DESC)
                .sortBy(SortField.KEY11, SortOrder.DESC)
                .sortBy(SortField.KEY12, SortOrder.DESC)
                .sortBy(SortField.KEY13, SortOrder.DESC)
                .sortBy(SortField.KEY14, SortOrder.DESC)
                .sortBy(SortField.KEY15, SortOrder.DESC)
                .sortBy(SortField.KEY16, SortOrder.DESC)
                .sortBy(SortField.KEY17, SortOrder.DESC)
                .sortBy(SortField.KEY18, SortOrder.DESC)
                .sortBy(SortField.KEY19, SortOrder.DESC)
                .sortBy(SortField.KEY20, SortOrder.DESC)
                .sortBy(SortField.RANGE_KEY1, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY2, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY3, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY4, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY5, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY6, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY7, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY8, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY9, SortOrder.ASC)
                .sortBy(SortField.RANGE_KEY10, SortOrder.ASC)
                .sortBy(SortField.CREATED_AT, SortOrder.ASC)
                .sortBy(SortField.UPDATED_AT, SortOrder.ASC)
                .build();
        String jsonFilter = JsonUtils.toJsonString(filter, new CryptoManager(null, "<envId>", null, false, true));
        assertEquals("{\"filter\":{\"key1\":[\"b99fc3a4b5365f543fbb39af2fddead40edcb3e72368df475c8c1385549968b1\"]}," +
                        "\"options\":{\"sort\":[" +
                        "{\"key1\":\"desc\"},{\"key2\":\"desc\"},{\"key3\":\"desc\"},{\"key4\":\"desc\"},{\"key5\":\"desc\"}," +
                        "{\"key6\":\"desc\"},{\"key7\":\"desc\"},{\"key8\":\"desc\"},{\"key9\":\"desc\"},{\"key10\":\"desc\"}," +
                        "{\"key11\":\"desc\"},{\"key12\":\"desc\"},{\"key13\":\"desc\"},{\"key14\":\"desc\"},{\"key15\":\"desc\"}," +
                        "{\"key16\":\"desc\"},{\"key17\":\"desc\"},{\"key18\":\"desc\"},{\"key19\":\"desc\"},{\"key20\":\"desc\"}," +
                        "{\"range_key1\":\"asc\"},{\"range_key2\":\"asc\"},{\"range_key3\":\"asc\"},{\"range_key4\":\"asc\"},{\"range_key5\":\"asc\"}," +
                        "{\"range_key6\":\"asc\"},{\"range_key7\":\"asc\"},{\"range_key8\":\"asc\"},{\"range_key9\":\"asc\"},{\"range_key10\":\"asc\"}," +
                        "{\"created_at\":\"asc\"},{\"updated_at\":\"asc\"}],\"limit\":100,\"offset\":0}}",
                jsonFilter);
    }

    @Test
    void sortingNegativeTest() throws StorageClientException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .sortBy(SortField.KEY1, SortOrder.ASC);
        StorageClientException ex = assertThrows(StorageClientException.class, () ->
                builder.sortBy(SortField.KEY1, SortOrder.ASC));
        assertEquals("Field KEY1 is already in sorting list", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () ->
                builder.sortBy(null, SortOrder.ASC));
        assertEquals("Sorting field is null", ex.getMessage());
    }

    @Test
    void nullFilterTest() throws StorageClientException {
        FindFilter filter = FindFilterBuilder.create()
                .keyIsNotNull(NumberField.RANGE_KEY1)
                .keyIsNotNull(StringField.KEY1)
                .keyIsNull(NumberField.RANGE_KEY2)
                .keyIsNull(StringField.KEY2)
                .build();
        String jsonFilter = JsonUtils.toJsonString(filter, new CryptoManager(null, "<envId>", null, false, true));
        assertTrue(jsonFilter.contains("\"range_key1\":{\"$not\":null}"));
        assertTrue(jsonFilter.contains("\"key1\":{\"$not\":null}"));
        assertTrue(jsonFilter.contains("\"range_key2\":null"));
        assertTrue(jsonFilter.contains("\"key2\":null"));
    }
}
