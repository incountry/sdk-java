package com.incountry.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import static com.incountry.crypto.CryptoUtils.generateSalt;
import static com.incountry.crypto.CryptoUtils.generateStrongPasswordHash;
import static com.incountry.Utils.*;

public class Crypto implements ICrypto {
    private String SECRET;
    private static final int AUTH_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 64;
    private static final int PBKDF2_ITERATIONS_COUNT = 10000;

    public Crypto(String secret) {
        SECRET = secret;
    }

    public String encrypt(String plainText) throws GeneralSecurityException, IOException {
        byte[] clean = plainText.getBytes();
        byte[] salt = generateSalt(SALT_LENGTH);
        byte[] strong = generateStrongPasswordHash(SECRET, salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);

        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        randomSecureRandom.nextBytes(iv);

        SecretKeySpec secretKeySpec = new SecretKeySpec(strong, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH * 8, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(salt);
        outputStream.write(iv);
        outputStream.write(encrypted);

        byte[] res = outputStream.toByteArray();

        return bytesToHex(res);
    }


    public String decrypt(String cipherText) throws GeneralSecurityException {
        byte[] parts = hexToBytes(cipherText);
        byte[] salt = Arrays.copyOfRange(parts, 0, 64);
        byte[] iv = Arrays.copyOfRange(parts, 64, 76);
        byte[] encrypted = Arrays.copyOfRange(parts, 76, parts.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] strong = generateStrongPasswordHash(SECRET, salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);

        SecretKeySpec keySpec = new SecretKeySpec(strong, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        byte[] decryptedText = cipher.doFinal(encrypted);

        return new String(decryptedText);
    }
}