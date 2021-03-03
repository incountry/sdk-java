package com.incountry.residence.sdk.tools.crypto.cipher;

import java.nio.charset.Charset;

public class AesGcmPbkdf10kBase64Cipher extends AesGcmPbkdfBase64Cipher {

    private static final String CIPHER_NAME = "2";
    private static final int ITERATIONS = 10_000;

    @Override
    public String getName() {
        return CIPHER_NAME;
    }

    public AesGcmPbkdf10kBase64Cipher(Charset charset) {
        super(ITERATIONS, charset);
    }
}
