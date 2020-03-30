package com.incountry.residence.sdk.dto.search;

//todo create using Builder pattern

/**
 * Container for filters to searching of stored data by param values
 */
public class FindFilter {
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

    @Override
    public String toString() {
        return "FindFilter{" +
                "keyParam=" + keyParam +
                ", key2Param=" + key2Param +
                ", key3Param=" + key3Param +
                ", profileKeyParam=" + profileKeyParam +
                ", rangeKeyParam=" + rangeKeyParam +
                ", versionParam=" + versionParam +
                '}';
    }
}
