package com.incountry.residence.sdk.dto.search.filters;

import com.incountry.residence.sdk.dto.search.fields.SortField;
import com.incountry.residence.sdk.dto.search.SortOrder;
import com.incountry.residence.sdk.dto.search.SortingParam;
import com.incountry.residence.sdk.dto.search.fields.NumberField;
import com.incountry.residence.sdk.dto.search.fields.StringField;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class FindFilter {

    private static final Logger LOG = LogManager.getLogger(FindFilter.class);

    private static final String MSG_ERR_SEARCH_KEYS_LEN = "SEARCH_KEYS should contain at least 3 characters and be not longer than 200";
    private static final String MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS = "SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY10 lookup";
    private static final String MSG_ERR_LIMIT = "Illegal limit at filter. Expected value must be in range [1..%s], but was %s";
    private static final String MSG_ERR_OFFSET = "Offset must be more than 0";
    private static final String MSG_ERR_SORTING = "Field %s is already in sorting list";

    public static final int MAX_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 0;
    private static final int SEARCH_KEYS_MIN_LENGTH = 3;
    private static final int SEARCH_KEYS_MAX_LENGTH = 200;

    private final EnumMap<StringField, Filter> stringFilters = new EnumMap<>(StringField.class);
    private final EnumMap<NumberField, Filter> numberFilters = new EnumMap<>(NumberField.class);
    private static List<StringField> nonHashedKeyList = new ArrayList<>();
    private List<SortingParam> sortingList = new ArrayList<>();

    private int limit = MAX_LIMIT;
    private int offset = DEFAULT_OFFSET;
    private String searchKeys;


    static {
        nonHashedKeyList.add(StringField.KEY1);
        nonHashedKeyList.add(StringField.KEY2);
        nonHashedKeyList.add(StringField.KEY3);
        nonHashedKeyList.add(StringField.KEY4);
        nonHashedKeyList.add(StringField.KEY5);
        nonHashedKeyList.add(StringField.KEY6);
        nonHashedKeyList.add(StringField.KEY7);
        nonHashedKeyList.add(StringField.KEY8);
        nonHashedKeyList.add(StringField.KEY9);
        nonHashedKeyList.add(StringField.KEY10);
    }

    private FindFilter() {
    }

    public static FindFilter create() {
        return new FindFilter();
    }

    public FindFilter clear() {
        stringFilters.clear();
        numberFilters.clear();
        limit = MAX_LIMIT;
        offset = DEFAULT_OFFSET;
        searchKeys = null;
        sortingList.clear();
        return this;
    }

    public FindFilter limitAndOffset(int limit, int offset) throws StorageClientException {
        if (limit > MAX_LIMIT || limit < 1) {
            String message = String.format(MSG_ERR_LIMIT, MAX_LIMIT, limit);
            LOG.error(message);
            throw new StorageClientException(message);
        }
        if (offset < 0) {
            LOG.error(MSG_ERR_OFFSET);
            throw new StorageClientException(MSG_ERR_OFFSET);
        }
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    public FindFilter keyEq(StringField field, String... values) throws StorageClientException {
        validateStringFilters(field, searchKeys);
        stringFilters.put(field, new StringFilter(Arrays.asList(values)));
        return this;
    }

    public FindFilter keyEq(NumberField field, Long... values) throws StorageClientException {
        numberFilters.put(field, new NumberFilter(Arrays.asList(values)));
        return this;
    }

    public FindFilter keyNotEq(StringField field, String... values) throws StorageClientException {
        validateStringFilters(field, searchKeys);
        stringFilters.put(field, new StringFilter(Arrays.asList(values), true));
        return this;
    }

    public FindFilter keyNotEq(NumberField field, Long... values) throws StorageClientException {
        numberFilters.put(field, new NumberFilter(Arrays.asList(values), Filter.OPERATOR_NOT));
        return this;
    }

    public FindFilter keyGreater(NumberField field, Long value) throws StorageClientException {
        return keyGreater(field, value, true);
    }

    public FindFilter keyGreater(NumberField field, Long value, Boolean includingValue) throws StorageClientException {
        List<Long> list = new ArrayList<>();
        list.add(value);
        numberFilters.put(field, new NumberFilter(list, Boolean.TRUE.equals(includingValue) ? Filter.OPERATOR_GREATER_OR_EQUALS : Filter.OPERATOR_GREATER));
        return this;
    }

    public FindFilter keyLess(NumberField field, long value) throws StorageClientException {
        return keyLess(field, value, true);
    }

    public FindFilter keyLess(NumberField field, long value, Boolean includingValue) throws StorageClientException {
        List<Long> list = new ArrayList<>();
        list.add(value);
        numberFilters.put(field, new NumberFilter(list, Boolean.TRUE.equals(includingValue) ? Filter.OPERATOR_LESS_OR_EQUALS : Filter.OPERATOR_LESS));
        return this;
    }

    public FindFilter keyBetween(NumberField field, long fromValue, long toValue) throws StorageClientException {
        return keyBetween(field, fromValue, toValue, true, true);
    }

    public FindFilter keyBetween(NumberField field, long fromValue, long toValue, Boolean includeFrom) throws StorageClientException {
        return keyBetween(field, fromValue, toValue, includeFrom, true);
    }

    public FindFilter keyBetween(NumberField field, long fromValue, long toValue, Boolean includeFrom, Boolean includeTo) throws StorageClientException {
        RangeFilter rangeFilter = new RangeFilter(fromValue,
                Boolean.TRUE.equals(includeFrom) ? Filter.OPERATOR_GREATER_OR_EQUALS : Filter.OPERATOR_GREATER,
                toValue,
                Boolean.TRUE.equals(includeTo) ? Filter.OPERATOR_LESS_OR_EQUALS : Filter.OPERATOR_LESS
        );
        numberFilters.put(field, rangeFilter);
        return this;
    }

    public FindFilter searchKeysLike(String value) throws StorageClientException {
        if (value != null) {
            if (value.length() < SEARCH_KEYS_MIN_LENGTH || value.length() > SEARCH_KEYS_MAX_LENGTH) {
                LOG.error(MSG_ERR_SEARCH_KEYS_LEN);
                throw new StorageClientException(MSG_ERR_SEARCH_KEYS_LEN);
            }
            validateStringFilters(null, value);
        }

        searchKeys = value;
        return this;
    }

    public FindFilter sortBy(SortField field, SortOrder order) throws StorageClientException {
        for (SortingParam param : sortingList) {
            if (param.getField().equals(field)) {
                String message = String.format(MSG_ERR_SORTING, field);
                LOG.error(message);
                throw new StorageClientException(message);
            }
        }
        sortingList.add(new SortingParam(field, order));
        return this;
    }

    public FindFilter nullable(NumberField field) {
        return nullable(field, true);
    }

    public FindFilter nullable(NumberField field, boolean isNull) {
        numberFilters.put(field,  new NullFilter(isNull));
        return this;
    }

    public FindFilter nullable(StringField field) {
        return nullable(field, true);
    }

    public FindFilter nullable(StringField field, boolean isNull) {
        stringFilters.put(field, new NullFilter(isNull));
        return this;
    }

    private void validateStringFilters(StringField field, String searchKeysValue) throws StorageClientException {
        if (searchKeysValue == null) {
            return;
        }

        if (field != null) {
            if (nonHashedKeyList.contains(field)) {
                LOG.error(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
                throw new StorageClientException(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
            }
            return;
        }
        for (StringField key : stringFilters.keySet()) {
            if (nonHashedKeyList.contains(key)) {
                LOG.error(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
                throw new StorageClientException(MSG_ERR_KEY1_KEY10_AND_SEARCH_KEYS);
            }
        }
    }

    public int getLimit() {
        return limit;
    }

    private void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    public String getSearchKeys() {
        return searchKeys;
    }

    public Map<StringField, Filter> getStringFilters() {
        return stringFilters;
    }

    public Map<NumberField, Filter> getNumberFilters() {
        return numberFilters;
    }

    public static List<StringField> getNonHashedKeyList() {
        return nonHashedKeyList;
    }

    public List<SortingParam> getSortingList() {
        return sortingList;
    }

    public FindFilter copy() {
        FindFilter clone = new FindFilter();
        clone.stringFilters.putAll(this.stringFilters);
        clone.numberFilters.putAll(this.numberFilters);
        clone.setOffset(this.getOffset());
        clone.setLimit(this.getLimit());
        clone.sortingList.addAll(sortingList);
        clone.searchKeys = searchKeys;
        return clone;
    }

    @Override
    public String toString() {
        return "FindFilter{" +
                "stringFilterMap=" + stringFilters +
                ", numberFilterMap=" + numberFilters +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
