package com.incountry.residence.sdk.dto.search.internal;

import java.util.HashMap;
import java.util.Map;

public class NullFilter extends Filter {

    private final boolean nullable;

    public NullFilter(boolean isNull) {
        nullable = isNull;
    }

    @Override
    public Object toTransferObject() {
        if (nullable) {
            return null;
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put(OPERATOR_NOT, null);
            return result;
        }
    }
}
