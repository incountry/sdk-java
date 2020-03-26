package com.incountry.storage.sdk.dto;

import com.incountry.storage.sdk.tools.crypto.Crypto;
import org.json.JSONObject;


public class FindFilter {
    private static final String P_KEY = "key";
    private static final String P_KEY_2 = "key2";
    private static final String P_KEY_3 = "key3";
    private static final String P_PROFILE_KEY = "profile_key";
    private static final String P_RANGE_KEY = "range_key";
    private static final String VERSION = "version";

    private FilterStringParam keyParam;

    private FilterStringParam key2Param;

    private FilterStringParam key3Param;

    private FilterStringParam profileKeyParam;

    private FilterRangeParam rangeKeyParam;

    private FilterStringParam versionParam;

    public FindFilter() {
    }

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

    public FilterStringParam getKeyParam() {
        return keyParam;
    }

    public void setKeyParam(FilterStringParam keyParam) {
        this.keyParam = keyParam;
    }

    public FilterStringParam getKey2Param() {
        return key2Param;
    }

    public void setKey2Param(FilterStringParam key2Param) {
        this.key2Param = key2Param;
    }

    public FilterStringParam getKey3Param() {
        return key3Param;
    }

    public void setKey3Param(FilterStringParam key3Param) {
        this.key3Param = key3Param;
    }

    public FilterStringParam getProfileKeyParam() {
        return profileKeyParam;
    }

    public void setProfileKeyParam(FilterStringParam profileKeyParam) {
        this.profileKeyParam = profileKeyParam;
    }

    public FilterRangeParam getRangeKeyParam() {
        return rangeKeyParam;
    }

    public void setRangeKeyParam(FilterRangeParam rangeKeyParam) {
        this.rangeKeyParam = rangeKeyParam;
    }

    public FilterStringParam getVersionParam() {
        return versionParam;
    }

    public void setVersionParam(FilterStringParam versionParam) {
        this.versionParam = versionParam;
    }

    /**
     * Adds 'not' condition to parameter
     *
     * @param param       parameter to which the not condition should be added
     * @param mCrypto     crypto object
     * @param isForString the condition must be added for string params
     * @return JSONObject with added 'not' condition
     */
    private JSONObject addNotCondition(FilterStringParam param, Crypto mCrypto, boolean isForString) {
        if (isForString) {
            return new JSONObject(String.format("{$not: %s}", param.toJSONString(mCrypto).toString()));
        }
        return new JSONObject(String.format("{$not: %s}", param.toJSONInt().toString()));
    }


    private void addToJson(JSONObject json, String paramName, FilterStringParam param, Crypto mCrypto) {
        if (param != null) {
            if (paramName.equals(VERSION)) {
                json.put(paramName, param.isNotCondition() ? addNotCondition(param, null, false) : param.toJSONInt());
            } else {
                json.put(paramName, param.isNotCondition() ? addNotCondition(param, mCrypto, true) : param.toJSONString(mCrypto));
            }
        }
    }

    /**
     * Creates JSONObject with FindFilter object properties
     *
     * @param mCrypto crypto object
     * @return JSONObject with properties corresponding to FindFilter object properties
     */
    public JSONObject toJSONObject(Crypto mCrypto) {
        JSONObject json = new JSONObject();
        addToJson(json, P_KEY, keyParam, mCrypto);
        addToJson(json, P_KEY_2, key2Param, mCrypto);
        addToJson(json, P_KEY_3, key3Param, mCrypto);
        addToJson(json, P_PROFILE_KEY, profileKeyParam, mCrypto);
        addToJson(json, VERSION, versionParam, mCrypto);
        if (rangeKeyParam != null) {
            json.put(P_RANGE_KEY, rangeKeyParam.isConditional() ? rangeKeyParam.conditionJSON() : rangeKeyParam.valueJSON());
        }
        return json;
    }
}
