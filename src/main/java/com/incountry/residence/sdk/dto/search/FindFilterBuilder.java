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

    public FindFilterBuilder stringKeyEq(StringField filed, String... keys) throws StorageClientException {
        filter.setStringFilter(filed, new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder stringKeyNotEq(StringField filed, String... keys) throws StorageClientException {
        filter.setStringFilter(filed, new FilterStringParam(keys, true));
        return this;
    }

    public FindFilterBuilder numberKeyEq(NumberField field, Long... keys) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder numberKeyGT(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder numberKeyGTE(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder numberKeyLT(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder numberKeyLTE(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder numberKeyBetween(NumberField field, long fromValue, long toValue) throws StorageClientException {
        return numberKeyBetween(field, fromValue, true, toValue, true);
    }

    public FindFilterBuilder numberKeyBetween(NumberField field, long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
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
