package com.incountry.residence.sdk.tools.crypto.cipher;

import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.crypto.SecretKeyFactory;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public abstract class AesGcmPbkdfBase64Cipher extends AesGcmPbkdfCipher implements Cipher {
    private static final Logger LOG = LogManager.getLogger(AesGcmPbkdfBase64Cipher.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);

    private static final String MSG_ERR_ALG_EXCEPTION = "AES/GCM/NoPadding algorithm exception";
    private static final String MSG_ERR_ENCRYPTION = "Data encryption error";

    private final int pbkdf2Iterations;
    private final Charset charset;

    protected AesGcmPbkdfBase64Cipher(int pbkdf2Iterations, Charset charset) {
        this.pbkdf2Iterations = pbkdf2Iterations;
        this.charset = charset;
    }

    @Override
    public Ciphertext encrypt(String text, Secret secret) throws StorageCryptoException {
        byte[] clean = text.getBytes(charset);
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        byte[] key = SecretKeyFactory.getKey(salt, secret, pbkdf2Iterations);
        byte[] iv = generateRandomBytes(IV_LENGTH);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM_NAME);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH_IN_BITS, iv);
        byte[] encrypted;
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            encrypted = cipher.doFinal(clean);
        } catch (GeneralSecurityException e) {
            throw new StorageCryptoException(MSG_ERR_ALG_EXCEPTION, e);
        }
        byte[] resultByteArray;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.write(encrypted);
            resultByteArray = outputStream.toByteArray();
        } catch (IOException e) {
            throw new StorageCryptoException(MSG_ERR_ENCRYPTION, e);
        }
        byte[] encoded = Base64.getEncoder().encode(resultByteArray);
        return new Ciphertext(getName() + ":" + new String(encoded, charset), secret.getVersion());
    }

    private static byte[] generateRandomBytes(int length) {
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        randomSecureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    @Override
    public String decrypt(String cipherText, Secret secret) throws StorageCryptoException {
        boolean invalidCipherText = cipherText == null || cipherText.isEmpty();
        HELPER.check(StorageCryptoException.class, invalidCipherText, MSG_ERR_LENGTH);
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        return decodeBytes(decodedBytes, secret, pbkdf2Iterations, charset);
    }
}
