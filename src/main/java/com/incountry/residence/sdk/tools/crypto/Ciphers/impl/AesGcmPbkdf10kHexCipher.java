package com.incountry.residence.sdk.tools.crypto.Ciphers.impl;

import com.incountry.residence.sdk.tools.crypto.Ciphers.AesGcmCipher;
import com.incountry.residence.sdk.tools.crypto.Ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.Ciphers.DefaultCipher;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

public class AesGcmPbkdf10kHexCipher extends AesGcmCipher implements DefaultCipher {

    protected static final int PBKDF2_ITERATIONS = 10_000;
    private Charset charset;
    public String code;

    public AesGcmPbkdf10kHexCipher(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String getCode() {
        return "1";
    }

    @Override
    public CipherText encrypt(String text, Secret secretKey) throws StorageCryptoException {
        return null;
    }

    @Override
    public String decrypt(String cipherText, Secret secret) throws StorageCryptoException {
        if (cipherText == null || cipherText.isEmpty()) {
            return decodeBytes(new byte[0], secret, PBKDF2_ITERATIONS, charset);
        }
        byte[] bytes = DatatypeConverter.parseHexBinary(cipherText);
        return decodeBytes(bytes, secret, PBKDF2_ITERATIONS, charset);
    }
}
