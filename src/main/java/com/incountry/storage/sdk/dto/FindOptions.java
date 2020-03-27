package com.incountry.storage.sdk.dto;

public class FindOptions {
    private static final int MAX_LIMIT = 100;

    private int limit;
    private int offset;

    /**
     * Constructor for FindOptions class
     */
    public FindOptions() {
        this(MAX_LIMIT, 0);
    }

    /**
     * Constructor for FindOptions class with the specified find options
     *
     * @param limit  the number of records to return
     * @param offset number of records which on will the result be offset
     */
    public FindOptions(int limit, int offset) {
        if (limit > MAX_LIMIT) {
            throw new IllegalArgumentException(String.format("Max limit is %l. Use offset to populate more", MAX_LIMIT));
        }
        if (limit < 1) {
            throw new IllegalArgumentException(String.format("Limit must be more than 0"));
        }
        this.limit = limit;
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "FindOptions{" +
                "limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
