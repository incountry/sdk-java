package com.incountry.residence.sdk.dto.search;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Container for filters to searching of stored data by param values
 */
public class FindFilter {
    private static final Logger LOG = LogManager.getLogger(FindFilter.class);

    public static final int MAX_LIMIT = 100;
    public static final int DEF_OFFSET = 0;
    private static final String MSG_MAX_LIMIT = "Max limit is %d. Use offset to populate more";
    private static final String MSG_NEG_LIMIT = "Limit must be more than 1";
    private static final String MSG_NEG_OFFSET = "Offset must be more than 0";

    private FilterStringParam recordKeyFilter;
    private FilterStringParam key1Filter;
    private FilterStringParam key2Filter;
    private FilterStringParam key3Filter;
    private FilterStringParam key4Filter;
    private FilterStringParam key5Filter;
    private FilterStringParam key6Filter;
    private FilterStringParam key7Filter;
    private FilterStringParam key8Filter;
    private FilterStringParam key9Filter;
    private FilterStringParam key10Filter;
    private FilterStringParam errorCorrectionKey1Filter;
    private FilterStringParam errorCorrectionKey2Filter;
    private FilterStringParam profileKeyFilter;
    private FilterNumberParam rangeKey1Filter;
    private FilterNumberParam rangeKey2Filter;
    private FilterNumberParam rangeKey3Filter;
    private FilterNumberParam rangeKey4Filter;
    private FilterNumberParam rangeKey5Filter;
    private FilterNumberParam rangeKey6Filter;
    private FilterNumberParam rangeKey7Filter;
    private FilterNumberParam rangeKey8Filter;
    private FilterNumberParam rangeKey9Filter;
    private FilterNumberParam rangeKey10Filter;
    private FilterStringParam versionFilter;

    private int limit = MAX_LIMIT;
    private int offset = DEF_OFFSET;

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

    public FilterStringParam getRecordKeyFilter() {
        return recordKeyFilter;
    }

    public void setRecordKeyFilter(FilterStringParam recordKeyFilter) {
        this.recordKeyFilter = recordKeyFilter;
    }

    public FilterStringParam getKey1Filter() {
        return key1Filter;
    }

    public void setKey1Filter(FilterStringParam key1Filter) {
        this.key1Filter = key1Filter;
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

    public FilterStringParam getKey4Filter() {
        return key4Filter;
    }

    public void setKey4Filter(FilterStringParam key4Filter) {
        this.key4Filter = key4Filter;
    }

    public FilterStringParam getKey5Filter() {
        return key5Filter;
    }

    public void setKey5Filter(FilterStringParam key5Filter) {
        this.key5Filter = key5Filter;
    }

    public FilterStringParam getKey6Filter() {
        return key6Filter;
    }

    public void setKey6Filter(FilterStringParam key6Filter) {
        this.key6Filter = key6Filter;
    }

    public FilterStringParam getKey7Filter() {
        return key7Filter;
    }

    public void setKey7Filter(FilterStringParam key7Filter) {
        this.key7Filter = key7Filter;
    }

    public FilterStringParam getKey8Filter() {
        return key8Filter;
    }

    public void setKey8Filter(FilterStringParam key8Filter) {
        this.key8Filter = key8Filter;
    }

    public FilterStringParam getKey9Filter() {
        return key9Filter;
    }

    public void setKey9Filter(FilterStringParam key9Filter) {
        this.key9Filter = key9Filter;
    }

    public FilterStringParam getKey10Filter() {
        return key10Filter;
    }

    public void setKey10Filter(FilterStringParam key10Filter) {
        this.key10Filter = key10Filter;
    }

    public FilterStringParam getProfileKeyFilter() {
        return profileKeyFilter;
    }

    public void setProfileKeyFilter(FilterStringParam profileKeyFilter) {
        this.profileKeyFilter = profileKeyFilter;
    }

    public FilterNumberParam getRangeKey1Filter() {
        return rangeKey1Filter;
    }

    public FilterStringParam getErrorCorrectionKey1Filter() {
        return errorCorrectionKey1Filter;
    }

    public void setErrorCorrectionKey1Filter(FilterStringParam errorCorrectionKey1Filter) {
        this.errorCorrectionKey1Filter = errorCorrectionKey1Filter;
    }

    public FilterStringParam getErrorCorrectionKey2Filter() {
        return errorCorrectionKey2Filter;
    }

    public void setErrorCorrectionKey2Filter(FilterStringParam errorCorrectionKey2Filter) {
        this.errorCorrectionKey2Filter = errorCorrectionKey2Filter;
    }

    public FilterNumberParam getRangeKey2Filter() {
        return rangeKey2Filter;
    }

    public void setRangeKey2Filter(FilterNumberParam rangeKey2Filter) {
        this.rangeKey2Filter = rangeKey2Filter;
    }

    public FilterNumberParam getRangeKey3Filter() {
        return rangeKey3Filter;
    }

    public void setRangeKey3Filter(FilterNumberParam rangeKey3Filter) {
        this.rangeKey3Filter = rangeKey3Filter;
    }

    public FilterNumberParam getRangeKey4Filter() {
        return rangeKey4Filter;
    }

    public void setRangeKey4Filter(FilterNumberParam rangeKey4Filter) {
        this.rangeKey4Filter = rangeKey4Filter;
    }

    public FilterNumberParam getRangeKey5Filter() {
        return rangeKey5Filter;
    }

    public void setRangeKey5Filter(FilterNumberParam rangeKey5Filter) {
        this.rangeKey5Filter = rangeKey5Filter;
    }

    public FilterNumberParam getRangeKey6Filter() {
        return rangeKey6Filter;
    }

    public void setRangeKey6Filter(FilterNumberParam rangeKey6Filter) {
        this.rangeKey6Filter = rangeKey6Filter;
    }

    public FilterNumberParam getRangeKey7Filter() {
        return rangeKey7Filter;
    }

    public void setRangeKey7Filter(FilterNumberParam rangeKey7Filter) {
        this.rangeKey7Filter = rangeKey7Filter;
    }

    public FilterNumberParam getRangeKey8Filter() {
        return rangeKey8Filter;
    }

    public void setRangeKey8Filter(FilterNumberParam rangeKey8Filter) {
        this.rangeKey8Filter = rangeKey8Filter;
    }

    public FilterNumberParam getRangeKey9Filter() {
        return rangeKey9Filter;
    }

    public void setRangeKey9Filter(FilterNumberParam rangeKey9Filter) {
        this.rangeKey9Filter = rangeKey9Filter;
    }

    public FilterNumberParam getRangeKey10Filter() {
        return rangeKey10Filter;
    }

    public void setRangeKey10Filter(FilterNumberParam rangeKey10Filter) {
        this.rangeKey10Filter = rangeKey10Filter;
    }

    public void setRangeKey1Filter(FilterNumberParam rangeKey1Filter) {
        this.rangeKey1Filter = rangeKey1Filter;
    }

    public FilterStringParam getVersionFilter() {
        return versionFilter;
    }

    public void setVersionFilter(FilterStringParam versionFilter) {
        this.versionFilter = versionFilter;
    }

    public FindFilter copy() throws StorageClientException {
        FindFilter clone = new FindFilter();
        clone.setRecordKeyFilter(getRecordKeyFilter() != null ? getRecordKeyFilter().copy() : null);
        cloneKeyFilters(clone);
        cloneRangeKeyFilters(clone);
        clone.setErrorCorrectionKey1Filter(getErrorCorrectionKey1Filter() != null ? getErrorCorrectionKey1Filter().copy() : null);
        clone.setErrorCorrectionKey2Filter(getErrorCorrectionKey2Filter() != null ? getErrorCorrectionKey2Filter().copy() : null);
        clone.setProfileKeyFilter(getProfileKeyFilter() != null ? getProfileKeyFilter().copy() : null);
        clone.setVersionFilter(getVersionFilter() != null ? getVersionFilter().copy() : null);
        clone.setOffset(getOffset());
        clone.setLimit(getLimit());
        return clone;
    }

    private void cloneKeyFilters(FindFilter clone) {
        clone.setKey1Filter(getKey1Filter() != null ? getKey1Filter().copy() : null);
        clone.setKey2Filter(getKey2Filter() != null ? getKey2Filter().copy() : null);
        clone.setKey3Filter(getKey3Filter() != null ? getKey3Filter().copy() : null);
        clone.setKey4Filter(getKey4Filter() != null ? getKey4Filter().copy() : null);
        clone.setKey5Filter(getKey5Filter() != null ? getKey5Filter().copy() : null);
        clone.setKey6Filter(getKey6Filter() != null ? getKey6Filter().copy() : null);
        clone.setKey7Filter(getKey7Filter() != null ? getKey7Filter().copy() : null);
        clone.setKey8Filter(getKey8Filter() != null ? getKey8Filter().copy() : null);
        clone.setKey9Filter(getKey9Filter() != null ? getKey9Filter().copy() : null);
        clone.setKey10Filter(getKey10Filter() != null ? getKey10Filter().copy() : null);
    }

    private void cloneRangeKeyFilters(FindFilter clone) {
        clone.setRangeKey1Filter(getRangeKey1Filter() != null ? getRangeKey1Filter().copy() : null);
        clone.setRangeKey2Filter(getRangeKey2Filter() != null ? getRangeKey2Filter().copy() : null);
        clone.setRangeKey3Filter(getRangeKey3Filter() != null ? getRangeKey3Filter().copy() : null);
        clone.setRangeKey4Filter(getRangeKey4Filter() != null ? getRangeKey4Filter().copy() : null);
        clone.setRangeKey5Filter(getRangeKey5Filter() != null ? getRangeKey5Filter().copy() : null);
        clone.setRangeKey6Filter(getRangeKey6Filter() != null ? getRangeKey6Filter().copy() : null);
        clone.setRangeKey7Filter(getRangeKey7Filter() != null ? getRangeKey7Filter().copy() : null);
        clone.setRangeKey8Filter(getRangeKey8Filter() != null ? getRangeKey8Filter().copy() : null);
        clone.setRangeKey9Filter(getRangeKey9Filter() != null ? getRangeKey9Filter().copy() : null);
        clone.setRangeKey10Filter(getRangeKey10Filter() != null ? getRangeKey10Filter().copy() : null);
    }

    @Override
    public String toString() {
        return "FindFilter{" +
                "recordKeyFilter=" + recordKeyFilter +
                ", key1Filter=" + key1Filter +
                ", key2Filter=" + key2Filter +
                ", key3Filter=" + key3Filter +
                ", key4Filter=" + key4Filter +
                ", key5Filter=" + key5Filter +
                ", key6Filter=" + key6Filter +
                ", key7Filter=" + key7Filter +
                ", key8Filter=" + key8Filter +
                ", key9Filter=" + key9Filter +
                ", key10Filter=" + key10Filter +
                ", errorCorrectionKey1Filter=" + errorCorrectionKey1Filter +
                ", errorCorrectionKey2Filter=" + errorCorrectionKey2Filter +
                ", profileKeyFilter=" + profileKeyFilter +
                ", rangeKey1Filter=" + rangeKey1Filter +
                ", rangeKey2Filter=" + rangeKey2Filter +
                ", rangeKey3Filter=" + rangeKey3Filter +
                ", rangeKey4Filter=" + rangeKey4Filter +
                ", rangeKey5Filter=" + rangeKey5Filter +
                ", rangeKey6Filter=" + rangeKey6Filter +
                ", rangeKey7Filter=" + rangeKey7Filter +
                ", rangeKey8Filter=" + rangeKey8Filter +
                ", rangeKey9Filter=" + rangeKey9Filter +
                ", rangeKey10Filter=" + rangeKey10Filter +
                ", versionFilter=" + versionFilter +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
