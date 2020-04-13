package com.incountry.residence.sdk.dto.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FilterStringParam {

    private static final Logger LOG = LoggerFactory.getLogger(FilterStringParam.class);
    private static final String MSG_NULL_FILTER = "FilterStringParam value can't be null";
    private static final String MSG_NULL_FILTERS = "FilterStringParam values can't be null";

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
            LOG.error(MSG_NULL_FILTER);
            throw new IllegalArgumentException(MSG_NULL_FILTER);
        }
        this.value = new ArrayList<>();
        value.add(filterValue);
        notCondition = notConditionValue;
    }

    public FilterStringParam(List<String> values, boolean notConditionValue) {
        if (values == null || values.isEmpty()) {
            LOG.error(MSG_NULL_FILTERS);
            throw new IllegalArgumentException(MSG_NULL_FILTERS);
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
