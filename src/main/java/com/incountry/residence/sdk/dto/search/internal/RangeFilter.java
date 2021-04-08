package com.incountry.residence.sdk.dto.search.internal;

import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RangeFilter extends Filter {

    private static final ValidationHelper HELPER = new ValidationHelper(LogManager.getLogger(RangeFilter.class));
    private static final List<String> OPERATOR_1_VALUES = Arrays.asList(OPERATOR_GREATER, OPERATOR_GREATER_OR_EQUALS);
    private static final List<String> OPERATOR_2_VALUES = Arrays.asList(OPERATOR_LESS, OPERATOR_LESS_OR_EQUALS);

    private static final String MSG_ERR_OPERATOR_1 = "Operator1 in range number filter can by only in [$gt,$gte]";
    private static final String MSG_ERR_OPERATOR_2 = "Operator2 in range number filter can by only in [$lt,$lte]";
    private static final String MSG_ERR_RANGE = "The first value in range filter can by only less or equals the second value";

    private final long value1;
    private final String operator1;
    private final long value2;
    private final String operator2;

    public RangeFilter(long value1, String operator1, long value2, String operator2) throws StorageClientException {
        validate(value1, operator1, value2, operator2);
        this.value1 = value1;
        this.value2 = value2;
        this.operator1 = operator1;
        this.operator2 = operator2;
    }

    private void validate(long value1, String operator1, long value2, String operator2) throws StorageClientException {
        boolean invalidOperator1 = !OPERATOR_1_VALUES.contains(operator1);
        HELPER.check(StorageClientException.class, invalidOperator1, MSG_ERR_OPERATOR_1);

        boolean invalidOperator2 = !OPERATOR_2_VALUES.contains(operator2);
        HELPER.check(StorageClientException.class, invalidOperator2, MSG_ERR_OPERATOR_2);

        boolean invalidRange = value1 > value2;
        HELPER.check(StorageClientException.class, invalidRange, MSG_ERR_RANGE);
    }

    @Override
    public Object toTransferObject() {
        Map<String, Long> result = new HashMap<>();
        result.put(operator1, value1);
        result.put(operator2, value2);
        return result;
    }
}

