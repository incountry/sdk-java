package com.incountry.residence.sdk.tools.transfer;

import java.util.List;

public class TransferFindResult {
    private FindMeta meta;
    private List<TransferRecord> data;

    public FindMeta getMeta() {
        return meta;
    }

    public void setMeta(FindMeta meta) {
        this.meta = meta;
    }

    public List<TransferRecord> getData() {
        return data;
    }

    public void setData(List<TransferRecord> data) {
        this.data = data;
    }

    public static class FindMeta {
        private int limit;
        private int offset;
        private int total;
        private int count;

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

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}