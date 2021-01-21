package com.incountry.residence.sdk.dto.search.internal;

public class NullFilter {

    private final boolean nullable;

    public NullFilter(boolean isNull) {
        nullable = isNull;
    }

    public boolean isNullable() {
        return nullable;
    }
}
