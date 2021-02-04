package com.incountry.residence.sdk.tools.transfer;

import com.incountry.residence.sdk.dto.Record;

public class TransferRecord extends Record {

    private String key;
    private boolean is_encrypted;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
//        return this;
    }

    public boolean isEncrypted() {
        return is_encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.is_encrypted = encrypted;
    }

    public Integer getVersion() {
        return super.version;
    }

    public void setVersion(Integer version) {
        super.version = version;
    }
}
