package com.incountry.residence.sdk.dto.search.internal;

import com.incountry.residence.sdk.dto.search.SortFields;
import com.incountry.residence.sdk.dto.search.SortOrder;

public class SortingParam {
    private final SortFields field;
    private final SortOrder order;

    public SortingParam(SortFields field, SortOrder order) {
        this.field = field;
        this.order = order;
    }

    public SortFields getField() {
        return field;
    }

    public SortOrder getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "SortingParam{" + field + ", " + order + '}';
    }
}
