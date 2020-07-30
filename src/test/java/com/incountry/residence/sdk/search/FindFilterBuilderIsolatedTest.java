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
        String jsonFilter = JsonUtils.toJsonString(filter, new CryptoManager(null, "<envId>", null, false));
        //{"filter":{"record_key":["7dd05cb65e032bb0c4de5663b0d55a42dea08414018a6c4fe4475f968bd78137","92615295d3285663166fab9e7a9371035448579643d1fcd12c48572c39c3e455"],"key1":["81bc20701d75abe5e2b059d65f803d3641e0c173c3fb9829867a3eb22b9b622f","1b519097ca847d24259e588d86a0f6d268cbb017249bf8521deae3d71f791e51"],"key4":["c2d1c230bef60781359d6c5fb8fa0847679a1e91fcbf5d77f5d88f3fcfdbcbde","67646b3766bd7d3b1cbf450df481f8dc1d1f2ac10166f268592c08a9ddf69d7b"],"key5":["9321a570fc0d078fba0f439895c5fd093157f310ea1e79a10aefc0bb391bab6e","3b60c5a4393d6d42fe69f08b58d51d6abcc128092eba881c213154b2f03058b9"],"key6":["b6f275371c158ee94432a30647d2bc48e517da9695a14e90d68d4f7b363ec163","687993ee27af92578070125863a62f0037bdf62d6ecb6fe137e7850b1beb65e8"],"key7":["b8c5099007270e0343f4ad0ac77a31b10ab330f5327d44db2bbe535f086af9fb","721de6cde00687fd8b8ad6782d4ea1b60693a64dc3175daaf4968d116eb83e78"],"key8":["5f2f91cf413d1b3350225616e9eabff2f33deff745a1bd399120162872c87380","6ab4af624983f85ac6f59063d6827859a12114240223a485b0d5dd59848b1ad7"],"key9":["7dd14e0c0f2c1faac7e63bc1cc7bd8eaf97a21385d9b6e5677413173e43f653f","1eb9f681274de87a9b7afb621b13c2e36e9782cd1dc6f8953e9931fef7132295"],"key10":["6e3923281194da1d99b58ef24966b2d6a8dff8ebd906c671b8290412f15f7a10","858ba664a88a3dfb9bd30bda06624a50334827f4167616ebbababbdbea45f6e4"],"service_key1":["f19b00f28cc24c3b3639254f01792ba5c091cd2db9f9337949dcd6ec47a2e55f","5d63a8c865ee33b489a78d84021bccf9d22b5948e9f52e6953ee29f83ddf6490"],"service_key2":["7d93ce30c71ee3538a1497e0ef485de7f7cda7401617adeea77b21d9d8a00277","be3af632cdb4e7fcee4d6a7ff44ce603481c540c117e1424418abba770bb0859"],"range_key1":[23,24],"range_key2":[25,26],"range_key3":[27,28],"range_key4":[29,30],"range_key5":[31,32],"range_key6":[33,34],"range_key7":[35,36],"range_key8":[37,38],"range_key9":[39,40],"range_key10":[41,42]},"options":{"limit":100,"offset":0}}
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
