package com.incountry.residence.sdk.tools.transfer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.Map;

public class TransferFilterContainer {

    private Map<String, Object> filter;
    private TransferFindOptions options;

    public TransferFilterContainer(Map<String, Object> filter, long limit, long offset, List<Map<String, String>> sort) {
        this.filter = filter;
        this.options = new TransferFindOptions(limit, offset, sort);
    }

    @SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC")
    public class TransferFindOptions {
        private long limit;
        private long offset;

        private List<Map<String, String>> sort;

        public TransferFindOptions(long limit, long offset, List<Map<String, String>> sort) {
            this.limit = limit;
            this.offset = offset;
            if (sort != null && !sort.isEmpty()) {
                this.sort = sort;
            }
        }

        public long getLimit() {
            return limit;
        }

        public long getOffset() {
            return offset;
        }

        public List<Map<String, String>> getSort() {
            return sort;
        }
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public TransferFindOptions getOptions() {
        return options;
    }
}
