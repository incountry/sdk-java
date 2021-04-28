package com.incountry.residence.sdk.tools.transfer;

import java.util.List;

public class TransferRecordList {
    private List<TransferRecord> records;

    public TransferRecordList(List<TransferRecord> records) {
        this.records = records;
    }

    public List<TransferRecord> getRecords() {
        return records;
    }
}
