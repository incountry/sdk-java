package com.incountry.residence.sdk.dto.search.filters;

import java.util.HashMap;
import java.util.Map;

public class NullFilter extends Filter {

    private boolean nullable;

    public NullFilter(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public Object toTransferObject() {
        if (!nullable) {
            Map<Object, Object> obj = new HashMap<>();
            obj.put(OPERATOR_NOT, null);
            return obj;
        }
        return null;
    }
}
