package com.incountry.residence.sdk.tools.transfer;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

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
        public int limit;
        public int offset;
        public int total;
        public int count;
    }
}