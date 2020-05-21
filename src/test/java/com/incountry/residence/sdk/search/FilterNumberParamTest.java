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
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(null));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(null, 1));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam("WrongOperator!@#", 1));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(null, 0, null, 1));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, null));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, 0, null, 1));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, 0, OPER_GT, 1));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_LT, 0, OPER_LT, 1));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, 100, OPER_LT, 0));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, null, OPER_LT, 0));
        assertThrows(StorageClientException.class, () -> new FilterNumberParam(OPER_GT, 100, OPER_LT, null));
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
