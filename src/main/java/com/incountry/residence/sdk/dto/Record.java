package com.incountry.residence.sdk.dto;

import java.util.Objects;

public class Record {
    private String key;
    private String key2;
    private String key3;
    private String profileKey;
    private Integer rangeKey;
    private String body;


    public Record() {
    }

    /**
     * Short constructor
     *
     * @param key  key
     * @param body data to be stored and encrypted
     */
    public Record(String key, String body) {
        this.key = key;
        this.body = body;
    }

    /**
     * Full constructor
     *
     * @param key        Required, record key
     * @param body       Optional, data to be stored and encrypted
     * @param profileKey Optional, profile key
     * @param rangeKey   Optional, range key for sorting in pagination
     * @param key2       Optional, key2
     * @param key3       Optional, key3
     */
    public Record(String key, String body, String profileKey, Integer rangeKey, String key2, String key3) {
        this.key = key;
        this.body = body;
        this.profileKey = profileKey;
        this.rangeKey = rangeKey;
        this.key2 = key2;
        this.key3 = key3;
    }

    private static <T> T mergeKeys(T oldKey, T newKey) {
        return newKey != null ? newKey : oldKey;
    }

    /**
     * merge records. Notnull field values from @merged replaces old ones in @base
     *
     * @param base   base record
     * @param merged new records, null fileds are ignored
     * @return new record with merged fileds
     */
    public static Record merge(Record base, Record merged) {
        String mergedKey = mergeKeys(base.getKey(), merged.getKey());
        String mergedBody = mergeKeys(base.getBody(), merged.getBody());
        String mergedProfileKey = mergeKeys(base.getProfileKey(), merged.getProfileKey());
        Integer mergedRangeKey = mergeKeys(base.getRangeKey(), merged.getRangeKey());
        String mergedKey2 = mergeKeys(base.getKey2(), merged.getKey2());
        String mergedKey3 = mergeKeys(base.getKey3(), merged.getKey3());

        return new Record(mergedKey, mergedBody, mergedProfileKey, mergedRangeKey, mergedKey2, mergedKey3);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public Integer getRangeKey() {
        return rangeKey;
    }

    public void setRangeKey(Integer rangeKey) {
        this.rangeKey = rangeKey;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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
        return Objects.equals(key, record.key) &&
                Objects.equals(key2, record.key2) &&
                Objects.equals(key3, record.key3) &&
                Objects.equals(profileKey, record.profileKey) &&
                Objects.equals(rangeKey, record.rangeKey) &&
                Objects.equals(body, record.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, key2, key3, profileKey, rangeKey, body);
    }
}
