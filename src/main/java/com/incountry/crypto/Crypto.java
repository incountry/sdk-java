package com.incountry.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import static com.incountry.crypto.CryptoUtils.generateSalt;
import static com.incountry.crypto.CryptoUtils.generateStrongPasswordHash;
import static com.incountry.Utils.*;

public class Crypto implements ICrypto {
    private String secret;
    private String envId;
    private static final int AUTH_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 64;
    private static final int PBKDF2_ITERATIONS_COUNT = 10000;
    private static final String VERSION = "1";

    public Crypto(String secret) {
        this.secret = secret;
    }

    public Crypto(String secret, String envId) {
        this.secret = secret;
        this.envId = envId;
    }

    public String encrypt(String plainText) throws GeneralSecurityException, IOException {
        byte[] clean = plainText.getBytes();
        byte[] salt = generateSalt(SALT_LENGTH);
        byte[] strong = generateStrongPasswordHash(secret, salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);

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

        return VERSION + ":" + bytesToHex(res);
    }

    private static String createHash(String stringToHash) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(stringToHash);
    }

    public String createKeyHash(String key) {
        if (key == null) return null;
        String stringToHash = key + ":" + envId;
        return createHash(stringToHash);
    }

    public String decrypt(String cipherText) throws GeneralSecurityException {
        if (cipherText == null) return null;

        String[] parts = cipherText.split(":");
        if (parts.length != 2){
            return decryptV0(cipherText);
        }
        return decryptV1(parts[1]);
    }


    private String decryptV1(String cipherText) throws GeneralSecurityException {
        byte[] parts = hexToBytes(cipherText);
        byte[] salt = Arrays.copyOfRange(parts, 0, 64);
        byte[] iv = Arrays.copyOfRange(parts, 64, 76);
        byte[] encrypted = Arrays.copyOfRange(parts, 76, parts.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] strong = generateStrongPasswordHash(secret, salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);

        SecretKeySpec keySpec = new SecretKeySpec(strong, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        byte[] decryptedText = cipher.doFinal(encrypted);

        return new String(decryptedText);
    }


    private String decryptV0(String cipherText) throws GeneralSecurityException {
        int keySize = 16;

        byte[] encryptedBytes  = hexToBytes(cipherText);
        // Hash key.
        byte[] keyBytes = new byte[keySize];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(secret.getBytes(StandardCharsets.UTF_8));
        byte[] longKey = md.digest();
        System.arraycopy(longKey, 0, keyBytes, 0, keySize);
        byte[] ivBytes = new byte[keySize];
        System.arraycopy(longKey, keySize, ivBytes, 0, keySize);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return new String(decrypted);
    }
}