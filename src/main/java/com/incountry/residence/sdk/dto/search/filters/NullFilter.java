package com.incountry.residence.sdk.dto.search.filters;

import com.incountry.residence.sdk.dto.search.filters.Filter;

import java.util.HashMap;

public class NullFilter extends Filter {

    private boolean nullable;

    public NullFilter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public Object toTransferObject() {
        return nullable ? null : new HashMap<Object, Object>() {{put(OPERATOR_NOT, null);}};
    }
}
