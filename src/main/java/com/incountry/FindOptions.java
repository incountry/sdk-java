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

    public class FindOptionsException extends Exception {
        FindOptionsException(String s){
            super(s);
        }
    }

    public FindOptions() throws FindOptionsException {
        this(MAX_LIMIT, 0);
    }

    public FindOptions(int limit, int offset) throws FindOptionsException {
        if (limit> MAX_LIMIT) throw new FindOptionsException(String.format("Max limit is %l. Use offset to populate more", MAX_LIMIT));
        this.limit = limit;
        this.offset = offset;
    }

    public JSONObject toJSONObject() {
        return new JSONObject()
            .put("limit", limit)
            .put("offset", offset);
    }
}
