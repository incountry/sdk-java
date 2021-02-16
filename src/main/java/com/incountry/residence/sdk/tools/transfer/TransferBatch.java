package com.incountry.residence.sdk.tools.transfer;

import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.util.List;

public class TransferBatch {

    private static final String MSG_ERR_NULL_META = "Response error: Meta is null";
    private static final String MSG_ERR_NEGATIVE_META = "Response error: negative values in batch metadata";
    private static final String MSG_ERR_INCORRECT_COUNT = "Response error: count in batch metadata differs from data size";
    private static final String MSG_ERR_INCORRECT_TOTAL = "Response error: incorrect total in batch metadata, less then received";

    FindResult meta;
    List<TransferRecord> data;

    public void validate() throws StorageServerException {
        if (meta == null) {
            throw new StorageServerException(MSG_ERR_NULL_META);
        }
        boolean negativeNumber = meta.getCount() < 0 || meta.getLimit() < 0 || meta.getOffset() < 0 || meta.getTotal() < 0;
        boolean positiveMetaEmptyData = meta.getCount() > 0 && (data == null || data.isEmpty() || data.size() != meta.getCount());
        boolean zeroMetaNonEmptyData = meta.getCount() == 0 && data != null && !data.isEmpty();
        if (negativeNumber) {
            throw new StorageServerException(MSG_ERR_NEGATIVE_META);
        } else if (positiveMetaEmptyData || zeroMetaNonEmptyData) {
            throw new StorageServerException(MSG_ERR_INCORRECT_COUNT);
        } else if (meta.getCount() > meta.getTotal()) {
            throw new StorageServerException(MSG_ERR_INCORRECT_TOTAL);
        }
    }

    public FindResult getMeta() {
        return meta;
    }

    public void setMeta(FindResult meta) {
        this.meta = meta;
    }

    public List<TransferRecord> getData() {
        return data;
    }

    public void setData(List<TransferRecord> data) {
        this.data = data;
    }
}