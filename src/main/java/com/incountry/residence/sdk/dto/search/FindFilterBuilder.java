package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

/**
 * Builder for cosy creation of FindFilter
 */
public class FindFilterBuilder {

    public static final String OPER_NOT = "$not";
    public static final String OPER_GT = "$gt";
    public static final String OPER_GTE = "$gte";
    public static final String OPER_LT = "$lt";
    public static final String OPER_LTE = "$lte";

    private FindFilter filter;

    public static FindFilterBuilder create() {
        return new FindFilterBuilder();
    }

    private FindFilterBuilder() {
        filter = new FindFilter();
    }

    private FindFilterBuilder(FindFilter filter) {
        this.filter = filter;
    }

    public FindFilter build() throws StorageClientException {
        return filter.copy();
    }

    public FindFilterBuilder clear() {
        filter = new FindFilter();
        return this;
    }

    public FindFilterBuilder limitAndOffset(int limit, int offset) throws StorageClientException {
        filter.setLimit(limit);
        filter.setOffset(offset);
        return this;
    }

    //key
    public FindFilterBuilder keyEq(String... keys) throws StorageClientException {
        filter.setKeyFilter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder keyNotEq(String... keys) throws StorageClientException {
        filter.setKeyFilter(new FilterStringParam(keys, true));
        return this;
    }

    //key2
    public FindFilterBuilder key2Eq(String... keys) throws StorageClientException {
        filter.setKey2Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key2NotEq(String... keys) throws StorageClientException {
        filter.setKey2Filter(new FilterStringParam(keys, true));
        return this;
    }

    //key3
    public FindFilterBuilder key3Eq(String... keys) throws StorageClientException {
        filter.setKey3Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key3NotEq(String... keys) throws StorageClientException {
        filter.setKey3Filter(new FilterStringParam(keys, true));
        return this;
    }

    //profileKey
    public FindFilterBuilder profileKeyEq(String... keys) throws StorageClientException {
        filter.setProfileKeyFilter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder profileKeyNotEq(String... keys) throws StorageClientException {
        filter.setProfileKeyFilter(new FilterStringParam(keys, true));
        return this;
    }

    //version
    public FindFilterBuilder versionEq(String... versions) throws StorageClientException {
        filter.setVersionFilter(new FilterStringParam(versions));
        return this;
    }

    public FindFilterBuilder versionNotEq(String... versions) throws StorageClientException {
        filter.setVersionFilter(new FilterStringParam(versions, true));
        return this;
    }

    //rangeKey
    public FindFilterBuilder rangeKeyEq(Integer... keys) throws StorageClientException {
        filter.setRangeKeyFilter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKeyGT(int key) throws StorageClientException {
        filter.setRangeKeyFilter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKeyGTE(int key) throws StorageClientException {
        filter.setRangeKeyFilter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKeyLT(int key) throws StorageClientException {
        filter.setRangeKeyFilter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKeyLTE(int key) throws StorageClientException {
        filter.setRangeKeyFilter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKeyBetween(int fromValue, int toValue) throws StorageClientException {
        return rangeKeyBetween(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKeyBetween(int fromValue, boolean includeFrom, int toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKeyFilter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    @Override
    public String toString() {
        return "FindFilterBuilder{" +
                "filter=" + filter +
                '}';
    }

    public FindFilterBuilder copy() throws StorageClientException {
        return new FindFilterBuilder(this.filter.copy());
    }
}
