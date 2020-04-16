package com.incountry.residence.sdk.dto;

import com.incountry.residence.sdk.tools.exceptions.RecordException;

import java.util.List;

public class BatchRecord {
    private int count;
    private int limit;
    private int offset;
    private int total;
    private List<Record> records;
    private List<RecordException> errors;

    public BatchRecord(List<Record> records, int count, int limit, int offset, int total, List<RecordException> errors) {
        this.count = count;
        this.limit = limit;
        this.offset = offset;
        this.total = total;
        this.records = records;
        this.errors = errors;
    }

    public int getCount() {
        return count;
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

    public List<Record> getRecords() {
        return records;
    }

    public List<RecordException> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "BatchRecord{" +
                "count=" + count +
                ", limit=" + limit +
                ", offset=" + offset +
                ", total=" + total +
                ", records=" + toString(records) +
                ", errors=" + errors +
                '}';
    }

    public static String toString(List<Record> records) {
        if (records == null || records.isEmpty()) {
            return "[]";
        }
        StringBuilder result = new StringBuilder("[");
        records.forEach(one -> {
            if (result.length() > 1) {
                result.append(", ");
            }
            result.append("SECURE[")
                    .append(one.hashCode())
                    .append("]");
        });
        return result.append("]").toString();
    }
}
