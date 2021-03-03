package com.incountry.residence.sdk.tools.crypto.cipher;

import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

public class AesGcmPbkdf10kHexCipher extends AesGcmPbkdfCipher implements Cipher {

    private static final int PBKDF2_ITERATIONS = 10_000;
    private static final String NAME = "1";

    private static final String MSG_ERR_NOT_SUPPORTED = "Cipher doesn't support encryption";

    private final Charset charset;

    public AesGcmPbkdf10kHexCipher(Charset charset) {
        super();
        this.charset = charset;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Ciphertext encrypt(String text, Secret secret) throws StorageCryptoException {
        throw new StorageCryptoException(MSG_ERR_NOT_SUPPORTED);
    }

    @Override
    public String decrypt(String cipherText, Secret secret) throws StorageCryptoException {
        byte[] decodedBytes = DatatypeConverter.parseHexBinary(cipherText);
        return decodeBytes(decodedBytes, secret, PBKDF2_ITERATIONS, charset);
    }
}
