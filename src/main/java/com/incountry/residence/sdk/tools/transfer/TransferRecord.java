package com.incountry.residence.sdk.tools.transfer;

import com.incountry.residence.sdk.dto.Record;

public class TransferRecord extends Record {

    private String key;
    private boolean encrypted;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    @Override
    public Integer getVersion() {
        return super.version;
    }

    @Override
    public void setVersion(Integer version) {
        super.version = version;
    }
}
