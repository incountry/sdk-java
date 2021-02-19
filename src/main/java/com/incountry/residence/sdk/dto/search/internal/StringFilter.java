package com.incountry.residence.sdk.dto.search.internal;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class StringFilter extends Filter {

    private static final Logger LOG = LogManager.getLogger(StringFilter.class);
    private static final String MSG_NULL_FILTERS = "StringFilter values can't be null";

    private final List<String> values;
    private final boolean notCondition;

    public StringFilter(String[] values) throws StorageClientException {
        this(values, false);
    }

    public StringFilter(String[] values, boolean notConditionValue) throws StorageClientException {
        if (values == null || values.length == 0 || Stream.of(values).anyMatch(Objects::isNull)) {
            LOG.error(MSG_NULL_FILTERS);
            throw new StorageClientException(MSG_NULL_FILTERS);
        }
        this.values = new ArrayList<>(Arrays.asList(values));
        this.notCondition = notConditionValue;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isNotCondition() {
        return notCondition;
    }

    @Override
    public String toString() {
        return "StringFilter{" +
                "value=" + values +
                ", notCondition=" + notCondition +
                '}';
    }

    @Override
    public Object toTransferObject() {
        if (notCondition){
            Map<String, Object[]> result = new HashMap<>();
            result.put(OPERATOR_NOT, values.toArray());
            return result;
        }
        return values.toArray();
    }
}
