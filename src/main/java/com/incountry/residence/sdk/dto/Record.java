package com.incountry.residence.sdk.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Record {
    private String recordKey;
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
    protected Date createdAt;
    protected Date updatedAt;
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

    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }

    public List<AttachmentMeta> getAttachments() {
        return attachments;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Record record = (Record) obj;
        return Objects.equals(recordKey, record.recordKey) &&
                Objects.equals(key1, record.key1) &&
                Objects.equals(key2, record.key2) &&
                Objects.equals(key3, record.key3) &&
                Objects.equals(key4, record.key4) &&
                Objects.equals(key5, record.key5) &&
                Objects.equals(key6, record.key6) &&
                Objects.equals(key7, record.key7) &&
                Objects.equals(key8, record.key8) &&
                Objects.equals(key9, record.key9) &&
                Objects.equals(key10, record.key10) &&
                Objects.equals(profileKey, record.profileKey) &&
                Objects.equals(rangeKey1, record.rangeKey1) &&
                Objects.equals(rangeKey2, record.rangeKey2) &&
                Objects.equals(rangeKey3, record.rangeKey3) &&
                Objects.equals(rangeKey4, record.rangeKey4) &&
                Objects.equals(rangeKey5, record.rangeKey5) &&
                Objects.equals(rangeKey6, record.rangeKey6) &&
                Objects.equals(rangeKey7, record.rangeKey7) &&
                Objects.equals(rangeKey8, record.rangeKey8) &&
                Objects.equals(rangeKey9, record.rangeKey9) &&
                Objects.equals(rangeKey10, record.rangeKey10) &&
                Objects.equals(body, record.body) &&
                Objects.equals(precommitBody, record.precommitBody) &&
                Objects.equals(serviceKey1, record.serviceKey1) &&
                Objects.equals(serviceKey2, record.serviceKey2) &&
                Objects.equals(createdAt, record.createdAt) &&
                Objects.equals(updatedAt, record.updatedAt) &&
                Objects.equals(attachments, record.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordKey, key1, key2, key3, key4, key5, key6, key7, key8, key9, key10,
                rangeKey1, rangeKey2, rangeKey3, rangeKey4, rangeKey5, rangeKey6, rangeKey7, rangeKey8, rangeKey9, rangeKey10,
                body, profileKey, precommitBody, serviceKey1, serviceKey2,
                createdAt, updatedAt, attachments);
    }

    /**
     * get copy of Record immutably
     *
     * @return return copy
     */
    protected Record copy() {
        Record newRecord = new Record(recordKey);
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
        newRecord.createdAt = getCreatedAt();
        newRecord.updatedAt = getUpdatedAt();
        newRecord.attachments = attachments == null ? new ArrayList<>() : attachments.stream().collect(Collectors.toList());
        return newRecord;
    }
}
