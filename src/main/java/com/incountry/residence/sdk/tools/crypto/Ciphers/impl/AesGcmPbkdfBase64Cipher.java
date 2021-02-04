package com.incountry.residence.sdk.tools.crypto.Ciphers.impl;

import com.incountry.residence.sdk.tools.crypto.Ciphers.AesGcmCipher;
import com.incountry.residence.sdk.tools.crypto.Ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.Ciphers.DefaultCipher;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.crypto.SecretsFactory;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmPbkdfBase64Cipher extends AesGcmCipher implements DefaultCipher {

    private static final Logger LOG = LogManager.getLogger(AesGcmCipher.class);

    private static final String MSG_ERR_ALG_EXCEPTION = "AES/GCM/NoPadding algorithm exception";
    private static final String MSG_ERR_ENCRYPTION = "Data encryption error";
    private static final String MSG_ERR_ENCRYPTED_TEXT = "Encrypted text is incorrect";

    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    protected static final int AUTH_TAG_LENGTH_IN_BITS = 128;

    private String cipherVersion;
    private int pbkdf2Iterations;
    private Charset charset;
    private SecureRandom randomSecureRandom;
    private String code;

    public AesGcmPbkdfBase64Cipher(String cipherVersion, int pbkdf2Iterations, Charset charset) {
        this.cipherVersion = cipherVersion;
        this.pbkdf2Iterations = pbkdf2Iterations;
        this.charset = charset;
        this.randomSecureRandom = new SecureRandom();
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public CipherText encrypt(String text, Secret secret) throws StorageCryptoException {
        byte[] clean = text.getBytes(charset);
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        byte[] key = SecretsFactory.getKey(salt, secret, pbkdf2Iterations);
        byte[] iv = generateRandomBytes(IV_LENGTH);

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, SECRET_KEY_ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH_IN_BITS, iv);

        byte[] encrypted;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
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
        return new CipherText(cipherVersion + ":" + new String(encoded, charset), secret.getVersion());
    }

    @Override
    public String decrypt(String cipherText, Secret secret) throws StorageCryptoException {
        if (cipherText == null || cipherText.isEmpty()) {
            LOG.error(MSG_ERR_ENCRYPTED_TEXT);
            throw new StorageCryptoException(MSG_ERR_ENCRYPTED_TEXT);
        }
        byte[] bytes = Base64.getDecoder().decode(cipherText);
        return decodeBytes(bytes, secret, pbkdf2Iterations, charset);
    }

    private static byte[] generateRandomBytes(int length) {
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        randomSecureRandom.nextBytes(randomBytes);
        return randomBytes;
    }
}
