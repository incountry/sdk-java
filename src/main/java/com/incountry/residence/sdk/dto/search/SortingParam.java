package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.dto.search.fields.SortField;

public class SortingParam {

    public SortField field;
    public SortOrder order;

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
}
