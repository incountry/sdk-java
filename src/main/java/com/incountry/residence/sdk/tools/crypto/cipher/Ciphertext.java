package com.incountry.residence.sdk.tools.crypto.cipher;

public class Ciphertext {

    private String data;
    private Integer keyVersion;

    public String getData() {
        return data;
    }

    public Integer getKeyVersion() {
        return keyVersion;
    }

    public Ciphertext(String data, Integer keyVersion) {
        this.data = data;
        this.keyVersion = keyVersion;
    }
}
