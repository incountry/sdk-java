package com.incountry.residence.sdk.search;

import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_GT;
import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_LT;

import com.google.gson.Gson;
import com.incountry.residence.sdk.dto.search.FilterNumberParam;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_NOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterNumberParamTest {

    @Test
    void negativeRangeTest() {
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(null));
        assertEquals("FilterNumberParam values can't be null", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(null, 1));
        assertEquals("Operator in number filter can by only in [$lt,$lte,$gt,$gte,$not]", ex2.getMessage());
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> new FilterNumberParam("WrongOperator!@#", 1));
        assertEquals("Operator in number filter can by only in [$lt,$lte,$gt,$gte,$not]", ex3.getMessage());
        StorageClientException ex4 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(null, 0, null, 1));
        assertEquals("Operator1 in range filter can by only $gt or $gte", ex4.getMessage());
        StorageClientException ex5 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, null));
        assertEquals("FilterNumberParam values can't be null", ex5.getMessage());
        StorageClientException ex6 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, 0, null, 1));
        assertEquals("Operator2 in range filter can by only $lt or $lte", ex6.getMessage());
        StorageClientException ex7 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, 0, OPER_GT, 1));
        assertEquals("Operator2 in range filter can by only $lt or $lte", ex7.getMessage());
        StorageClientException ex8 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_LT, 0, OPER_LT, 1));
        assertEquals("Operator1 in range filter can by only $gt or $gte", ex8.getMessage());
        StorageClientException ex9 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, 100, OPER_LT, 0));
        assertEquals("Value1 in range filter can by only less or equals value2", ex9.getMessage());
        StorageClientException ex10 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, null, OPER_LT, 0));
        assertEquals("FilterNumberParam values can't be null", ex10.getMessage());
        StorageClientException ex11 = assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, 100, OPER_LT, null));
        assertEquals("FilterNumberParam values can't be null", ex11.getMessage());
    }

    @Test
    void isRangeTest() throws StorageClientException {
        assertFalse(new FilterNumberParam(new Integer[]{1, 2}).isRange());
        assertFalse(new FilterNumberParam(OPER_NOT, 1).isRange());
        assertTrue(new FilterNumberParam(OPER_GT, 100, OPER_LT, 200).isRange());
        String numberFilterJson = "{'operator2'='not'}";
        FilterNumberParam numberParam = new Gson().fromJson(numberFilterJson, FilterNumberParam.class);
        assertFalse(numberParam.isRange());
    }

    @Test
    void fromJsonEmptyFieldsNumberTest() {
        String numberFilterJson = "{}";
        FilterNumberParam numberParam = new Gson().fromJson(numberFilterJson, FilterNumberParam.class);
        Integer[] values = numberParam.getValues();
        assertEquals(0, values.length);
    }
}
