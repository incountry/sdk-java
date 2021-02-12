package com.incountry.residence.sdk.crypto.cipher;

import com.incountry.residence.sdk.tools.crypto.ciphers.AbstractCipher;
import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.nio.charset.StandardCharsets;

public class CipherWithException extends AbstractCipher {

    private String exceptionText;

    @SuppressFBWarnings(value = "NM_CLASS_NOT_EXCEPTION")
    public CipherWithException(String code) {
        this(code, "");
    }

    public CipherWithException(String code, String exceptionText) {
        super(code, StandardCharsets.UTF_8);
        this.exceptionText = exceptionText;
    }

    @Override
    public String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) {
        throw new RuntimeException(exceptionText);
    }

    @Override
    public String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey)  {
        throw new RuntimeException(exceptionText);
    }
}
