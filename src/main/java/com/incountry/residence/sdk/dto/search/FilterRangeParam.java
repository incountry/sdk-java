package com.incountry.residence.sdk.dto.search;

import java.util.Arrays;

public class FilterRangeParam {
    private int[] values;
    private int value;
    private String operator;

    public FilterRangeParam(int[] values) {
        this.values = values;
    }

    public FilterRangeParam(int value) {
        this.values = new int[]{value};
    }

    public FilterRangeParam(String operator, int value) {
        this.value = value;
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

    @Override
    public String toString() {
        return "FilterRangeParam{" +
                "values=" + Arrays.toString(values) +
                ", value=" + value +
                ", operator='" + operator + '\'' +
                '}';
    }
}
