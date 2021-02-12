package com.incountry.residence.sdk.dto;

import com.incountry.residence.sdk.tools.exceptions.RecordException;

import java.util.List;

public class FindResult {

    private List<Record> records;
    private List<RecordException> errors;

    private int limit;
    private int offset;
    private int total;
    private int count;

    public FindResult(List<Record> records, List<RecordException> errors, int limit, int offset, int total, int count) {
        this.records = records;
        this.errors = errors;
        this.limit = limit;
        this.offset = offset;
        this.total = total;
        this.count = count;
    }

    public List<Record> getRecords() {
        return records;
    }

    public List<RecordException> getErrors() {
        return errors;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getTotal() {
        return total;
    }

    public int getCount() {
        return count;
    }
}
