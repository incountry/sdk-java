package com.incountry.storage.sdk.dto.search;

import java.util.ArrayList;
import java.util.List;

public class FilterStringParam {
    private List<String> value;
    private boolean notCondition;

    public FilterStringParam(List<String> value) {
        this.value = value;
    }

    public FilterStringParam(String filterValue) {
        this.value = new ArrayList<>();
        if (filterValue != null) {
            value.add(filterValue);
            notCondition = false;
        }
    }

    public FilterStringParam(String filterValue, boolean notConditionValue) {
        this.value = new ArrayList<>();
        if (filterValue != null) {
            value.add(filterValue);
            notCondition = notConditionValue;
        }
    }

    public List<String> getValue() {
        return value;
    }

    public boolean isNotCondition() {
        return notCondition;
    }

    @Override
    public String toString() {
        return "FilterStringParam{" +
                "value=" + value +
                ", notCondition=" + notCondition +
                '}';
    }
}
