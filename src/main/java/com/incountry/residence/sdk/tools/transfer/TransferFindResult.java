package com.incountry.residence.sdk.tools.transfer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

public class TransferFindResult {

    private List<TransferRecord> data;
    private FindMeta meta;

    public TransferFindResult(List<TransferRecord> data, FindMeta meta) {
        this.data = data;
        this.meta = meta;
    }

    public List<TransferRecord> getData() {
        return data;
    }

    public FindMeta getMeta() {
        return meta;
    }

    @SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC")
    public class FindMeta {
        private int limit;
        private int offset;
        private int total;
        private int count;

        public FindMeta(int limit, int offset, int total, int count) {
            this.limit = limit;
            this.offset = offset;
            this.total = total;
            this.count = count;
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

        public int getCount() {
            return count;
        }
    }


}
