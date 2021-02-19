package com.incountry.residence.sdk.tools.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransferFilterContainer {
    private Map<Object, Object> filter;
    private TransferFindOptions options;

    public TransferFilterContainer(Map<Object, Object> filters, long limit, long offset, List<Map<Object, Object>> sort) {
        filter = filters;
        options = new TransferFindOptions(limit, offset, sort);
    }


    static class TransferFindOptions {
        long limit;
        long offset;
        List<Map<Object, Object>> sort;

        public TransferFindOptions(long limit, long offset, List<Map<Object, Object>> sort) {
            this.limit = limit;
            this.offset = offset;
            if (sort != null && !sort.isEmpty()) {
                this.sort = new ArrayList<>(sort);
            }
        }
    }
}
