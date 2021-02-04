package com.incountry.residence.sdk.tools.crypto.Ciphers.impl;

import com.incountry.residence.sdk.tools.crypto.Ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.Ciphers.DefaultCipher;
import com.incountry.residence.sdk.tools.crypto.Secret;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PlainTextBase64Cipher implements DefaultCipher {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String PREFIX_PLAIN_TEXT_VERSION = "pt";

    private Charset encoding;

    public PlainTextBase64Cipher(Charset encoding) {
        this.encoding = encoding;
    }

    @Override
    public CipherText encrypt(String text, Secret secretKey) {
        String ptEncoded = new String(Base64.getEncoder().encode(text.getBytes(CHARSET)), CHARSET);
        return new CipherText(PREFIX_PLAIN_TEXT_VERSION + ":" + ptEncoded);
    }

    @Override
    public String decrypt(String cipherText, Secret secret) {
        byte[] ptEncodedBytes = Base64.getDecoder().decode(cipherText);
        return new String(ptEncodedBytes, encoding);
    }

    @Override
    public String getCode() {
        return PREFIX_PLAIN_TEXT_VERSION;
    }
}
