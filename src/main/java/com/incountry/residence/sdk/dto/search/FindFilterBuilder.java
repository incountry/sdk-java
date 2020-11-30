package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Builder for cosy creation of FindFilter
 */
public class FindFilterBuilder {

    private static final Logger LOG = LogManager.getLogger(FindFilterBuilder.class);

    private static final String MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS = "SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY10 lookup";

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
        checkSearchKeys();
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

    public FindFilterBuilder keyEq(StringField field, String... keys) throws StorageClientException {
        filter.setStringFilter(field, new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder keyEq(NumberField field, Long... keys) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder keyNotEq(StringField field, String... keys) throws StorageClientException {
        filter.setStringFilter(field, new FilterStringParam(keys, true));
        return this;
    }

    public FindFilterBuilder keyGT(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder keyGTE(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder keyLT(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder keyLTE(NumberField field, long key) throws StorageClientException {
        filter.setNumberFilter(field, new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder keyBetween(NumberField field, long fromValue, long toValue) throws StorageClientException {
        return keyBetween(field, fromValue, true, toValue, true);
    }

    public FindFilterBuilder keyBetween(NumberField field, long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
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

    private void checkSearchKeys() throws StorageClientException {
        Set<StringField> searchKeys = filter.getStringFilterMap().keySet();
        if ((searchKeys.contains(StringField.KEY1)
                || searchKeys.contains(StringField.KEY2)
                || searchKeys.contains(StringField.KEY3)
                || searchKeys.contains(StringField.KEY4)
                || searchKeys.contains(StringField.KEY5)
                || searchKeys.contains(StringField.KEY6)
                || searchKeys.contains(StringField.KEY7)
                || searchKeys.contains(StringField.KEY8)
                || searchKeys.contains(StringField.KEY9)
                || searchKeys.contains(StringField.KEY10))
                && searchKeys.contains(StringField.SEARCH_KEYS)) {
            LOG.error(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
            throw new StorageClientException(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
        }
    }
}
