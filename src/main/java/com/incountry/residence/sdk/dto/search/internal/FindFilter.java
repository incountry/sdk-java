package com.incountry.residence.sdk.dto.search.internal;

import com.incountry.residence.sdk.dto.search.RecordField;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private final Map<RecordField, Object> filterMap = new HashMap<>();
    private final List<SortingParam> sortingList = new ArrayList<>();

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

    public <T extends Enum<T> & RecordField> void setFilter(T field, Object param) {
        filterMap.put(field, param);
    }

    public void addSorting(SortingParam param) {
        sortingList.add(param);
    }

    public Map<RecordField, Object> getFilterMap() {
        return filterMap;
    }

    public List<SortingParam> getSortingList() {
        return sortingList;
    }

    public FindFilter copy() throws StorageClientException {
        FindFilter clone = new FindFilter();
        clone.filterMap.putAll(this.filterMap);
        clone.setOffset(this.getOffset());
        clone.setLimit(this.getLimit());
        clone.sortingList.addAll(this.sortingList);
        return clone;
    }

    @Override
    public String toString() {
        return "FindFilter{" +
                "filterMap=" + filterMap +
                ", limit=" + limit +
                ", offset=" + offset +
                ", sorting=" + sortingList +
                '}';
    }
}

