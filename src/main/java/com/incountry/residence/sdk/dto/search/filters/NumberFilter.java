package com.incountry.residence.sdk.dto.search.filters;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NumberFilter extends Filter {

    private static final Logger LOG = LogManager.getLogger(NumberFilter.class);

    private static final String MSG_ERR_NULL_NUMBER_FILTER = "Number filter or it's values can't be null";
    private static final String MSG_ERR_OPERATOR_NON_RANGE = "Operator in non range number filter can by only in [NULL,$not,$lt,$lte,$gt,$gte]";
    private static final String MSG_ERR_OPERATOR_IN_LIST = "Operator in list number filter can by only in [NULL,$not]";

    private static List<String> sAllOperators = new ArrayList<>();
    private static List<String> sMultipleValueOperators = new ArrayList<>();


    private List<Long> values = new ArrayList<>();
    private String operator;

    static {
        sAllOperators.add(null);
        sAllOperators.add(OPERATOR_GREATER);
        sAllOperators.add(OPERATOR_GREATER_OR_EQUALS);
        sAllOperators.add(OPERATOR_LESS);
        sAllOperators.add(OPERATOR_LESS_OR_EQUALS);
        sAllOperators.add(OPERATOR_NOT);

        sMultipleValueOperators.add(null);
        sMultipleValueOperators.add(OPERATOR_NOT);
    }

    public NumberFilter(List<Long> values) throws StorageClientException {
        this(values, null);
    }

    public NumberFilter(List<Long> values, String operator) throws StorageClientException {
        validate(values, operator);
        this.values.addAll(values);
        this.operator = operator;
    }

    private static void validate(List<Long> values, String operator) throws StorageClientException {
        if (values == null || values.isEmpty()) {
            LOG.error(MSG_ERR_NULL_NUMBER_FILTER);
            throw new StorageClientException(MSG_ERR_NULL_NUMBER_FILTER);
        }
        if (!sAllOperators.contains(operator)) {
            LOG.error(MSG_ERR_OPERATOR_NON_RANGE);
            throw new StorageClientException(MSG_ERR_OPERATOR_NON_RANGE);
        }
        if (!sMultipleValueOperators.contains(operator) && values.size() > 1) {
            LOG.error(MSG_ERR_OPERATOR_IN_LIST);
            throw new StorageClientException(MSG_ERR_OPERATOR_IN_LIST);
        }
    }

    @Override
    public Object toTransferObject() {
        if (operator == null) {
            return values;
        }
        Map<Object, Object> transferObject = new HashMap<>();
        transferObject.put(operator, values);
        return transferObject;
    }

    public List<Long> getValues() {
        return values;
    }

    public String getOperator() {
        return operator;
    }
}
