package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.EnumMap;
import java.util.Map;

/**
 * Container for filters to searching of stored data by param values
 */
public class FindFilter {
    private static final Logger LOG = LogManager.getLogger(FindFilter.class);

    public static final int MAX_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 0;
    private static final String MSG_MAX_LIMIT = "Max limit is %d. Use offset to populate more";
    private static final String MSG_NEG_LIMIT = "Limit must be more than 1";
    private static final String MSG_NEG_OFFSET = "Offset must be more than 0";

    private final EnumMap<StringField, FilterStringParam> stringFilterMap = new EnumMap<>(StringField.class);
    private final EnumMap<NumberField, FilterNumberParam> numberFilterMap = new EnumMap<>(NumberField.class);

    private int limit = MAX_LIMIT;
    private int offset = DEFAULT_OFFSET;

    public void setLimit(int limit) throws StorageClientException {
        if (limit > MAX_LIMIT) {
            String message = String.format(MSG_MAX_LIMIT, MAX_LIMIT);
            LOG.error(message);
            throw new StorageClientException(message);
        }
        if (limit < 1) {
            LOG.error(MSG_NEG_LIMIT);
            throw new StorageClientException(MSG_NEG_LIMIT);
        }
        this.limit = limit;
    }

    public void setOffset(int offset) throws StorageClientException {
        if (offset < 0) {
            LOG.error(MSG_NEG_OFFSET);
            throw new StorageClientException(MSG_NEG_OFFSET);
        }
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setStringFilter(StringField field, FilterStringParam param) {
        stringFilterMap.put(field, param);
    }

    public void setNumberFilter(NumberField field, FilterNumberParam param) {
        numberFilterMap.put(field, param);
    }

    public Map<StringField, FilterStringParam> getStringFilterMap() {
        return stringFilterMap;
    }

    public Map<NumberField, FilterNumberParam> getNumberFilterMap() {
        return numberFilterMap;
    }

    public FindFilter copy() throws StorageClientException {
        FindFilter clone = new FindFilter();
        clone.stringFilterMap.putAll(this.stringFilterMap);
        clone.numberFilterMap.putAll(this.numberFilterMap);
        clone.setOffset(this.getOffset());
        clone.setLimit(this.getLimit());
        return clone;
    }

    @Override
    public String toString() {
        return "FindFilter{" +
                "stringFilterMap=" + stringFilterMap +
                ", numberFilterMap=" + numberFilterMap +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}

