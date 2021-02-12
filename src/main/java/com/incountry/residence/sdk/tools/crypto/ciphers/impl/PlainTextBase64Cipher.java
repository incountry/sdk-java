package com.incountry.residence.sdk.tools.crypto.ciphers.impl;

import com.incountry.residence.sdk.tools.crypto.ciphers.Cipher;
import com.incountry.residence.sdk.tools.crypto.ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.Secret;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PlainTextBase64Cipher implements Cipher {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String CIPHER_CODE = "pt";

    private Charset encoding;

    public PlainTextBase64Cipher(Charset encoding) {
        this.encoding = encoding;
    }

    @Override
    public CipherText encrypt(String text, Secret secretKey) {
        String ptEncoded = new String(Base64.getEncoder().encode(text.getBytes(CHARSET)), CHARSET);
        return new CipherText(CIPHER_CODE + ":" + ptEncoded);
    }

    @Override
    public String decrypt(String cipherText, Secret secret) {
        byte[] ptEncodedBytes = Base64.getDecoder().decode(cipherText);
        return new String(ptEncodedBytes, encoding);
    }

    @Override
    public String getCode() {
        return CIPHER_CODE;
    }
}
