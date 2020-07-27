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

    //recordKey
    public FindFilterBuilder recordKeyEq(String... keys) throws StorageClientException {
        filter.setRecordKeyFilter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder recordKeyNotEq(String... keys) throws StorageClientException {
        filter.setRecordKeyFilter(new FilterStringParam(keys, true));
        return this;
    }

    //key1
    public FindFilterBuilder key1Eq(String... keys) throws StorageClientException {
        filter.setKey1Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key1NotEq(String... keys) throws StorageClientException {
        filter.setKey1Filter(new FilterStringParam(keys, true));
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

    //key4
    public FindFilterBuilder key4Eq(String... keys) throws StorageClientException {
        filter.setKey4Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key4NotEq(String... keys) throws StorageClientException {
        filter.setKey4Filter(new FilterStringParam(keys, true));
        return this;
    }

    //key5
    public FindFilterBuilder key5Eq(String... keys) throws StorageClientException {
        filter.setKey5Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key5NotEq(String... keys) throws StorageClientException {
        filter.setKey5Filter(new FilterStringParam(keys, true));
        return this;
    }

    //key6
    public FindFilterBuilder key6Eq(String... keys) throws StorageClientException {
        filter.setKey6Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key6NotEq(String... keys) throws StorageClientException {
        filter.setKey6Filter(new FilterStringParam(keys, true));
        return this;
    }

    //key7
    public FindFilterBuilder key7Eq(String... keys) throws StorageClientException {
        filter.setKey7Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key7NotEq(String... keys) throws StorageClientException {
        filter.setKey7Filter(new FilterStringParam(keys, true));
        return this;
    }

    //key8
    public FindFilterBuilder key8Eq(String... keys) throws StorageClientException {
        filter.setKey8Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key8NotEq(String... keys) throws StorageClientException {
        filter.setKey8Filter(new FilterStringParam(keys, true));
        return this;
    }

    //key9
    public FindFilterBuilder key9Eq(String... keys) throws StorageClientException {
        filter.setKey9Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key9NotEq(String... keys) throws StorageClientException {
        filter.setKey9Filter(new FilterStringParam(keys, true));
        return this;
    }

    //key10
    public FindFilterBuilder key10Eq(String... keys) throws StorageClientException {
        filter.setKey10Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder key10NotEq(String... keys) throws StorageClientException {
        filter.setKey10Filter(new FilterStringParam(keys, true));
        return this;
    }

    //errorCorrectionKey1
    public FindFilterBuilder errorCorrectionKey1Eq(String... keys) throws StorageClientException {
        filter.setErrorCorrectionKey1Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder errorCorrectionKey1NotEq(String... keys) throws StorageClientException {
        filter.setErrorCorrectionKey1Filter(new FilterStringParam(keys, true));
        return this;
    }

    //errorCorrectionKey2
    public FindFilterBuilder errorCorrectionKey2Eq(String... keys) throws StorageClientException {
        filter.setErrorCorrectionKey2Filter(new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder errorCorrectionKey2NotEq(String... keys) throws StorageClientException {
        filter.setErrorCorrectionKey2Filter(new FilterStringParam(keys, true));
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
    public FindFilterBuilder rangeKey1Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey1Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey1GT(long key) throws StorageClientException {
        filter.setRangeKey1Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey1GTE(long key) throws StorageClientException {
        filter.setRangeKey1Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey1LT(long key) throws StorageClientException {
        filter.setRangeKey1Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey1LTE(long key) throws StorageClientException {
        filter.setRangeKey1Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey1Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey1Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey1Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey1Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey2
    public FindFilterBuilder rangeKey2Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey2Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey2GT(long key) throws StorageClientException {
        filter.setRangeKey2Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey2GTE(long key) throws StorageClientException {
        filter.setRangeKey2Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey2LT(long key) throws StorageClientException {
        filter.setRangeKey2Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey2LTE(long key) throws StorageClientException {
        filter.setRangeKey2Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey2Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey2Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey2Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey2Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey3
    public FindFilterBuilder rangeKey3Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey3Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey3GT(long key) throws StorageClientException {
        filter.setRangeKey3Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey3GTE(long key) throws StorageClientException {
        filter.setRangeKey3Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey3LT(long key) throws StorageClientException {
        filter.setRangeKey3Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey3LTE(long key) throws StorageClientException {
        filter.setRangeKey3Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey3Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey3Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey3Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey3Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey4
    public FindFilterBuilder rangeKey4Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey4Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey4GT(long key) throws StorageClientException {
        filter.setRangeKey4Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey4GTE(long key) throws StorageClientException {
        filter.setRangeKey4Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey4LT(long key) throws StorageClientException {
        filter.setRangeKey4Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey4LTE(long key) throws StorageClientException {
        filter.setRangeKey4Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey4Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey4Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey4Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey4Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey5
    public FindFilterBuilder rangeKey5Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey5Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey5GT(long key) throws StorageClientException {
        filter.setRangeKey5Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey5GTE(long key) throws StorageClientException {
        filter.setRangeKey5Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey5LT(long key) throws StorageClientException {
        filter.setRangeKey5Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey5LTE(long key) throws StorageClientException {
        filter.setRangeKey5Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey5Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey5Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey5Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey5Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey6
    public FindFilterBuilder rangeKey6Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey6Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey6GT(long key) throws StorageClientException {
        filter.setRangeKey6Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey6GTE(long key) throws StorageClientException {
        filter.setRangeKey6Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey6LT(long key) throws StorageClientException {
        filter.setRangeKey6Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey6LTE(long key) throws StorageClientException {
        filter.setRangeKey6Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey6Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey6Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey6Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey6Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey7
    public FindFilterBuilder rangeKey7Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey7Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey7GT(long key) throws StorageClientException {
        filter.setRangeKey7Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey7GTE(long key) throws StorageClientException {
        filter.setRangeKey7Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey7LT(long key) throws StorageClientException {
        filter.setRangeKey7Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey7LTE(long key) throws StorageClientException {
        filter.setRangeKey7Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey7Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey7Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey7Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey7Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey8
    public FindFilterBuilder rangeKey8Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey8Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey8GT(long key) throws StorageClientException {
        filter.setRangeKey8Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey8GTE(long key) throws StorageClientException {
        filter.setRangeKey8Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey8LT(long key) throws StorageClientException {
        filter.setRangeKey8Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey8LTE(long key) throws StorageClientException {
        filter.setRangeKey8Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey8Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey8Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey8Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey8Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey9
    public FindFilterBuilder rangeKey9Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey9Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey9GT(long key) throws StorageClientException {
        filter.setRangeKey9Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey9GTE(long key) throws StorageClientException {
        filter.setRangeKey9Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey9LT(long key) throws StorageClientException {
        filter.setRangeKey9Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey9LTE(long key) throws StorageClientException {
        filter.setRangeKey9Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey9Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey9Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey9Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey9Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    //rangeKey10
    public FindFilterBuilder rangeKey10Eq(Long... keys) throws StorageClientException {
        filter.setRangeKey10Filter(new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder rangeKey10GT(long key) throws StorageClientException {
        filter.setRangeKey10Filter(new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder rangeKey10GTE(long key) throws StorageClientException {
        filter.setRangeKey10Filter(new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey10LT(long key) throws StorageClientException {
        filter.setRangeKey10Filter(new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder rangeKey10LTE(long key) throws StorageClientException {
        filter.setRangeKey10Filter(new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder rangeKey10Between(long fromValue, long toValue) throws StorageClientException {
        return rangeKey10Between(fromValue, true, toValue, true);
    }

    public FindFilterBuilder rangeKey10Between(long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setRangeKey10Filter(new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
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
