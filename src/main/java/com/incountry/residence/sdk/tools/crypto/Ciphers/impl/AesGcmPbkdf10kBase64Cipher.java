package com.incountry.residence.sdk.tools.crypto.Ciphers.impl;

import java.nio.charset.Charset;

public class AesGcmPbkdf10kBase64Cipher extends AesGcmPbkdfBase64Cipher {

    public static String CIPHER_CODE = "2";
    private static int ITERATIONS = 10_000;

    public AesGcmPbkdf10kBase64Cipher(Charset charset) {
        super(CIPHER_CODE, ITERATIONS, charset);
    }

    @Override
    public String getCode() {
        return CIPHER_CODE;
    }
}
