package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FilterStringParam {

    private static final Logger LOG = LogManager.getLogger(FilterStringParam.class);
    private static final String MSG_NULL_FILTERS = "FilterStringParam values can't be null";

    private final List<String> values;
    private final boolean notCondition;

    public FilterStringParam(String[] values) throws StorageClientException {
        this(values, false);
    }

    public FilterStringParam(String[] values, boolean notConditionValue) throws StorageClientException {
        if (values == null || values.length == 0 || Stream.of(values).anyMatch(Objects::isNull)) {
            LOG.error(MSG_NULL_FILTERS);
            throw new StorageClientException(MSG_NULL_FILTERS);
        }
        this.values = new ArrayList<>(Arrays.asList(values));
        this.notCondition = notConditionValue;
    }

    private FilterStringParam(List<String> values, boolean notConditionValue) {
        this.values = values;
        this.notCondition = notConditionValue;
    }

    public List<String> getValues() {
        if (values != null) {
            return new ArrayList<>(values);
        }
        return new ArrayList<>();
    }

    public boolean isNotCondition() {
        return notCondition;
    }

    public FilterStringParam copy() {
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
