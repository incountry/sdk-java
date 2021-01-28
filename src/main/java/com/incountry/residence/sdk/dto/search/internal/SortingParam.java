package com.incountry.residence.sdk.dto.search.internal;

import com.incountry.residence.sdk.dto.search.SortField;
import com.incountry.residence.sdk.dto.search.SortOrder;

public class SortingParam {
    private final SortField field;
    private final SortOrder order;

    public SortingParam(SortField field, SortOrder order) {
        this.field = field;
        this.order = order;
    }

    public SortField getField() {
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
