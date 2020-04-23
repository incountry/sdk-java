package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;

import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_GT;
import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_GTE;
import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_LT;
import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_LTE;
import static com.incountry.residence.sdk.dto.search.FindFilterBuilder.OPER_NOT;

public class FilterNumberParam {

    private static final Logger LOG = LogManager.getLogger(FilterNumberParam.class);

    private static final String ERR_NULL_VALUE = "FilterNumberParam values can't be null";
    private static final String ERR_OPER1_RESTR = String.format("Operator1 in range filter can by only %s or %s", OPER_GT, OPER_GTE);
    private static final String ERR_OPER2_RESTR = String.format("Operator2 in range filter can by only %s or %s", OPER_LT, OPER_LTE);
    private static final String ERR_RANGE_RESTR = "Value1 in range filter can by only less or equals value2";
    private static final String ERR_CONDITION_RESTR = String.format("Operator in number filter can by only in [%s,%s,%s,%s,%s]",
            OPER_LT, OPER_LTE, OPER_GT, OPER_GTE, OPER_NOT);

    private int[] values;
    private String operator1;
    private String operator2;

    private FilterNumberParam() {
    }

    public FilterNumberParam(int[] values) throws StorageClientException {
        if (values == null || values.length == 0) {
            LOG.error(ERR_NULL_VALUE);
            throw new StorageClientException(ERR_NULL_VALUE);
        }
        this.values = Arrays.copyOf(values, values.length);
    }

    public FilterNumberParam(int value) {
        this.values = new int[]{value};
    }

    public FilterNumberParam(String operator, int value) throws StorageClientException {
        if (operator == null || notIn(operator, OPER_GT, OPER_GTE, OPER_NOT, OPER_LT, OPER_LTE)) {
            LOG.error(ERR_CONDITION_RESTR);
            throw new StorageClientException(ERR_CONDITION_RESTR);
        }
        this.values = new int[]{value};
        this.operator1 = operator;
    }

    public FilterNumberParam(String operator1, int value1, String operator2, int value2) throws StorageClientException {
        if (operator1 == null || notIn(operator1, OPER_GT, OPER_GTE)) {
            LOG.error(ERR_OPER1_RESTR);
            throw new StorageClientException(ERR_OPER1_RESTR);
        }
        if (operator2 == null || (!OPER_LT.equals(operator2)) && !OPER_LTE.equals(operator2)) {
            LOG.error(ERR_OPER2_RESTR);
            throw new StorageClientException(ERR_OPER2_RESTR);
        }
        if (value1 > value2) {
            LOG.error(ERR_RANGE_RESTR);
            throw new StorageClientException(ERR_RANGE_RESTR);
        }
        this.values = new int[]{value1, value2};
        this.operator1 = operator1;
        this.operator2 = operator2;
    }

    public static FilterNumberParam createNotFilter(int[] values) throws StorageClientException {
        FilterNumberParam filter = new FilterNumberParam(values);
        filter.operator1 = OPER_NOT;
        return filter;
    }

    private boolean notIn(String operator, String... permitted) {
        for (String one : permitted) {
            if (one.equals(operator)) {
                return false;
            }
        }
        return true;
    }

    public boolean isConditional() {
        return operator1 != null;
    }

    public boolean isRange() {
        return operator2 != null && operator1 != null;
    }

    public int[] getValues() {
        if (values != null && values.length > 0) {
            return Arrays.copyOf(values, values.length);
        }
        return new int[]{};
    }

    public String getOperator1() {
        return operator1;
    }

    public String getOperator2() {
        return operator2;
    }

    public FilterNumberParam copy() {
        FilterNumberParam clone = new FilterNumberParam();
        clone.values = getValues();
        clone.operator1 = operator1;
        clone.operator2 = operator2;
        return clone;
    }

    @Override
    public String toString() {
        return "FilterRangeParam{" +
                "values=" + Arrays.toString(values) +
                ", operator1='" + operator1 + '\'' +
                ", operator2='" + operator2 + '\'' +
                '}';
    }
}
