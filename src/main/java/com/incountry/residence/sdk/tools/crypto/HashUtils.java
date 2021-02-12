package com.incountry.residence.sdk.tools.crypto;

import org.apache.commons.codec.digest.DigestUtils;

public class HashUtils {

    private String environmentId;
    private boolean normalizeKeys;

    public HashUtils(String environmentId, boolean normalizeKeys) {
        this.environmentId = environmentId;
        this.normalizeKeys = normalizeKeys;
    }

    public String getSha256Hash(String key) {
        if (key == null) {
            return null;
        }
        String stringToHash = key + ":" + environmentId;
        return DigestUtils.sha256Hex(normalizeKeys ? stringToHash.toLowerCase() : stringToHash);
    }

}
