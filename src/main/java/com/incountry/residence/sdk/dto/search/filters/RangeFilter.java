package com.incountry.residence.sdk.dto.search.filters;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RangeFilter extends Filter {

    private static final Logger LOG = LogManager.getLogger(RangeFilter.class);

    private static final String MSG_ERR_OPERATOR1_RANGE = "Operator1 in range number filter can by only in [$gt,$gte]";
    private static final String MSG_ERR_OPERATOR2_RANGE = "Operator2 in range number filter can by only in [$lt,$lte]";
    private static final String MSG_ERR_FIRST_VALUE_MORE_THEN_SECOND_VALUE = "The first value in range filter can by only less or equals the second value";

    private static List<Object> sOperator1Values  = new ArrayList<>();
    private static List<Object> sOperator2Values  = new ArrayList<>();

    private long value1;
    private String operator1;
    private long value2;
    private String operator2;

    public RangeFilter(long value1, String operator1, long value2, String operator2) throws StorageClientException {
        sOperator1Values.add(OPERATOR_GREATER);
        sOperator1Values.add(OPERATOR_GREATER_OR_EQUALS);
        sOperator2Values.add(OPERATOR_LESS);
        sOperator2Values.add(OPERATOR_LESS_OR_EQUALS);

        validate(value1, operator1, value2, operator2);
        this.value1 = value1;
        this.operator1 = operator1;
        this.value2 = value2;
        this.operator2 = operator2;
    }

    private static void validate(long value1, String operator1, long value2, String operator2) throws StorageClientException {
        if (!sOperator1Values.contains(operator1)) {
            LOG.error(MSG_ERR_OPERATOR1_RANGE);
            throw new StorageClientException(MSG_ERR_OPERATOR1_RANGE);
        }
        if (!sOperator2Values.contains(operator2)) {
            LOG.error(MSG_ERR_OPERATOR2_RANGE);
            throw new StorageClientException(MSG_ERR_OPERATOR2_RANGE);
        }
        if (value1 > value2) {
            LOG.error(MSG_ERR_FIRST_VALUE_MORE_THEN_SECOND_VALUE);
            throw new StorageClientException(MSG_ERR_FIRST_VALUE_MORE_THEN_SECOND_VALUE);
        }
    }

    @Override
    public Object toTransferObject() {
        Map<String, Long> obj =  new HashMap<>();
        obj.put(operator1, value1);
        obj.put(operator2, value2);
        return obj;
    }
}
