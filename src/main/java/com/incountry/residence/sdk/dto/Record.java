package com.incountry.residence.sdk.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP", "EI_EXPOSE_REP"})
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
    protected Integer version;
    private List<AttachmentMeta> attachments = new ArrayList<>();

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getKey3() {
        return key3;
    }

    public void setKey3(String key3) {
        this.key3 = key3;
    }

    public String getKey4() {
        return key4;
    }

    public void setKey4(String key4) {
        this.key4 = key4;
    }

    public String getKey5() {
        return key5;
    }

    public void setKey5(String key5) {
        this.key5 = key5;
    }

    public String getKey6() {
        return key6;
    }

    public void setKey6(String key6) {
        this.key6 = key6;
    }

    public String getKey7() {
        return key7;
    }

    public void setKey7(String key7) {
        this.key7 = key7;
    }

    public String getKey8() {
        return key8;
    }

    public void setKey8(String key8) {
        this.key8 = key8;
    }

    public String getKey9() {
        return key9;
    }

    public void setKey9(String key9) {
        this.key9 = key9;
    }

    public String getKey10() {
        return key10;
    }

    public void setKey10(String key10) {
        this.key10 = key10;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public Long getRangeKey1() {
        return rangeKey1;
    }

    public void setRangeKey1(Long rangeKey1) {
        this.rangeKey1 = rangeKey1;
    }

    public Long getRangeKey2() {
        return rangeKey2;
    }

    public void setRangeKey2(Long rangeKey2) {
        this.rangeKey2 = rangeKey2;
    }

    public Long getRangeKey3() {
        return rangeKey3;
    }

    public void setRangeKey3(Long rangeKey3) {
        this.rangeKey3 = rangeKey3;
    }

    public Long getRangeKey4() {
        return rangeKey4;
    }

    public void setRangeKey4(Long rangeKey4) {
        this.rangeKey4 = rangeKey4;
    }

    public Long getRangeKey5() {
        return rangeKey5;
    }

    public void setRangeKey5(Long rangeKey5) {
        this.rangeKey5 = rangeKey5;
    }

    public Long getRangeKey6() {
        return rangeKey6;
    }

    public void setRangeKey6(Long rangeKey6) {
        this.rangeKey6 = rangeKey6;
    }

    public Long getRangeKey7() {
        return rangeKey7;
    }

    public void setRangeKey7(Long rangeKey7) {
        this.rangeKey7 = rangeKey7;
    }

    public Long getRangeKey8() {
        return rangeKey8;
    }

    public void setRangeKey8(Long rangeKey8) {
        this.rangeKey8 = rangeKey8;
    }

    public Long getRangeKey9() {
        return rangeKey9;
    }

    public void setRangeKey9(Long rangeKey9) {
        this.rangeKey9 = rangeKey9;
    }

    public Long getRangeKey10() {
        return rangeKey10;
    }

    public void setRangeKey10(Long rangeKey10) {
        this.rangeKey10 = rangeKey10;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPrecommitBody() {
        return precommitBody;
    }

    public void setPrecommitBody(String precommitBody) {
        this.precommitBody = precommitBody;
    }

    public String getServiceKey1() {
        return serviceKey1;
    }

    public void setServiceKey1(String serviceKey1) {
        this.serviceKey1 = serviceKey1;
    }

    public String getServiceKey2() {
        return serviceKey2;
    }

    public void setServiceKey2(String serviceKey2) {
        this.serviceKey2 = serviceKey2;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<AttachmentMeta> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentMeta> attachments) {
        this.attachments = attachments;
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

    @Override
    public String toString() {
        return "Record{" +
                "recordKey='" + recordKey + "', hash=" + hashCode() +
                '}';
    }
}

