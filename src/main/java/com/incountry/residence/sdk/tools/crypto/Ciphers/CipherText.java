package com.incountry.residence.sdk.tools.crypto.Ciphers;

public class CipherText {

    private String data;
    private int keyVersion;

    public CipherText(String data) {
        this.data = data;
    }

    public CipherText(String data, int keyVersion) {
        this.data = data;
        this.keyVersion = keyVersion;
    }

    public String getData() {
        return data;
    }

    public int getKeyVersion() {
        return keyVersion;
    }
}
