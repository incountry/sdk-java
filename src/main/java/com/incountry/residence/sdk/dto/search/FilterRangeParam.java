package com.incountry.residence.sdk.dto.search;

import java.util.Arrays;

public class FilterRangeParam {
    private int[] values;
    private int value;
    private String operator;

    public FilterRangeParam(int[] values) {
        this(null, values);
    }

    public FilterRangeParam(int value) {
        this.values = new int[]{value};
    }

    public FilterRangeParam(String operator, int value) {
        this.value = value;
        this.operator = operator;
    }

    public FilterRangeParam(String operator, int[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("FilterRangeParam values can't be null");
        }
        this.values = values;
        this.operator = operator;
    }

    public boolean isConditional() {
        return operator != null;
    }

    public int[] getValues() {
        return values;
    }

    public int getValue() {
        return value;
    }

    public String getOperator() {
        return operator;
    }

    public FilterRangeParam copy() {
        FilterRangeParam clone = new FilterRangeParam(operator, value);
        if (values != null) {
            clone.values = Arrays.copyOf(values, values.length);
        }
        return clone;
    }

    @Override
    public String toString() {
        return "FilterRangeParam{" +
                "values=" + Arrays.toString(values) +
                ", value=" + value +
                ", operator='" + operator + '\'' +
                '}';
    }
}
