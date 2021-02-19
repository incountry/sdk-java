package com.incountry.residence.sdk.tools.transfer;

import com.incountry.residence.sdk.dto.Record;

public class TransferRecord extends Record {

    //for backwards compatibility
    private String key;
    private boolean isEncrypted;


    public TransferRecord(String recordKey) {
        super(recordKey);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public TransferRecord setVersion(Integer version) {
        super.version=version;
        return this;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public TransferRecord setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
        return this;
    }
}
