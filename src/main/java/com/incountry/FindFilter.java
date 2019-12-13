package com.incountry;

import com.incountry.crypto.impl.Crypto;
import lombok.Setter;
import org.json.JSONObject;


public class FindFilter {
    private static final String P_KEY = "key";
    private static final String P_KEY_2 = "key2";
    private static final String P_KEY_3 = "key3";
    private static final String P_PROFILE_KEY = "profile_key";
    private static final String P_RANGE_KEY = "range_key";
    private static final String VERSION = "version";

    @Setter
    FilterStringParam keyParam;

    @Setter
    FilterStringParam key2Param;

    @Setter
    FilterStringParam key3Param;

    @Setter
    FilterStringParam profileKeyParam;

    @Setter
    FilterRangeParam rangeKeyParam;

    @Setter
    FilterStringParam versionParam;

    public FindFilter(){}

    public FindFilter(
            FilterStringParam key,
            FilterStringParam key2,
            FilterStringParam key3,
            FilterStringParam profileKey,
            FilterRangeParam rangeKey,
            FilterStringParam version) {
        this.keyParam = key;
        this.profileKeyParam = profileKey;
        this.key2Param = key2;
        this.key3Param = key3;
        this.rangeKeyParam = rangeKey;
        this.versionParam = version;
    }

    /**
     * Add 'not' condition to parameter
     * @param param parameter to which the not condition should be added
     * @param mCrypto crypto object
     * @return JSONObject with added 'not' condition
     */
    private JSONObject addNotCondition(FilterStringParam param, Crypto mCrypto) {
        return new JSONObject(String.format("{$not: %s}", param.toJSON(mCrypto).toString()));
    }

    private JSONObject addToJson(JSONObject json, String paramName, FilterStringParam param, Crypto mCrypto) {
        if (param != null) {
            if (paramName.equals(VERSION)) {
                json.put(paramName, param.isNotCondition() ? addNotCondition(param, null) : param.toJSON(null));
            } else {
                json.put(paramName, param.isNotCondition() ? addNotCondition(param, mCrypto) : param.toJSON(mCrypto));
            }
        }
        return json;
    }

    public JSONObject toJSONObject(Crypto mCrypto) {
        JSONObject json = new JSONObject();
        addToJson(json, P_KEY, keyParam, mCrypto);
        addToJson(json, P_KEY_2, key2Param, mCrypto);
        addToJson(json, P_KEY_3, key3Param, mCrypto);
        addToJson(json, P_PROFILE_KEY, profileKeyParam, mCrypto);
        addToJson(json, VERSION, versionParam, mCrypto);
        if (rangeKeyParam != null){
            json.put(P_RANGE_KEY,  rangeKeyParam.isConditional() ? rangeKeyParam.conditionJSON() : rangeKeyParam.valueJSON());
        }
        return json;
    }
}
