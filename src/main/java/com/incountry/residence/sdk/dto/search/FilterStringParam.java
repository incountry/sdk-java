package com.incountry.residence.sdk.dto.search;

import java.util.ArrayList;
import java.util.List;

public class FilterStringParam {

    private List<String> value;
    private boolean notCondition;

    public FilterStringParam(List<String> values) {
        this(values, false);
    }

    public FilterStringParam(String filterValue) {
        this(filterValue, false);
    }

    public FilterStringParam(String filterValue, boolean notConditionValue) {
        if (filterValue == null) {
            throw new IllegalArgumentException("FilterStringParam value can't be null");
        }
        this.value = new ArrayList<>();
        value.add(filterValue);
        notCondition = notConditionValue;
    }

    public FilterStringParam(List<String> values, boolean notConditionValue) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("FilterStringParam values can't be null");
        }
        this.value = values;
        this.notCondition = notConditionValue;
    }

    public List<String> getValue() {
        return value;
    }

    public boolean isNotCondition() {
        return notCondition;
    }

    public FilterStringParam copy() {
        FilterStringParam clone = new FilterStringParam(new ArrayList<>(value));
        clone.notCondition = notCondition;
        return clone;
    }

    @Override
    public String toString() {
        return "FilterStringParam{" +
                "value=" + value +
                ", notCondition=" + notCondition +
                '}';
    }
}
