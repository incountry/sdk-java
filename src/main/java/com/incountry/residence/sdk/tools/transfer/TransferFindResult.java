package com.incountry.residence.sdk.tools.transfer;

import java.util.List;

public class TransferFindResult {

    public List<TransferRecord> data;
    public FindMeta meta;

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
