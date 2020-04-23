package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Container for filters to searching of stored data by param values
 */
public class FindFilter {
    private static final Logger LOG = LogManager.getLogger(FindFilter.class);

    private static final int MAX_LIMIT = 100;
    private static final String MSG_MAX_LIMIT = "Max limit is %d. Use offset to populate more";
    private static final String MSG_NEG_LIMIT = "Limit must be more than 1";
    private static final String MSG_NEG_OFFSET = "Offset must be more than 0";

    private FilterStringParam keyFilter;
    private FilterStringParam key2Filter;
    private FilterStringParam key3Filter;
    private FilterStringParam profileKeyFilter;
    private FilterNumberParam rangeKeyFilter;
    private FilterStringParam versionFilter;

    private int limit = MAX_LIMIT;
    private int offset = 0;

    public FindFilter() {
    }

    public FindFilter(FilterStringParam keyFilter, FilterStringParam key2Filter, FilterStringParam key3Filter, FilterStringParam profileKeyFilter, FilterNumberParam rangeKeyFilter, FilterStringParam versionFilter) {
        this.keyFilter = keyFilter;
        this.key2Filter = key2Filter;
        this.key3Filter = key3Filter;
        this.profileKeyFilter = profileKeyFilter;
        this.rangeKeyFilter = rangeKeyFilter;
        this.versionFilter = versionFilter;
    }

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

    public FilterStringParam getKeyFilter() {
        return keyFilter;
    }

    public void setKeyFilter(FilterStringParam keyFilter) {
        this.keyFilter = keyFilter;
    }

    public FilterStringParam getKey2Filter() {
        return key2Filter;
    }

    public void setKey2Filter(FilterStringParam key2Filter) {
        this.key2Filter = key2Filter;
    }

    public FilterStringParam getKey3Filter() {
        return key3Filter;
    }

    public void setKey3Filter(FilterStringParam key3Filter) {
        this.key3Filter = key3Filter;
    }

    public FilterStringParam getProfileKeyFilter() {
        return profileKeyFilter;
    }

    public void setProfileKeyFilter(FilterStringParam profileKeyFilter) {
        this.profileKeyFilter = profileKeyFilter;
    }

    public FilterNumberParam getRangeKeyFilter() {
        return rangeKeyFilter;
    }

    public void setRangeKeyFilter(FilterNumberParam rangeKeyFilter) {
        this.rangeKeyFilter = rangeKeyFilter;
    }

    public FilterStringParam getVersionFilter() {
        return versionFilter;
    }

    public void setVersionFilter(FilterStringParam versionFilter) {
        this.versionFilter = versionFilter;
    }

    public FindFilter copy() {
        FindFilter clone = null;
        try {
            clone = new FindFilter();
            clone.setKeyFilter(getKeyFilter() != null ? getKeyFilter().copy() : null);
            clone.setKey2Filter(getKey2Filter() != null ? getKey2Filter().copy() : null);
            clone.setKey3Filter(getKey3Filter() != null ? getKey3Filter().copy() : null);
            clone.setProfileKeyFilter(getProfileKeyFilter() != null ? getProfileKeyFilter().copy() : null);
            clone.setRangeKeyFilter(getRangeKeyFilter() != null ? getRangeKeyFilter().copy() : null);
            clone.setVersionFilter(getVersionFilter() != null ? getVersionFilter().copy() : null);
            clone.setOffset(getOffset());
            clone.setLimit(getLimit());
        } catch (StorageClientException ex) {
            LOG.error(ex);
        }
        return clone;
    }

    @Override
    public String toString() {
        return "FindFilter{" +
                "keyFilter=" + keyFilter +
                ", key2Filter=" + key2Filter +
                ", key3Filter=" + key3Filter +
                ", profileKeyFilter=" + profileKeyFilter +
                ", rangeKeyFilter=" + rangeKeyFilter +
                ", versionFilter=" + versionFilter +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
