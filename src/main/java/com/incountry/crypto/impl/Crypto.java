package com.incountry.crypto.impl;

import com.incountry.crypto.ICrypto;
import com.incountry.keyaccessor.key.SecretKey;

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
import java.util.Base64;

import com.incountry.keyaccessor.key.SecretKeysData;
import org.javatuples.Pair;

import static com.incountry.crypto.CryptoUtils.generateSalt;
import static com.incountry.crypto.CryptoUtils.generateStrongPasswordHash;
import static com.incountry.Utils.*;

public class Crypto implements ICrypto {
    private SecretKeysData secretKeysData;
    private String envId;
    private static final int AUTH_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 64;
    private static final int PBKDF2_ITERATIONS_COUNT = 10000;
    private static final String VERSION = "2";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";

    public Crypto(SecretKeysData secret) {
        this.secretKeysData = secret;
    }

    public Crypto(SecretKeysData secret, String envId) {
        this.secretKeysData = secret;
        this.envId = envId;
    }

    public Pair<String, Integer> encrypt(String plainText) throws GeneralSecurityException, IOException {
        byte[] clean = plainText.getBytes();
        byte[] salt = generateSalt(SALT_LENGTH);
        SecretKey secretKeyObj = getSecret(secretKeysData.getCurrentVersion());
        byte[] strong = generateStrongPasswordHash(secretKeyObj.getSecret(), salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);

        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        randomSecureRandom.nextBytes(iv);

        SecretKeySpec secretKeySpec = new SecretKeySpec(strong, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH * 8, iv);

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(salt);
        outputStream.write(iv);
        outputStream.write(encrypted);

        byte[] res = outputStream.toByteArray();
        byte[] encoded = Base64.getEncoder().encode(res);

        return new Pair<>(VERSION + ":" + new String(encoded), secretKeyObj.getVersion());
    }

    private static String createHash(String stringToHash) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(stringToHash);
    }

    private String decryptUnpacked(byte[] parts, Integer decryptKeyVersion) throws GeneralSecurityException {
        byte[] salt = Arrays.copyOfRange(parts, 0, 64);
        byte[] iv = Arrays.copyOfRange(parts, 64, 76);
        byte[] encrypted = Arrays.copyOfRange(parts, 76, parts.length);

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        byte[] strongPasswordHash = generateStrongPasswordHash(getSecret(decryptKeyVersion).getSecret(), salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);

        SecretKeySpec keySpec = new SecretKeySpec(strongPasswordHash, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        byte[] decryptedText = cipher.doFinal(encrypted);

        return new String(decryptedText);
    }

    private SecretKey getSecret(Integer version){
        SecretKey secret = null;
        for (SecretKey item : secretKeysData.getSecrets()) {
            if (item.getVersion() == version) {
                secret = item;
                break;
            }
        }
        if (secret == null) throw new IllegalArgumentException("SecretKeyGenerator returns data in which there is no current version of the key");
        return secret;
    }

    public int getCurrentSecretVersion() {
        return secretKeysData.getCurrentVersion();
    }

    public String createKeyHash(String key) {
        if (key == null) return null;
        String stringToHash = key + ":" + envId;
        return createHash(stringToHash);
    }

    public String decrypt(String cipherText, Integer decryptKeyVersion) throws GeneralSecurityException {
        if (cipherText == null) return null;

        String[] parts = cipherText.split(":");

        switch (parts[0]) {
            case "1":
                return decryptV1(parts[1], decryptKeyVersion);
            case "2":
                return decryptV2(parts[1], decryptKeyVersion);
            default:
                return decryptV0(cipherText, decryptKeyVersion);
        }
    }

    private String decryptV2(String cipherText, Integer decryptKeyVersion) throws GeneralSecurityException {
        byte[] parts = Base64.getDecoder().decode(cipherText);
        return this.decryptUnpacked(parts, decryptKeyVersion);
    }

    private String decryptV1(String cipherText, Integer decryptKeyVersion) throws GeneralSecurityException {
        byte[] parts = hexToBytes(cipherText);
        return this.decryptUnpacked(parts, decryptKeyVersion);
    }


    private String decryptV0(String cipherText, Integer decryptKeyVersion) throws GeneralSecurityException {
        int keySize = 16;

        byte[] encryptedBytes  = hexToBytes(cipherText);
        // Hash key.
        byte[] keyBytes = new byte[keySize];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(getSecret(decryptKeyVersion).getSecret().getBytes(StandardCharsets.UTF_8));
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