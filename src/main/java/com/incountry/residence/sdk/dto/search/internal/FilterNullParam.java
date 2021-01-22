package com.incountry.residence.sdk.dto.search.internal;

public class FilterNullParam {

    private final boolean nullable;

    public FilterNullParam(boolean isNull) {
        nullable = isNull;
    }

    public boolean isNullable() {
        return nullable;
    }
}
