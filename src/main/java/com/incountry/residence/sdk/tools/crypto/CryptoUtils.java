package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtils {

    private CryptoUtils() {
    }

    public static byte[] generateStrongPasswordHash(String password, byte[] salt, int iterations, int length) throws StorageCryptoException {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, length * 8);
        byte[] strongPasswordHash = {};
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            strongPasswordHash = skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new StorageCryptoException("Unable to generate secret - cannot find PBKDF2WithHmacSHA512 algorithm. Please, check your JVM configuration", e);
        } catch (InvalidKeySpecException e) {
            throw new StorageCryptoException("Secret generation exception", e);
        }

        return strongPasswordHash;
    }

    public static byte[] generateRandomBytes(int length) {
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        randomSecureRandom.nextBytes(randomBytes);
        return randomBytes;
    }
}
