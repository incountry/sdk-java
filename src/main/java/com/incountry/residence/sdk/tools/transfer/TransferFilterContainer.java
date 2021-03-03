package com.incountry.residence.sdk.tools.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("java:S1068")
public class TransferFilterContainer {
    private static final String CODE_LIMIT = "limit";
    private static final String CODE_OFFSET = "offset";
    private static final String CODE_SORT = "sort";

    private Map<String, Object> filter;
    private Map<String, Object> options;

    public TransferFilterContainer(Map<String, Object> filter, long limit, long offset, List<Map<String, Object>> sort) {
        this.filter = filter;
        options = new HashMap<>();
        options.put(CODE_LIMIT, limit);
        options.put(CODE_OFFSET, offset);
        if (sort != null && !sort.isEmpty()) {
            options.put(CODE_SORT, new ArrayList<>(sort));
        }
    }
}
