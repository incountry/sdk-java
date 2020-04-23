package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class FilterStringParam {

    private static final Logger LOG = LogManager.getLogger(FilterStringParam.class);
    private static final String MSG_NULL_FILTER = "FilterStringParam value can't be null";
    private static final String MSG_NULL_FILTERS = "FilterStringParam values can't be null";

    private List<String> value;
    private boolean notCondition;

    public FilterStringParam(List<String> values) throws StorageClientException {
        this(values, false);
    }

    public FilterStringParam(String filterValue) throws StorageClientException {
        this(filterValue, false);
    }

    public FilterStringParam(String filterValue, boolean notConditionValue) throws StorageClientException {
        if (filterValue == null) {
            LOG.error(MSG_NULL_FILTER);
            throw new StorageClientException(MSG_NULL_FILTER);
        }
        this.value = new ArrayList<>();
        value.add(filterValue);
        notCondition = notConditionValue;
    }

    public FilterStringParam(List<String> values, boolean notConditionValue) throws StorageClientException {
        if (values == null || values.isEmpty()) {
            LOG.error(MSG_NULL_FILTERS);
            throw new StorageClientException(MSG_NULL_FILTERS);
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

    public FilterStringParam copy() throws StorageClientException {
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
