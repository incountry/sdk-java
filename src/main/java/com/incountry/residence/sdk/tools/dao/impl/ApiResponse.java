package com.incountry.residence.sdk.tools.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * container for response codes params
 */
public class ApiResponse {
    public static final Map<Integer, ApiResponse> COUNTRY;
    public static final Map<Integer, ApiResponse> READ;
    public static final Map<Integer, ApiResponse> WRITE;
    public static final Map<Integer, ApiResponse> BATCH_WRITE;
    public static final Map<Integer, ApiResponse> FIND;
    public static final Map<Integer, ApiResponse> DELETE;

    static {
        Map<Integer, ApiResponse> map = new HashMap<>();
        map.put(200, new ApiResponse(false, false));
        map.put(400, new ApiResponse(true, false));
        COUNTRY = Collections.unmodifiableMap(map);
        map = new HashMap<>();
        map.put(200, new ApiResponse(false, false));
        map.put(404, new ApiResponse(true, true));
        READ = Collections.unmodifiableMap(map);
        map = new HashMap<>();
        map.put(201, new ApiResponse(false, false));
        map.put(404, new ApiResponse(true, false));
        WRITE = Collections.unmodifiableMap(map);
        map = new HashMap<>();
        map.put(201, new ApiResponse(false, false));
        map.put(400, new ApiResponse(true, false));
        BATCH_WRITE = Collections.unmodifiableMap(map);
        map = new HashMap<>();
        map.put(200, new ApiResponse(false, false));
        map.put(400, new ApiResponse(true, false));
        map.put(409, new ApiResponse(true, false));
        FIND = Collections.unmodifiableMap(map);
        map = new HashMap<>();
        map.put(200, new ApiResponse(false, false));
        map.put(400, new ApiResponse(true, false));
        DELETE = Collections.unmodifiableMap(map);
    }

    private final boolean error;
    private final boolean ignored;

    public ApiResponse(boolean error, boolean ignored) {
        this.error = error;
        this.ignored = ignored;
    }

    public boolean isError() {
        return error;
    }

    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "error=" + error +
                ", ignored=" + ignored +
                '}';
    }
}
