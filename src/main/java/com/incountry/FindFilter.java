package com.incountry;

import com.incountry.crypto.Crypto;
import org.json.JSONObject;

public class FindFilter {
    private static final String P_KEY = "key";
    private static final String P_KEY_2 = "key2";
    private static final String P_KEY_3 = "key3";
    private static final String P_PROFILE_KEY = "profile_key";
    private static final String P_RANGE_KEY = "range_key";

    FilterStringParam keyParam;
    FilterStringParam profileKeyParam;
    FilterStringParam key2Param;
    FilterStringParam key3Param;
    FilterRangeParam rangeKeyParam;

    public FindFilter(){}

    public FindFilter(FilterStringParam key, FilterStringParam profileKey, FilterRangeParam rangeKey, FilterStringParam key2, FilterStringParam key3) {
        this.keyParam = key;
        this.profileKeyParam = profileKey;
        this.key2Param = key2;
        this.key3Param = key3;
        this.rangeKeyParam = rangeKey;
    }

    public void setKeyParam(FilterStringParam param){
        this.keyParam = param;
    }

    public void setKey2Param(FilterStringParam param){
        this.key2Param = param;
    }

    public void setKey3Param(FilterStringParam param){
        this.key3Param = param;
    }

    public void setProfileKeyParam(FilterStringParam param){
        this.profileKeyParam = param;
    }

    public void setRangeKeyParam(FilterRangeParam param){
        this.rangeKeyParam = param;
    }


    public JSONObject toJSONObject(Crypto mCrypto) {
        JSONObject json = new JSONObject()
                .put(P_KEY, keyParam == null ? null : keyParam.toJSON(mCrypto))
                .put(P_KEY_2, key2Param == null ? null : key2Param.toJSON(mCrypto))
                .put(P_KEY_3, key3Param == null ? null : key3Param.toJSON(mCrypto))
                .put(P_PROFILE_KEY, profileKeyParam == null ? null : profileKeyParam.toJSON(mCrypto));
        if (rangeKeyParam != null){
            json.put(P_RANGE_KEY,  rangeKeyParam.isConditional() ? rangeKeyParam.conditionJSON() : rangeKeyParam.valueJSON());
        }
        return json;
    }
}
