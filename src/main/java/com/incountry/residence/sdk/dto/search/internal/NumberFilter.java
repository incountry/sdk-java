package com.incountry.residence.sdk.dto.search.internal;

import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Arrays;
import java.util.Map;

public class NumberFilter extends Filter {

    private static final ValidationHelper HELPER = new ValidationHelper(LogManager.getLogger(NumberFilter.class));

    private static final List<String> ALL_OPERATORS = Arrays.asList(OPERATOR_GREATER, OPERATOR_GREATER_OR_EQUALS, OPERATOR_LESS, OPERATOR_LESS_OR_EQUALS, OPERATOR_NOT, null);
    private static final List<String> MULTIPLE_VALUE_OPERATORS = Arrays.asList(OPERATOR_NOT, null);

    private static final String MSG_ERR_NULL_NUMBER_FILTER = "Number filter or it's values can't be null";
    private static final String MSG_ERR_ILLEGAL_OPERATOR = "Operator in non-range number filter can be only one of [NULL,$not,$lt,$lte,$gt,$gte]";
    private static final String MSG_ERR_ILLEGAL_OPERATOR_FOR_SINGLE_VALUE = "Operator in list number filter can be only one of [NULL,$not]";

    private final List<Long> values = new ArrayList<>();
    public final String operator;


    public NumberFilter(Long[] keys, String operator) throws StorageClientException {
        validate(keys, operator);
        this.values.addAll(Arrays.asList(keys));
        this.operator = operator;
    }

    @SuppressWarnings("java:S2259")
    private void validate(Long[] values, String operator) throws StorageClientException {
        boolean isInvalidFilter = values == null || values.length == 0;
        HELPER.check(StorageClientException.class, isInvalidFilter, MSG_ERR_NULL_NUMBER_FILTER);
        boolean isInvalidOperator = !ALL_OPERATORS.contains(operator);
        HELPER.check(StorageClientException.class, isInvalidOperator, MSG_ERR_ILLEGAL_OPERATOR);
        boolean isInvalidSingleOperatorValues = !MULTIPLE_VALUE_OPERATORS.contains(operator) && values.length > 1;
        HELPER.check(StorageClientException.class, isInvalidSingleOperatorValues, MSG_ERR_ILLEGAL_OPERATOR_FOR_SINGLE_VALUE);
    }

    @Override
    public Object toTransferObject() {
        if (operator == null) {
            return values.toArray();
        }
        Map<String, Object> result = new HashMap<>();
        result.put(operator, values.size() == 1 ? values.get(0) : values.toArray());
        return result;
    }
}
