package com.incountry;

import com.incountry.crypto.Impl.Crypto;
import lombok.Setter;
import org.json.JSONObject;


public class FindFilter {
    private static final String P_KEY = "key";
    private static final String P_KEY_2 = "key2";
    private static final String P_KEY_3 = "key3";
    private static final String P_PROFILE_KEY = "profile_key";
    private static final String P_RANGE_KEY = "range_key";

    @Setter
    FilterStringParam keyParam;

    @Setter
    FilterStringParam profileKeyParam;

    @Setter
    FilterStringParam key2Param;

    @Setter
    FilterStringParam key3Param;

    @Setter
    FilterRangeParam rangeKeyParam;

    public FindFilter(){}

    public FindFilter(FilterStringParam key, FilterStringParam profileKey, FilterRangeParam rangeKey, FilterStringParam key2, FilterStringParam key3) {
        this.keyParam = key;
        this.profileKeyParam = profileKey;
        this.key2Param = key2;
        this.key3Param = key3;
        this.rangeKeyParam = rangeKey;
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
