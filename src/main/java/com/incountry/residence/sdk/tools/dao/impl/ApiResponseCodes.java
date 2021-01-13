package com.incountry.residence.sdk.tools.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * container for response codes params
 */
public class ApiResponseCodes {
    public static final Map<Integer, ApiResponseCodes> COUNTRY;
    public static final Map<Integer, ApiResponseCodes> READ;
    public static final Map<Integer, ApiResponseCodes> WRITE;
    public static final Map<Integer, ApiResponseCodes> BATCH_WRITE;
    public static final Map<Integer, ApiResponseCodes> FIND;
    public static final Map<Integer, ApiResponseCodes> DELETE;
    public static final Map<Integer, ApiResponseCodes> ADD_ATTACHMENT;
    public static final Map<Integer, ApiResponseCodes> DELETE_ATTACHMENT;
    public static final Map<Integer, ApiResponseCodes> GET_ATTACHMENT_FILE;
    public static final Map<Integer, ApiResponseCodes> UPDATE_ATTACHMENT_META;
    public static final Map<Integer, ApiResponseCodes> GET_ATTACHMENT_META;

    static {
        Map<Integer, ApiResponseCodes> map = new HashMap<>();
        map.put(200, new ApiResponseCodes(false, false));
        map.put(400, new ApiResponseCodes(true, false));
        map.put(401, new ApiResponseCodes(true, false, true));
        COUNTRY = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(200, new ApiResponseCodes(false, false));
        map.put(404, new ApiResponseCodes(true, true));
        map.put(401, new ApiResponseCodes(true, false, true));
        READ = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(201, new ApiResponseCodes(false, false));
        map.put(404, new ApiResponseCodes(true, false));
        map.put(401, new ApiResponseCodes(true, false, true));
        WRITE = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(201, new ApiResponseCodes(false, false));
        map.put(400, new ApiResponseCodes(true, false));
        map.put(401, new ApiResponseCodes(true, false, true));
        BATCH_WRITE = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(200, new ApiResponseCodes(false, false));
        map.put(400, new ApiResponseCodes(true, false));
        map.put(409, new ApiResponseCodes(true, false));
        map.put(401, new ApiResponseCodes(true, false, true));
        FIND = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(200, new ApiResponseCodes(false, false));
        map.put(400, new ApiResponseCodes(true, false));
        map.put(401, new ApiResponseCodes(true, false, true));
        DELETE = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(201, new ApiResponseCodes(false, false));
        map.put(404, new ApiResponseCodes(true, false));
        map.put(401, new ApiResponseCodes(true, false, true));
        map.put(409, new ApiResponseCodes(true, false));
        ADD_ATTACHMENT = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(204, new ApiResponseCodes(false, false));
        map.put(400, new ApiResponseCodes(true, false));
        map.put(401, new ApiResponseCodes(true, false, true));
        DELETE_ATTACHMENT = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(200, new ApiResponseCodes(false, false));
        map.put(404, new ApiResponseCodes(true, true));
        map.put(401, new ApiResponseCodes(true, false, true));
        GET_ATTACHMENT_FILE = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(200, new ApiResponseCodes(false, false));
        map.put(404, new ApiResponseCodes(true, false));
        map.put(401, new ApiResponseCodes(true, false, true));
        UPDATE_ATTACHMENT_META = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(200, new ApiResponseCodes(false, false));
        map.put(404, new ApiResponseCodes(true, true));
        map.put(401, new ApiResponseCodes(true, false, true));
        GET_ATTACHMENT_META = Collections.unmodifiableMap(map);
    }

    private final boolean error;
    private final boolean ignored;
    private final boolean canRetry;

    public ApiResponseCodes(boolean error, boolean ignored) {
        this.error = error;
        this.ignored = ignored;
        this.canRetry = false;
    }

    public ApiResponseCodes(boolean error, boolean ignored, boolean canRetry) {
        this.error = error;
        this.ignored = ignored;
        this.canRetry = canRetry;
    }

    public boolean isError() {
        return error;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public boolean isCanRetry() {
        return canRetry;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "error=" + error +
                ", ignored=" + ignored +
                ", canRetry=" + canRetry +
                '}';
    }
}
