package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.dto.search.internal.AbstractFilter;
import com.incountry.residence.sdk.dto.search.internal.NullFilter;
import com.incountry.residence.sdk.dto.search.internal.Filter;
import com.incountry.residence.sdk.dto.search.internal.RangeFilter;
import com.incountry.residence.sdk.dto.search.internal.StringFilter;
import com.incountry.residence.sdk.dto.search.internal.SortingParam;
import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Container for filters to searching of stored data by param values
 */
public class FindFilter {
    private static final Logger LOG = LogManager.getLogger(FindFilter.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);

    public static final int MAX_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 0;
    public static final int SEARCH_KEYS_MIN_LENGTH = 3;
    public static final int SEARCH_KEYS_MAX_LENGTH = 200;

    private static final String MSG_ERR_MAX_LIMIT = "Max limit is " + MAX_LIMIT + ". Use offset to populate more";
    private static final String MSG_ERR_NULL_DATE = "Date filter can't be null";
    private static final String MSG_ERR_NEGATIVE_LIMIT = "Limit must be more than 1";
    private static final String MSG_ERR_NEGATIVE_OFFSET = "Offset must be more than 0";
    private static final String MSG_ERR_SEARCH_KEYS_COMBINATION = "SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY20 lookup";
    private static final String MSG_ERR_SEARCH_KEYS_LEN = "SEARCH_KEYS should contain at least " + SEARCH_KEYS_MIN_LENGTH +
            " characters and be not longer than " + SEARCH_KEYS_MAX_LENGTH;
    private static final String MSG_ERR_NULL_SORT_FIELD = "Sorting field is null";
    private static final String MSG_ERR_NULL_SORT_ORDER = "Sorting order is null";
    private static final String MSG_ERR_DUPL_SORT_FIELD = "Field %s is already in sorting list";
    private static final String MSG_ERR_NULL_DATE_OPERATION = "This operation is available only for field 'ExpiresAt'";

    private static final List<StringField> NON_HASHED_KEY_LIST = Arrays.asList(
            StringField.KEY1, StringField.KEY2, StringField.KEY3, StringField.KEY4, StringField.KEY5,
            StringField.KEY6, StringField.KEY7, StringField.KEY8, StringField.KEY9, StringField.KEY10,
            StringField.KEY11, StringField.KEY12, StringField.KEY13, StringField.KEY14, StringField.KEY15,
            StringField.KEY16, StringField.KEY17, StringField.KEY18, StringField.KEY19, StringField.KEY20
    );

    private int limit = MAX_LIMIT;
    private int offset = DEFAULT_OFFSET;
    private final Map<StringField, AbstractFilter> stringFilters = new EnumMap<>(StringField.class);
    private final Map<NumberField, AbstractFilter> numberFilters = new EnumMap<>(NumberField.class);
    private final Map<DateField, AbstractFilter> dateFilters = new EnumMap<>(DateField.class);
    private final List<SortingParam> sortingList = new ArrayList<>();
    private String searchKeys;

    public FindFilter clear() {
        stringFilters.clear();
        numberFilters.clear();
        dateFilters.clear();
        sortingList.clear();
        limit = MAX_LIMIT;
        offset = DEFAULT_OFFSET;
        searchKeys = null;
        return this;
    }

    public FindFilter limitAndOffset(int limit, int offset) throws StorageClientException {
        HELPER.check(StorageClientException.class, limit > MAX_LIMIT, MSG_ERR_MAX_LIMIT);
        HELPER.check(StorageClientException.class, limit < 1, MSG_ERR_NEGATIVE_LIMIT);
        HELPER.check(StorageClientException.class, offset < 0, MSG_ERR_NEGATIVE_OFFSET);
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    public FindFilter keyEq(StringField field, String... values) throws StorageClientException {
        validateStringFilters(field, searchKeys);
        stringFilters.put(field, new StringFilter(values));
        return this;
    }

    public FindFilter keyEq(NumberField field, Long... values) throws StorageClientException {
        numberFilters.put(field, new Filter<Long>(values, null));
        return this;
    }

    public FindFilter keyEq(DateField field, Date date) throws StorageClientException {
        HELPER.check(StorageClientException.class, date == null, MSG_ERR_NULL_DATE);
        dateFilters.put(field, new Filter<>(new Date[]{date}, null));
        return this;
    }

    public FindFilter keyIsNull(StringField field) throws StorageClientException {
        validateStringFilters(field, searchKeys);
        stringFilters.put(field, new NullFilter(true));
        return this;
    }

    public FindFilter keyIsNull(NumberField field) {
        numberFilters.put(field, new NullFilter(true));
        return this;
    }

    public FindFilter keyIsNull(DateField field) throws StorageClientException {
        HELPER.check(StorageClientException.class, field != DateField.EXPIRES_AT, MSG_ERR_NULL_DATE_OPERATION);
        dateFilters.put(field, new NullFilter(true));
        return this;
    }

    public FindFilter keyIsNotNull(StringField field) throws StorageClientException {
        validateStringFilters(field, searchKeys);
        stringFilters.put(field, new NullFilter(false));
        return this;
    }

    public FindFilter keyIsNotNull(NumberField field) {
        numberFilters.put(field, new NullFilter(false));
        return this;
    }

    public FindFilter keyIsNotNull(DateField field) throws StorageClientException {
        HELPER.check(StorageClientException.class, field != DateField.EXPIRES_AT, MSG_ERR_NULL_DATE_OPERATION);
        dateFilters.put(field, new NullFilter(false));
        return this;
    }

    public FindFilter keyNotEq(StringField field, String... values) throws StorageClientException {
        validateStringFilters(field, searchKeys);
        stringFilters.put(field, new StringFilter(values, true));
        return this;
    }

    public FindFilter keyNotEq(NumberField field, Long... values) throws StorageClientException {
        numberFilters.put(field, new Filter<Long>(values, AbstractFilter.OPERATOR_NOT));
        return this;
    }

    public FindFilter keyNotEq(DateField field, Date value) throws StorageClientException {
        dateFilters.put(field, new Filter<Date>(new Date[]{value}, AbstractFilter.OPERATOR_NOT));
        return this;
    }

    public FindFilter keyGreater(NumberField field, long value) throws StorageClientException {
        return keyGreater(field, value, false);
    }

    public FindFilter keyGreater(NumberField field, long value, boolean includingValue) throws StorageClientException {
        numberFilters.put(field, new Filter<Long>(new Long[]{value}, includingValue ? AbstractFilter.OPERATOR_GREATER_OR_EQUALS : AbstractFilter.OPERATOR_GREATER));
        return this;
    }

    public FindFilter keyGreater(DateField field, Date value) throws StorageClientException {
        return keyGreater(field, value, false);
    }

    public FindFilter keyGreater(DateField field, Date value, boolean includingValue) throws StorageClientException {
        dateFilters.put(field, new Filter<Date>(new Date[]{value}, includingValue ? AbstractFilter.OPERATOR_GREATER_OR_EQUALS : AbstractFilter.OPERATOR_GREATER));
        return this;
    }

    public FindFilter keyLess(NumberField field, long value) throws StorageClientException {
        return keyLess(field, value, false);
    }

    public FindFilter keyLess(NumberField field, long value, boolean includingValue) throws StorageClientException {
        numberFilters.put(field, new Filter<Long>(new Long[]{value}, includingValue ? AbstractFilter.OPERATOR_LESS_OR_EQUALS : AbstractFilter.OPERATOR_LESS));
        return this;
    }

    public FindFilter keyLess(DateField field, Date value) throws StorageClientException {
        return keyLess(field, value, false);
    }

    public FindFilter keyLess(DateField field, Date value, boolean includingValue) throws StorageClientException {
        dateFilters.put(field, new Filter<Date>(new Date[]{value}, includingValue ? AbstractFilter.OPERATOR_LESS_OR_EQUALS : AbstractFilter.OPERATOR_LESS));
        return this;
    }

    public FindFilter keyBetween(NumberField field, long fromValue, long toValue) throws StorageClientException {
        return keyBetween(field, fromValue, true, toValue, true);
    }

    public FindFilter keyBetween(NumberField field, long fromValue, boolean includeFrom, long toValue, boolean includeTo) throws StorageClientException {
        numberFilters.put(field, new RangeFilter<Long>(fromValue,
                includeFrom ? AbstractFilter.OPERATOR_GREATER_OR_EQUALS : AbstractFilter.OPERATOR_GREATER,
                toValue,
                includeTo ? AbstractFilter.OPERATOR_LESS_OR_EQUALS : AbstractFilter.OPERATOR_LESS));
        return this;
    }

    public FindFilter keyBetween(DateField field, Date fromValue, Date toValue) throws StorageClientException {
        return keyBetween(field, fromValue, true, toValue, true);
    }

    public FindFilter keyBetween(DateField field, Date fromValue, boolean includeFrom, Date toValue, boolean includeTo) throws StorageClientException {
        dateFilters.put(field, new RangeFilter<Date>(fromValue,
                includeFrom ? AbstractFilter.OPERATOR_GREATER_OR_EQUALS : AbstractFilter.OPERATOR_GREATER,
                toValue,
                includeTo ? AbstractFilter.OPERATOR_LESS_OR_EQUALS : AbstractFilter.OPERATOR_LESS));
        return this;
    }

    public FindFilter searchKeysLike(String value) throws StorageClientException {
        if (value != null) {
            boolean isInvalidLength = value.length() < SEARCH_KEYS_MIN_LENGTH || value.length() > SEARCH_KEYS_MAX_LENGTH;
            HELPER.check(StorageClientException.class, isInvalidLength, MSG_ERR_SEARCH_KEYS_LEN);
            validateStringFilters(null, value);
        }
        searchKeys = value;
        return this;
    }

    public FindFilter sortBy(SortField field, SortOrder order) throws StorageClientException {
        HELPER.check(StorageClientException.class, field == null, MSG_ERR_NULL_SORT_FIELD);
        HELPER.check(StorageClientException.class, order == null, MSG_ERR_NULL_SORT_ORDER);
        for (SortingParam param : sortingList) {
            boolean alreadyInSorting = param.getField().equals(field);
            HELPER.check(StorageClientException.class, alreadyInSorting, MSG_ERR_DUPL_SORT_FIELD, field);
        }
        sortingList.add(new SortingParam(field, order));
        return this;
    }

    private void validateStringFilters(StringField field, String searchKeysValue) throws StorageClientException {
        if (searchKeysValue == null) {
            return;
        }
        if (field != null) {
            HELPER.check(StorageClientException.class, NON_HASHED_KEY_LIST.contains(field), MSG_ERR_SEARCH_KEYS_COMBINATION);
            return;
        }
        for (StringField key : stringFilters.keySet()) {
            HELPER.check(StorageClientException.class, NON_HASHED_KEY_LIST.contains(key), MSG_ERR_SEARCH_KEYS_COMBINATION);
        }
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public List<SortingParam> getSortingList() {
        return sortingList;
    }

    public Map<StringField, AbstractFilter> getStringFilters() {
        return stringFilters;
    }

    public Map<NumberField, AbstractFilter> getNumberFilters() {
        return numberFilters;
    }

    public Map<DateField, AbstractFilter> getDateFilters() {
        return dateFilters;
    }

    public String getSearchKeys() {
        return searchKeys;
    }

    public FindFilter copy() {
        FindFilter newFilter = new FindFilter();
        newFilter.limit = limit;
        newFilter.offset = offset;
        newFilter.searchKeys = searchKeys;
        newFilter.sortingList.addAll(sortingList);
        newFilter.numberFilters.putAll(numberFilters);
        newFilter.stringFilters.putAll(stringFilters);
        newFilter.dateFilters.putAll(dateFilters);
        return newFilter;
    }

    public static boolean nonHashedKeysListContains(StringField key) {
        return NON_HASHED_KEY_LIST.contains(key);
    }

    @Override
    public String toString() {
        return "FindFilter{" +
                "stringFilters=" + stringFilters +
                ", numberFilters=" + numberFilters +
                ", dateFilters=" + dateFilters +
                ", searchKeys=" + searchKeys +
                ", limit=" + limit +
                ", offset=" + offset +
                ", sorting=" + sortingList +
                '}';
    }
}

