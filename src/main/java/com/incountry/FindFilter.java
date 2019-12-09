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

    public JSONObject toJSONObject(Crypto mCrypto) {
        JSONObject json = new JSONObject()
                .put(P_KEY, keyParam == null ? null : keyParam.toJSON(mCrypto))
                .put(P_KEY_2, key2Param == null ? null : key2Param.toJSON(mCrypto))
                .put(P_KEY_3, key3Param == null ? null : key3Param.toJSON(mCrypto))
                .put(P_PROFILE_KEY, profileKeyParam == null ? null : profileKeyParam.toJSON(mCrypto))
                .put(VERSION, versionParam == null ? null : versionParam.toJSON(mCrypto));
        if (rangeKeyParam != null){
            json.put(P_RANGE_KEY,  rangeKeyParam.isConditional() ? rangeKeyParam.conditionJSON() : rangeKeyParam.valueJSON());
        }
        return json;
    }
}
