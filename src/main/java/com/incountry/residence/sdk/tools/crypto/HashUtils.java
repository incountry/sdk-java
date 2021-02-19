package com.incountry.residence.sdk.tools.crypto;

import org.apache.commons.codec.digest.DigestUtils;

public class HashUtils {
    private final String salt;
    private final boolean normalizeKeys;

    public HashUtils(String salt, boolean normalizeKeys) {
        this.salt = salt;
        this.normalizeKeys = normalizeKeys;
    }

    public String getSha256Hash(String key) {
        if (key == null) {
            return null;
        }
        String stringToHash = key + ":" + salt;
        return DigestUtils.sha256Hex(normalizeKeys ? stringToHash.toLowerCase() : stringToHash);
    }
}
