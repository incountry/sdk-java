package com.incountry.residence.sdk;

import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_GT;
import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_LT;

import com.incountry.residence.sdk.dto.search.FilterNumberParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilterNumberParamTest {

    @Test
    public void negativeRangeTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FilterNumberParam(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new FilterNumberParam(null, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new FilterNumberParam("WrongOperator!@#", 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new FilterNumberParam(null, 0, null, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new FilterNumberParam(OPER_GT, 0, null, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new FilterNumberParam(OPER_GT, 0, OPER_GT, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new FilterNumberParam(OPER_LT, 0, OPER_LT, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new FilterNumberParam(OPER_GT, 100, OPER_LT, 0);
        });
    }
}
