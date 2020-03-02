package com.incountry.crypto.impl;

import com.incountry.crypto.Crypto;
import com.incountry.exceptions.StorageCryptoException;
import com.incountry.keyaccessor.key.SecretKey;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

import com.incountry.keyaccessor.key.SecretKeysData;
import org.javatuples.Pair;

import static com.incountry.Utils.*;
import static com.incountry.crypto.CryptoUtils.*;

public class CryptoImpl implements Crypto {
    private SecretKeysData secretKeysData;
    private String envId;
    private Boolean isUsingPTEncryption = false;
    private static final int AUTH_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 64;
    private static final int PBKDF2_ITERATIONS_COUNT = 10000;
    private static final String VERSION = "2";
    public static final String PT_ENC_VERSION = "pt";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";

    public CryptoImpl(SecretKeysData secret) {
        this.secretKeysData = secret;
    }

    public CryptoImpl(String envId) {
        this.envId = envId;
        this.isUsingPTEncryption = true;
    }


    public CryptoImpl(SecretKeysData secret, String envId) {
        this.secretKeysData = secret;
        this.envId = envId;
    }

    public Pair<String, Integer> encrypt(String plainText) throws StorageCryptoException {
        if (Boolean.TRUE.equals(isUsingPTEncryption)) {
            byte[] ptEncoded = Base64.getEncoder().encode(plainText.getBytes());
            return new Pair<>(PT_ENC_VERSION + ":" + new String(ptEncoded), null);
        }

        byte[] clean = plainText.getBytes();
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        SecretKey secretKeyObj = getSecret(secretKeysData.getCurrentVersion());
        byte[] key = getKey(salt, secretKeyObj);
        byte[] iv = generateRandomBytes(IV_LENGTH);

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH * 8, iv);

        byte[] encrypted = {};
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            encrypted = cipher.doFinal(clean);
        } catch (GeneralSecurityException e){
            throw new StorageCryptoException(ENCRYPTION_ALGORITHM + " algorithm exception", e);
        }

        byte[] resultByteArray = {};
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.write(encrypted);
            resultByteArray = outputStream.toByteArray();
        } catch (IOException e) {
            throw new StorageCryptoException("Data encryption error", e);
        }

        byte[] encoded = Base64.getEncoder().encode(resultByteArray);

        return new Pair<>(VERSION + ":" + new String(encoded), secretKeyObj.getVersion());
    }

    private byte[] getKey(byte[] salt, SecretKey secretKeyObj) throws StorageCryptoException {
        if (secretKeyObj.getIsKey() != null && secretKeyObj.getIsKey()) {
            return secretKeyObj.getSecret().getBytes(StandardCharsets.UTF_8);
        }
        return generateStrongPasswordHash(secretKeyObj.getSecret(), salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);
    }

    private static String createHash(String stringToHash) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(stringToHash);
    }

    private String decryptUnpacked(byte[] parts, Integer decryptKeyVersion) throws StorageCryptoException {
        byte[] salt = Arrays.copyOfRange(parts, 0, 64);
        byte[] iv = Arrays.copyOfRange(parts, 64, 76);
        byte[] encrypted = Arrays.copyOfRange(parts, 76, parts.length);

        byte[] decryptedText ={};
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            byte[] key = getKey(salt, getSecret(decryptKeyVersion));

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            decryptedText = cipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throw new StorageCryptoException("Data encryption error", e);
        }

        return new String(decryptedText);
    }

    private SecretKey getSecret(Integer version) {
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

    public String decrypt(String cipherText, Integer decryptKeyVersion) throws StorageCryptoException {
        if (cipherText == null) return null;

//        String[] parts = cipherText.split(":");
        String[] parts = cipherText.split(":", -1);

        if (parts[0].equals(PT_ENC_VERSION)) {
            System.out.println();
            return decryptVPT(parts[1]);
        }

        if (!parts[0].equals(PT_ENC_VERSION) && Boolean.TRUE.equals(isUsingPTEncryption)) {
            throw new StorageCryptoException("No secret provided. Cannot decrypt record: "+ cipherText);
        }

        switch (parts[0]) {
            case "1":
                return decryptV1(parts[1], decryptKeyVersion);
            case "2":
                return decryptV2(parts[1], decryptKeyVersion);
            default:
                throw new StorageCryptoException("Decryption error: Illegal decryption version");
        }
    }

    private String decryptV2(String cipherText, Integer decryptKeyVersion) throws StorageCryptoException {
        byte[] parts = Base64.getDecoder().decode(cipherText);
        return this.decryptUnpacked(parts, decryptKeyVersion);
    }

    private String decryptV1(String cipherText, Integer decryptKeyVersion) throws StorageCryptoException {
        byte[] parts = hexToBytes(cipherText);
        return this.decryptUnpacked(parts, decryptKeyVersion);
    }

    private String decryptVPT(String cipherText) {
        byte[] ptBytes = Base64.getDecoder().decode(cipherText);
        return new String(ptBytes);
    }
}