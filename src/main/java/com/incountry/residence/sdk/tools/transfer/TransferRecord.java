package com.incountry.residence.sdk.tools.transfer;

import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.Record;

import java.util.List;
import java.util.Objects;

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
        super.version = version;
        return this;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public TransferRecord setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
        return this;
    }

    @Override
    public Record setAttachments(List<AttachmentMeta> attachments) {
        return super.setAttachments(attachments);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TransferRecord)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        TransferRecord that = (TransferRecord) obj;
        return isEncrypted() == that.isEncrypted() && Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getKey(), isEncrypted());
    }
}
