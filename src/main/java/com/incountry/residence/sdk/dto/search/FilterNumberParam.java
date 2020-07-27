package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

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

    private final Long[] values;
    private final String operator1;
    private final String operator2;

    private FilterNumberParam(Long[] values, String operator1, String operator2) {
        this.values = Arrays.copyOf(values, values.length);
        this.operator1 = operator1;
        this.operator2 = operator2;
    }

    public FilterNumberParam(Long[] values) throws StorageClientException {
        if (values == null || values.length == 0 || (Stream.of(values).anyMatch(Objects::isNull))) {
            LOG.error(ERR_NULL_VALUE);
            throw new StorageClientException(ERR_NULL_VALUE);
        }
        this.values = Arrays.copyOf(values, values.length);
        this.operator1 = null;
        this.operator2 = null;
    }

    public FilterNumberParam(String operator, Long value) throws StorageClientException {
        if (operator == null || notIn(operator, OPER_GT, OPER_GTE, OPER_NOT, OPER_LT, OPER_LTE)) {
            LOG.error(ERR_CONDITION_RESTR);
            throw new StorageClientException(ERR_CONDITION_RESTR);
        }
        if (value == null) {
            LOG.error(ERR_NULL_VALUE);
            throw new StorageClientException(ERR_NULL_VALUE);
        }
        this.values = new Long[]{value};
        this.operator1 = operator;
        this.operator2 = null;
    }

    public FilterNumberParam(String operator1, Long value1, String operator2, Long value2) throws StorageClientException {
        if (operator1 == null || notIn(operator1, OPER_GT, OPER_GTE)) {
            LOG.error(ERR_OPER1_RESTR);
            throw new StorageClientException(ERR_OPER1_RESTR);
        }
        if (operator2 == null || (!OPER_LT.equals(operator2)) && !OPER_LTE.equals(operator2)) {
            LOG.error(ERR_OPER2_RESTR);
            throw new StorageClientException(ERR_OPER2_RESTR);
        }
        if (value1 == null || value2 == null) {
            LOG.error(ERR_NULL_VALUE);
            throw new StorageClientException(ERR_NULL_VALUE);
        }
        if (value1 > value2) {
            LOG.error(ERR_RANGE_RESTR);
            throw new StorageClientException(ERR_RANGE_RESTR);
        }
        this.values = new Long[]{value1, value2};
        this.operator1 = operator1;
        this.operator2 = operator2;
    }


    private boolean notIn(String operator, String... permitted) {
        for (String value : permitted) {
            if (value.equals(operator)) {
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

    public Long[] getValues() {
        if (values != null && values.length > 0) {
            return Arrays.copyOf(values, values.length);
        }
        return new Long[]{};
    }

    public String getOperator1() {
        return operator1;
    }

    public String getOperator2() {
        return operator2;
    }

    public FilterNumberParam copy() {
        return new FilterNumberParam(getValues(), getOperator1(), getOperator2());
    }

    @Override
    public String toString() {
        return "FilterRangeParam{" +
                "values=" + Arrays.toString(values) +
                ", operator1='" + "" + '\'' +
                ", operator2='" + "" + '\'' +
                '}';
    }
}
