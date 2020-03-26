package com.incountry.storage.sdk.tools.keyaccessor.key;

public class SecretKey {
    private String secret;
    private int version;
    private Boolean isKey;

    public SecretKey() {
    }

    public SecretKey(String secret, int version, Boolean isKey) {
        this.secret = secret;
        this.version = version;
        this.isKey = isKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Boolean getIsKey() {
        return isKey;
    }

    public void setIsKey(Boolean key) {
        isKey = key;
    }
}
