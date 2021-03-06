package com.incountry.residence.sdk.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Record {
    private String recordKey;
    private String parentKey;
    private String key1;
    private String key2;
    private String key3;
    private String key4;
    private String key5;
    private String key6;
    private String key7;
    private String key8;
    private String key9;
    private String key10;
    private String key11;
    private String key12;
    private String key13;
    private String key14;
    private String key15;
    private String key16;
    private String key17;
    private String key18;
    private String key19;
    private String key20;
    private String profileKey;
    private Long rangeKey1;
    private Long rangeKey2;
    private Long rangeKey3;
    private Long rangeKey4;
    private Long rangeKey5;
    private Long rangeKey6;
    private Long rangeKey7;
    private Long rangeKey8;
    private Long rangeKey9;
    private Long rangeKey10;
    private String body;
    private String precommitBody;
    private String serviceKey1;
    private String serviceKey2;
    private String serviceKey3;
    private String serviceKey4;
    private String serviceKey5;
    protected Date createdAt;
    protected Date updatedAt;
    private Date expiresAt;
    protected Integer version;
    private List<AttachmentMeta> attachments = new ArrayList<>();

    /**
     * Minimalistic constructor
     *
     * @param recordKey record key
     */
    public Record(String recordKey) {
        this.recordKey = recordKey;
    }

    /**
     * Overloaded constructor
     *
     * @param recordKey record key
     * @param body      data to be stored and encrypted
     */
    public Record(String recordKey, String body) {
        this.recordKey = recordKey;
        this.body = body;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public Record setRecordKey(String recordKey) {
        this.recordKey = recordKey;
        return this;
    }

    public String getParentKey() {
        return parentKey;
    }

    public Record setParentKey(String parentKey) {
        this.parentKey = parentKey;
        return this;
    }

    public String getKey1() {
        return key1;
    }

    public Record setKey1(String key1) {
        this.key1 = key1;
        return this;
    }

    public String getKey2() {
        return key2;
    }

    public Record setKey2(String key2) {
        this.key2 = key2;
        return this;
    }

    public String getKey3() {
        return key3;
    }

    public Record setKey3(String key3) {
        this.key3 = key3;
        return this;
    }

    public String getKey4() {
        return key4;
    }

    public Record setKey4(String key4) {
        this.key4 = key4;
        return this;
    }

    public String getKey5() {
        return key5;
    }

    public Record setKey5(String key5) {
        this.key5 = key5;
        return this;
    }

    public String getKey6() {
        return key6;
    }

    public Record setKey6(String key6) {
        this.key6 = key6;
        return this;
    }

    public String getKey7() {
        return key7;
    }

    public Record setKey7(String key7) {
        this.key7 = key7;
        return this;
    }

    public String getKey8() {
        return key8;
    }

    public Record setKey8(String key8) {
        this.key8 = key8;
        return this;
    }

    public String getKey9() {
        return key9;
    }

    public Record setKey9(String key9) {
        this.key9 = key9;
        return this;
    }

    public String getKey10() {
        return key10;
    }

    public Record setKey10(String key10) {
        this.key10 = key10;
        return this;
    }

    public String getKey11() {
        return key11;
    }

    public Record setKey11(String key11) {
        this.key11 = key11;
        return this;
    }

    public String getKey12() {
        return key12;
    }

    public Record setKey12(String key12) {
        this.key12 = key12;
        return this;
    }

    public String getKey13() {
        return key13;
    }

    public Record setKey13(String key13) {
        this.key13 = key13;
        return this;
    }

    public String getKey14() {
        return key14;
    }

    public Record setKey14(String key14) {
        this.key14 = key14;
        return this;
    }

    public String getKey15() {
        return key15;
    }

    public Record setKey15(String key15) {
        this.key15 = key15;
        return this;
    }

    public String getKey16() {
        return key16;
    }

    public Record setKey16(String key16) {
        this.key16 = key16;
        return this;
    }

    public String getKey17() {
        return key17;
    }

    public Record setKey17(String key17) {
        this.key17 = key17;
        return this;
    }

    public String getKey18() {
        return key18;
    }

    public Record setKey18(String key18) {
        this.key18 = key18;
        return this;
    }

    public String getKey19() {
        return key19;
    }

    public Record setKey19(String key19) {
        this.key19 = key19;
        return this;
    }

    public String getKey20() {
        return key20;
    }

    public Record setKey20(String key20) {
        this.key20 = key20;
        return this;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public Record setProfileKey(String profileKey) {
        this.profileKey = profileKey;
        return this;
    }

    public Long getRangeKey1() {
        return rangeKey1;
    }

    public Record setRangeKey1(Long rangeKey1) {
        this.rangeKey1 = rangeKey1;
        return this;
    }

    public Long getRangeKey2() {
        return rangeKey2;
    }

    public Record setRangeKey2(Long rangeKey2) {
        this.rangeKey2 = rangeKey2;
        return this;
    }

    public Long getRangeKey3() {
        return rangeKey3;
    }

    public Record setRangeKey3(Long rangeKey3) {
        this.rangeKey3 = rangeKey3;
        return this;
    }

    public Long getRangeKey4() {
        return rangeKey4;
    }

    public Record setRangeKey4(Long rangeKey4) {
        this.rangeKey4 = rangeKey4;
        return this;
    }

    public Long getRangeKey5() {
        return rangeKey5;
    }

    public Record setRangeKey5(Long rangeKey5) {
        this.rangeKey5 = rangeKey5;
        return this;
    }

    public Long getRangeKey6() {
        return rangeKey6;
    }

    public Record setRangeKey6(Long rangeKey6) {
        this.rangeKey6 = rangeKey6;
        return this;
    }

    public Long getRangeKey7() {
        return rangeKey7;
    }

    public Record setRangeKey7(Long rangeKey7) {
        this.rangeKey7 = rangeKey7;
        return this;
    }

    public Long getRangeKey8() {
        return rangeKey8;
    }

    public Record setRangeKey8(Long rangeKey8) {
        this.rangeKey8 = rangeKey8;
        return this;
    }

    public Long getRangeKey9() {
        return rangeKey9;
    }

    public Record setRangeKey9(Long rangeKey9) {
        this.rangeKey9 = rangeKey9;
        return this;
    }

    public Long getRangeKey10() {
        return rangeKey10;
    }

    public Record setRangeKey10(Long rangeKey10) {
        this.rangeKey10 = rangeKey10;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Record setBody(String body) {
        this.body = body;
        return this;
    }

    public String getPrecommitBody() {
        return precommitBody;
    }

    public Record setPrecommitBody(String precommitBody) {
        this.precommitBody = precommitBody;
        return this;
    }

    public String getServiceKey1() {
        return serviceKey1;
    }

    public Record setServiceKey1(String serviceKey1) {
        this.serviceKey1 = serviceKey1;
        return this;
    }

    public String getServiceKey2() {
        return serviceKey2;
    }

    public Record setServiceKey2(String serviceKey2) {
        this.serviceKey2 = serviceKey2;
        return this;
    }

    public String getServiceKey3() {
        return serviceKey3;
    }

    public Record setServiceKey3(String serviceKey3) {
        this.serviceKey3 = serviceKey3;
        return this;
    }

    public String getServiceKey4() {
        return serviceKey4;
    }

    public Record setServiceKey4(String serviceKey4) {
        this.serviceKey4 = serviceKey4;
        return this;
    }

    public String getServiceKey5() {
        return serviceKey5;
    }

    public Record setServiceKey5(String serviceKey5) {
        this.serviceKey5 = serviceKey5;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }

    public Date getExpiresAt() {
        return expiresAt != null ? new Date(expiresAt.getTime()) : null;
    }

    public Record setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt != null ? new Date(expiresAt.getTime()) : null;
        return this;
    }

    public List<AttachmentMeta> getAttachments() {
        return attachments;
    }

    protected Record setAttachments(List<AttachmentMeta> attachments) {
        this.attachments = attachments;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Record otherRecord = (Record) obj;
        return Objects.equals(recordKey, otherRecord.recordKey) &&
                Objects.equals(parentKey, otherRecord.parentKey) &&
                Objects.equals(key1, otherRecord.key1) &&
                Objects.equals(key2, otherRecord.key2) &&
                Objects.equals(key3, otherRecord.key3) &&
                Objects.equals(key4, otherRecord.key4) &&
                Objects.equals(key5, otherRecord.key5) &&
                Objects.equals(key6, otherRecord.key6) &&
                Objects.equals(key7, otherRecord.key7) &&
                Objects.equals(key8, otherRecord.key8) &&
                Objects.equals(key9, otherRecord.key9) &&
                Objects.equals(key10, otherRecord.key10) &&
                Objects.equals(key11, otherRecord.key11) &&
                Objects.equals(key12, otherRecord.key12) &&
                Objects.equals(key13, otherRecord.key13) &&
                Objects.equals(key14, otherRecord.key14) &&
                Objects.equals(key15, otherRecord.key15) &&
                Objects.equals(key16, otherRecord.key16) &&
                Objects.equals(key17, otherRecord.key17) &&
                Objects.equals(key18, otherRecord.key18) &&
                Objects.equals(key19, otherRecord.key19) &&
                Objects.equals(key20, otherRecord.key20) &&
                Objects.equals(profileKey, otherRecord.profileKey) &&
                Objects.equals(rangeKey1, otherRecord.rangeKey1) &&
                Objects.equals(rangeKey2, otherRecord.rangeKey2) &&
                Objects.equals(rangeKey3, otherRecord.rangeKey3) &&
                Objects.equals(rangeKey4, otherRecord.rangeKey4) &&
                Objects.equals(rangeKey5, otherRecord.rangeKey5) &&
                Objects.equals(rangeKey6, otherRecord.rangeKey6) &&
                Objects.equals(rangeKey7, otherRecord.rangeKey7) &&
                Objects.equals(rangeKey8, otherRecord.rangeKey8) &&
                Objects.equals(rangeKey9, otherRecord.rangeKey9) &&
                Objects.equals(rangeKey10, otherRecord.rangeKey10) &&
                Objects.equals(body, otherRecord.body) &&
                Objects.equals(precommitBody, otherRecord.precommitBody) &&
                Objects.equals(serviceKey1, otherRecord.serviceKey1) &&
                Objects.equals(serviceKey2, otherRecord.serviceKey2) &&
                Objects.equals(serviceKey3, otherRecord.serviceKey3) &&
                Objects.equals(serviceKey4, otherRecord.serviceKey4) &&
                Objects.equals(serviceKey5, otherRecord.serviceKey5) &&
                Objects.equals(expiresAt, otherRecord.expiresAt) &&
                Objects.equals(attachments, otherRecord.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordKey, parentKey, key1, key2, key3, key4, key5, key6, key7, key8, key9, key10,
                key11, key12, key13, key14, key15, key16, key17, key18, key19, key20,
                rangeKey1, rangeKey2, rangeKey3, rangeKey4, rangeKey5, rangeKey6, rangeKey7, rangeKey8, rangeKey9, rangeKey10,
                body, profileKey, precommitBody, serviceKey1, serviceKey2, serviceKey3, serviceKey4, serviceKey5,
                expiresAt, attachments, version);
    }

    /**
     * get copy of Record immutably
     *
     * @return return copy
     */
    public Record copy() {
        Record newRecord = new Record(recordKey);
        newRecord.parentKey = parentKey;
        newRecord.key1 = key1;
        newRecord.key2 = key2;
        newRecord.key3 = key3;
        newRecord.key4 = key4;
        newRecord.key5 = key5;
        newRecord.key6 = key6;
        newRecord.key7 = key7;
        newRecord.key8 = key8;
        newRecord.key9 = key9;
        newRecord.key10 = key10;
        newRecord.key11 = key11;
        newRecord.key12 = key12;
        newRecord.key13 = key13;
        newRecord.key14 = key14;
        newRecord.key15 = key15;
        newRecord.key16 = key16;
        newRecord.key17 = key17;
        newRecord.key18 = key18;
        newRecord.key19 = key19;
        newRecord.key20 = key20;
        newRecord.rangeKey1 = rangeKey1;
        newRecord.rangeKey2 = rangeKey2;
        newRecord.rangeKey3 = rangeKey3;
        newRecord.rangeKey4 = rangeKey4;
        newRecord.rangeKey5 = rangeKey5;
        newRecord.rangeKey6 = rangeKey6;
        newRecord.rangeKey7 = rangeKey7;
        newRecord.rangeKey8 = rangeKey8;
        newRecord.rangeKey9 = rangeKey9;
        newRecord.rangeKey10 = rangeKey10;
        newRecord.profileKey = profileKey;
        newRecord.body = body;
        newRecord.precommitBody = precommitBody;
        newRecord.serviceKey1 = serviceKey1;
        newRecord.serviceKey2 = serviceKey2;
        newRecord.serviceKey3 = serviceKey3;
        newRecord.serviceKey4 = serviceKey4;
        newRecord.serviceKey5 = serviceKey5;
        newRecord.createdAt = getCreatedAt();
        newRecord.updatedAt = getUpdatedAt();
        newRecord.expiresAt = getExpiresAt();
        newRecord.version = version;
        newRecord.attachments = attachments == null ? new ArrayList<>() : new ArrayList<>(attachments);
        return newRecord;
    }

    @Override
    public String toString() {
        return "Record{" +
                "recordKey='" + recordKey + "', hash=" + hashCode() +
                '}';
    }
}
