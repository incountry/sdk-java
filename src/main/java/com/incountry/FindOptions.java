package com.incountry;

import com.incountry.exceptions.FindOptionsException;
import org.json.JSONObject;

public class FindOptions {
    private static final int MAX_LIMIT = 100;
    int limit;
    int offset;

    public FindOptions() throws FindOptionsException {
        this(MAX_LIMIT, 0);
    }

    public FindOptions(int limit, int offset) throws FindOptionsException {
        if (limit> MAX_LIMIT) throw new FindOptionsException(String.format("Max limit is %l. Use offset to populate more", MAX_LIMIT));
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

    public JSONObject toJSONObject() {
        return new JSONObject()
            .put("limit", limit)
            .put("offset", offset);
    }
}
