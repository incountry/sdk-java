package com.incountry;

import org.json.JSONArray;
import org.json.JSONObject;

public class FilterRangeParam {
    private int[] values;
    private String operator;
    private int value;


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

    public JSONArray valueJSON() {
        if (values == null) return null;
        return new JSONArray(values);
    }

    public JSONObject conditionJSON() {
        return new JSONObject().put(operator, value);
    }
}
