package com.incountry;

import com.incountry.crypto.Crypto;
import lombok.Getter;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterStringParam {

    private List<String> value;

    @Getter
    private boolean notCondition;

    public FilterStringParam(List<String> value) {
        this.value = value;
    }

    public FilterStringParam(String filterValue) {
        this.value = new ArrayList<>();
        if (filterValue != null) {
            value.add(filterValue);
            notCondition = false;

        }
    }

    public FilterStringParam(String filterValue, boolean notConditionValue) {
        this.value = new ArrayList<>();
        if (filterValue != null) {
            value.add(filterValue);
            notCondition = notConditionValue;

        }
    }

    private List<String> hashValue(Crypto mCrypto) {
        return value.stream().map(mCrypto::createKeyHash).collect(Collectors.toList());
    }

    public JSONArray toJSONString(Crypto mCrypto) {
        if (value == null) return null;
        if (mCrypto == null) return new JSONArray(value);

        return new JSONArray(hashValue(mCrypto));
    }

    public JSONArray toJSONInt() {
        if (value == null) {
            return null;
        }
        return new JSONArray(value.stream().map(Integer::parseInt).collect(Collectors.toList()));
    }

}
