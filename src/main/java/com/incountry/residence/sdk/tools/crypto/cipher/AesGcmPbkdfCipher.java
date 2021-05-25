package com.incountry.residence.sdk.tools.crypto.cipher;

import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.crypto.SecretKeyFactory;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public abstract class AesGcmPbkdfCipher {
    private static final Logger LOG = LogManager.getLogger(AesGcmPbkdfCipher.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);

    private static final String MSG_ERR_DECRYPTION = "Data decryption error";
    protected static final String MSG_ERR_LENGTH = "Encrypted text is incorrect";
    private static final String MSG_ERR_NO_SECRET = "No secret provided";
    private static final String MSG_ERR_NO_CHARSET = "Charset is null";

    protected static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    protected static final String ALGORITHM_NAME = "AES";

    protected static final int AUTH_TAG_LENGTH_IN_BITS = 128;
    protected static final int SALT_LENGTH = 64;
    protected static final int IV_LENGTH = 12;
    private static final int META_INFO_LENGTH = SALT_LENGTH + IV_LENGTH;

    protected AesGcmPbkdfCipher() {
    }

    @SuppressWarnings("java:S2259")
    protected static String decodeBytes(byte[] decodedBytes, Secret secret, int pbkdf2Iterations, Charset charset) throws StorageCryptoException {
        boolean isInvalidCipherLength = decodedBytes.length < META_INFO_LENGTH;
        HELPER.check(StorageCryptoException.class, isInvalidCipherLength, MSG_ERR_LENGTH);
        HELPER.check(StorageCryptoException.class, secret == null, MSG_ERR_NO_SECRET);
        HELPER.check(StorageCryptoException.class, charset == null, MSG_ERR_NO_CHARSET);

        byte[] salt = Arrays.copyOfRange(decodedBytes, 0, SALT_LENGTH);
        byte[] iv = Arrays.copyOfRange(decodedBytes, SALT_LENGTH, META_INFO_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(decodedBytes, META_INFO_LENGTH, decodedBytes.length);

        byte[] decryptedText;
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TRANSFORMATION);
            byte[] key = SecretKeyFactory.getKey(salt, secret, pbkdf2Iterations);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH_IN_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            decryptedText = cipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throw new StorageCryptoException(MSG_ERR_DECRYPTION, e);
        }
        return new String(decryptedText, charset);
    }
}
