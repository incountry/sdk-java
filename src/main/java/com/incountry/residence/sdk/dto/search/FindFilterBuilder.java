package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.dto.search.internal.FilterNumberParam;
import com.incountry.residence.sdk.dto.search.internal.FilterStringParam;
import com.incountry.residence.sdk.dto.search.internal.FindFilter;
import com.incountry.residence.sdk.dto.search.internal.FilterNullParam;
import com.incountry.residence.sdk.dto.search.internal.SortingParam;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builder for cosy creation of FindFilter
 */
public class FindFilterBuilder {

    private static final Logger LOG = LogManager.getLogger(FindFilterBuilder.class);

    private static final String MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS = "SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY20 lookup";
    private static final String MSG_ERR_SEARCH_KEYS_LEN = "SEARCH_KEYS should contain at least 3 characters and be not longer than 200";
    private static final String MSG_ERR_SEARCH_KEYS_ADD = "SEARCH_KEYS can be used only via searchKeysLike method";
    private static final String MSG_ERR_NULL_SORT_FIELD = "Sorting field is null";
    private static final String MSG_ERR_DUPL_SORT_FIELD = "Field %s is already in sorting list";

    public static final String OPER_NOT = "$not";
    public static final String OPER_GT = "$gt";
    public static final String OPER_GTE = "$gte";
    public static final String OPER_LT = "$lt";
    public static final String OPER_LTE = "$lte";

    private FindFilter filter;
    private static final List<StringField> SEARCHABLE_KEYS = new ArrayList<>();

    static {
        SEARCHABLE_KEYS.add(StringField.KEY1);
        SEARCHABLE_KEYS.add(StringField.KEY2);
        SEARCHABLE_KEYS.add(StringField.KEY3);
        SEARCHABLE_KEYS.add(StringField.KEY4);
        SEARCHABLE_KEYS.add(StringField.KEY5);
        SEARCHABLE_KEYS.add(StringField.KEY6);
        SEARCHABLE_KEYS.add(StringField.KEY7);
        SEARCHABLE_KEYS.add(StringField.KEY8);
        SEARCHABLE_KEYS.add(StringField.KEY9);
        SEARCHABLE_KEYS.add(StringField.KEY10);
        SEARCHABLE_KEYS.add(StringField.KEY11);
        SEARCHABLE_KEYS.add(StringField.KEY12);
        SEARCHABLE_KEYS.add(StringField.KEY13);
        SEARCHABLE_KEYS.add(StringField.KEY14);
        SEARCHABLE_KEYS.add(StringField.KEY15);
        SEARCHABLE_KEYS.add(StringField.KEY16);
        SEARCHABLE_KEYS.add(StringField.KEY17);
        SEARCHABLE_KEYS.add(StringField.KEY18);
        SEARCHABLE_KEYS.add(StringField.KEY19);
        SEARCHABLE_KEYS.add(StringField.KEY20);
    }

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

    public FindFilterBuilder keyEq(StringField field, String... keys) throws StorageClientException {
        validateStringFilters(field);
        filter.setFilter(field, new FilterStringParam(keys));
        return this;
    }

    public FindFilterBuilder keyEq(NumberField field, Long... keys) throws StorageClientException {
        filter.setFilter(field, new FilterNumberParam(keys));
        return this;
    }

    public FindFilterBuilder keyIsNull(StringField field) {
        filter.setFilter(field, new FilterNullParam(true));
        return this;
    }

    public FindFilterBuilder keyIsNull(NumberField field) {
        filter.setFilter(field, new FilterNullParam(true));
        return this;
    }

    public FindFilterBuilder keyIsNotNull(StringField field) {
        filter.setFilter(field, new FilterNullParam(false));
        return this;
    }

    public FindFilterBuilder keyIsNotNull(NumberField field) {
        filter.setFilter(field, new FilterNullParam(false));
        return this;
    }

    public FindFilterBuilder keyNotEq(StringField field, String... keys) throws StorageClientException {
        validateStringFilters(field);
        filter.setFilter(field, new FilterStringParam(keys, true));
        return this;
    }

    public FindFilterBuilder keyGT(NumberField field, long key) throws StorageClientException {
        filter.setFilter(field, new FilterNumberParam(OPER_GT, key));
        return this;
    }

    public FindFilterBuilder keyGTE(NumberField field, long key) throws StorageClientException {
        filter.setFilter(field, new FilterNumberParam(OPER_GTE, key));
        return this;
    }

    public FindFilterBuilder keyLT(NumberField field, long key) throws StorageClientException {
        filter.setFilter(field, new FilterNumberParam(OPER_LT, key));
        return this;
    }

    public FindFilterBuilder keyLTE(NumberField field, long key) throws StorageClientException {
        filter.setFilter(field, new FilterNumberParam(OPER_LTE, key));
        return this;
    }

    public FindFilterBuilder keyBetween(NumberField field, long fromValue, long toValue) throws StorageClientException {
        return keyBetween(field, fromValue, true, toValue, true);
    }

    public FindFilterBuilder keyBetween(NumberField field, long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        filter.setFilter(field, new FilterNumberParam(includeFrom ? OPER_GTE : OPER_GT,
                fromValue,
                includeTo ? OPER_LTE : OPER_LT,
                toValue));
        return this;
    }

    public FindFilterBuilder searchKeysLike(String value) throws StorageClientException {
        Set<RecordField> searchKeys = filter.getFilterMap().keySet();
        for (StringField key : SEARCHABLE_KEYS) {
            if (searchKeys.contains(key)) {
                LOG.error(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
                throw new StorageClientException(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
            }
        }
        if (value.length() < 3 || value.length() > 200) {
            LOG.error(MSG_ERR_SEARCH_KEYS_LEN);
            throw new StorageClientException(MSG_ERR_SEARCH_KEYS_LEN);
        }
        filter.setFilter(StringField.SEARCH_KEYS, new FilterStringParam(new String[]{value}));
        return this;
    }

    public FindFilterBuilder sortBy(SortField field, SortOrder order) throws StorageClientException {
        if (field == null) {
            LOG.error(MSG_ERR_NULL_SORT_FIELD);
            throw new StorageClientException(MSG_ERR_NULL_SORT_FIELD);
        }
        for (SortingParam param : filter.getSortingList()) {
            if (param.getField().equals(field)) {
                String message = String.format(MSG_ERR_DUPL_SORT_FIELD, field);
                LOG.error(message);
                throw new StorageClientException(message);
            }
        }
        filter.addSorting(new SortingParam(field, order));
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

    private void validateStringFilters(StringField field) throws StorageClientException {
        if (field == StringField.SEARCH_KEYS) {
            LOG.error(MSG_ERR_SEARCH_KEYS_ADD);
            throw new StorageClientException(MSG_ERR_SEARCH_KEYS_ADD);
        }
        if (SEARCHABLE_KEYS.contains(field)
                && filter.getFilterMap().containsKey(StringField.SEARCH_KEYS)) {
            LOG.error(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
            throw new StorageClientException(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
        }
    }
}
