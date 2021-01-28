package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.SortField;
import com.incountry.residence.sdk.dto.search.SortOrder;
import com.incountry.residence.sdk.dto.search.internal.SortingParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortingParamTest {

    @Test
    void toStringTest() {
        SortingParam param = new SortingParam(SortField.RANGE_KEY1, SortOrder.DESC);
        assertEquals("SortingParam{RANGE_KEY1, DESC}", param.toString());
        param = new SortingParam(SortField.KEY1, SortOrder.ASC);
        assertEquals("SortingParam{KEY1, ASC}", param.toString());
    }
}
