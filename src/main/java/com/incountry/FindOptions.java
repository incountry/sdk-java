package com.incountry;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

public class FindOptions {
    private static final int MAX_LIMIT = 100;

    @Getter
    @Setter
    int limit;

    @Getter
    @Setter
    int offset;

    /**
     * Constructor for FindOptions class
     */
    public FindOptions() {
        this(MAX_LIMIT, 0);
    }

    /**
     * Constructor for FindOptions class with the specified find options
     * @param limit the number of records to return
     * @param offset number of records which on will the result be offset
     */
    public FindOptions(int limit, int offset) {
        if (limit > MAX_LIMIT) {
            throw new IllegalArgumentException(String.format("Max limit is %l. Use offset to populate more", MAX_LIMIT));
        }
        this.limit = limit;
        this.offset = offset;
    }

    public JSONObject toJSONObject() {
        return new JSONObject()
            .put("limit", limit)
            .put("offset", offset);
    }
}
