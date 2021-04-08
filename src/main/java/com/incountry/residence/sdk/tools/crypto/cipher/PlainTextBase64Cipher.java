package com.incountry.residence.sdk.tools.crypto.cipher;

import com.incountry.residence.sdk.crypto.Secret;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PlainTextBase64Cipher implements Cipher {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String CIPHER_NAME = "pt";

    @Override
    public String getName() {
        return CIPHER_NAME;
    }

    @Override
    public Ciphertext encrypt(String text, Secret secret) {
        String ptEncoded = new String(Base64.getEncoder().encode(text.getBytes(CHARSET)), CHARSET);
        return new Ciphertext(getName() + ":" + ptEncoded, null);
    }

    @Override
    public String decrypt(String cipherText, Secret secret) {
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        return new String(decodedBytes, CHARSET);
    }
}
