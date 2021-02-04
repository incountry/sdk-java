package com.incountry.residence.sdk.dto.search.filters;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StringFilter extends Filter {

    private static final Logger LOG = LogManager.getLogger(NumberFilter.class);

    private static final String MSG_ERR_STRING_FILTER_NULL = "String filter or it's values can't be null";

    private List<String> values = new ArrayList<>();
    private boolean notCondition;

    public StringFilter(List<String> values) throws StorageClientException {
        this(values, false);
    }

    public StringFilter(List<String> values, boolean notCondition) throws StorageClientException {
        validate(values);
        this.values = values;
        this.notCondition = notCondition;
    }

    private static void validate(List<String> values) throws StorageClientException {
        if (values == null || values.size() == 0 || values.contains(null)) {
            LOG.error(MSG_ERR_STRING_FILTER_NULL);
            throw new StorageClientException(MSG_ERR_STRING_FILTER_NULL);
        }
    }

    @Override
    public Object toTransferObject() {
        if (notCondition) {
            Map<Object, Object> transferObject = new HashMap<>();
            transferObject.put(OPERATOR_NOT, values);
            return transferObject;
        }
        return values;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isNotCondition() {
        return notCondition;
    }
}
