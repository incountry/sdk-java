package com.incountry;

import com.incountry.crypto.impl.Crypto;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterStringParam {

    private List<String> value;
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
            notCondition = true;

        }
    }

    private List<String> hashValue(Crypto mCrypto) {
        return value.stream().map(item -> mCrypto.createKeyHash(item)).collect(Collectors.toList());
    }

    /**
     * Add not condition to parameters
     * @param value list of values to which the not condition should be added
     * @return list of values with not condition
     */
    private List<String> addNotCondition(List<String> value) {
        return value.stream().map(item -> "$not: " + item).collect(Collectors.toList());
    }

    public JSONArray toJSON(){
        return toJSON(null);
    }

    public JSONArray toJSON(Crypto mCrypto){
        if (value == null) return null;
        if (mCrypto == null) {
            return notCondition ? new JSONArray(addNotCondition(value)) : new JSONArray(value);
        }
        return notCondition ? new JSONArray(addNotCondition(hashValue( mCrypto))) : new JSONArray(hashValue(mCrypto));
    }
}
