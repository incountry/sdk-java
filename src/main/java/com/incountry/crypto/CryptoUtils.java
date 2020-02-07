package com.incountry.crypto;

import com.incountry.exceptions.StorageCryptoException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtils {
    private CryptoUtils() {}

    public static byte[] generateStrongPasswordHash(String password, byte[] salt, int iterations, int length) throws StorageCryptoException {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, length * 8);
        byte[] trongPasswordHash = {};
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            trongPasswordHash = skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new StorageCryptoException("PBKDF2WithHmacSHA512 security algorithm exception", e);
        } catch (InvalidKeySpecException e) {
            throw new StorageCryptoException("Secret generation exception", e);
        }

        return trongPasswordHash;
    }

//    public static byte[] generateSalt(int length) throws NoSuchAlgorithmException {
    public static byte[] generateSalt(int length) throws StorageCryptoException {
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new StorageCryptoException("SHA1PRNG security algorithm exception", e);
        }
        byte[] salt = new byte[length];
        sr.nextBytes(salt);
        return salt;
    }
}
