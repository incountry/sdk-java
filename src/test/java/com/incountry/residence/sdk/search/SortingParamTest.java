package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.SortingField;
import com.incountry.residence.sdk.dto.search.internal.SortingParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortingParamTest {

    @Test
    void toStringTest() {
        SortingParam param = new SortingParam(SortingField.RANGE_KEY1, true);
        assertEquals("SortingParam{RANGE_KEY1, DESC}", param.toString());
        param = new SortingParam(SortingField.KEY1, false);
        assertEquals("SortingParam{KEY1, ASC}", param.toString());
    }
}
