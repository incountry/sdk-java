package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterStringParam {

    private static final Logger LOG = LogManager.getLogger(FilterStringParam.class);
    private static final String MSG_NULL_FILTERS = "FilterStringParam values can't be null";

    private List<String> values;
    private boolean notCondition;

    public FilterStringParam(String[] values) throws StorageClientException {
        this(values, false);
    }

    public FilterStringParam(String[] values, boolean notConditionValue) throws StorageClientException {
        if (values == null || values.length == 0
                || (values.length == 1 && (values[0] == null || values[0].isEmpty()))) {
            LOG.error(MSG_NULL_FILTERS);
            throw new StorageClientException(MSG_NULL_FILTERS);
        }
        this.values = new ArrayList<>(Arrays.asList(values));
        this.notCondition = notConditionValue;
    }

    private FilterStringParam(List<String> values, boolean notConditionValue) throws StorageClientException {
        if (values == null || values.isEmpty()
                || (values.size() == 1 && (values.get(0) == null || values.get(0).isEmpty()))) {
            LOG.error(MSG_NULL_FILTERS);
            throw new StorageClientException(MSG_NULL_FILTERS);
        }
        this.values = values;
        this.notCondition = notConditionValue;
    }

    public List<String> getValues() {
        if (values != null && !values.isEmpty()) {
            return new ArrayList<>(values);
        }
        return new ArrayList<>();
    }

    public boolean isNotCondition() {
        return notCondition;
    }

    public FilterStringParam copy() throws StorageClientException {
        return new FilterStringParam(getValues(), notCondition);
    }

    @Override
    public String toString() {
        return "FilterStringParam{" +
                "value=" + values +
                ", notCondition=" + notCondition +
                '}';
    }
}
