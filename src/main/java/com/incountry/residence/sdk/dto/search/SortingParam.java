package com.incountry.residence.sdk.dto.search;

public class SortingParam {
    private final SortingField field;
    private final boolean desc;

    public SortingParam(SortingField field, boolean desc) {
        this.field = field;
        this.desc = desc;
    }

    public SortingField getField() {
        return field;
    }

    public boolean isDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "SortingParam{" + field + ", " + (desc ? "DESC" : "ASC") + '}';
    }
}
