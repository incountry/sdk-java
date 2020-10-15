package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class DefaultCrypto implements Crypto {
    private static final String MSG_ERR_GEN_SECRET = "Secret generation exception";
    private static final String MSG_ERR_NO_ALGORITHM = "Unable to generate secret - cannot find PBKDF2WithHmacSHA512 algorithm. Please, check your JVM configuration";
    private static final String MSG_ERR_ENCRYPTION = "Data encryption error";
    private static final String MSG_ERR_ALG_EXCEPTION = "AES/GCM/NoPadding algorithm exception";

    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String VERSION = "2";
    private static final int AUTH_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 64;
    private static final int PBKDF2_ITERATIONS_COUNT = 10000;
    private final Charset charset;

    public DefaultCrypto(Charset charset) {
        this.charset = charset;
    }

    public String encrypt(String text, SecretKey secretKey) throws StorageCryptoException {
        byte[] clean = text.getBytes(charset);
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        byte[] key = getKey(salt, secretKey);
        byte[] iv = generateRandomBytes(IV_LENGTH);

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, SECRET_KEY_ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH * 8, iv);

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
        return new String(encoded, charset);
    }

    public String decrypt(String cipherText, SecretKey secretKey) throws StorageCryptoException {
        byte[] bytes = Base64.getDecoder().decode(cipherText);
        return decodeBytes(bytes, secretKey);
    }

    public String decryptV1(String cipherText, SecretKey secretKey) throws StorageCryptoException {
        byte[] bytes = DatatypeConverter.parseHexBinary(cipherText);
        return decodeBytes(bytes, secretKey);
    }

    private String decodeBytes(byte[] decodedBytes, SecretKey secretKey) throws StorageCryptoException {
        byte[] salt = Arrays.copyOfRange(decodedBytes, 0, 64);
        byte[] iv = Arrays.copyOfRange(decodedBytes, 64, 76);
        byte[] encrypted = Arrays.copyOfRange(decodedBytes, 76, decodedBytes.length);

        byte[] decryptedText;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            byte[] key = getKey(salt, secretKey);

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            decryptedText = cipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throw new StorageCryptoException(MSG_ERR_ENCRYPTION, e);
        }
        return new String(decryptedText, charset);
    }

    private byte[] generateStrongPasswordHash(byte[] password, byte[] salt, int iterations, int length) throws StorageCryptoException {
        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(password));
        char[] chars = charBuffer.array();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, length * 8);
        byte[] strongPasswordHash;
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
            strongPasswordHash = skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new StorageCryptoException(MSG_ERR_NO_ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throw new StorageCryptoException(MSG_ERR_GEN_SECRET, e);
        }
        return strongPasswordHash;
    }

    private byte[] getKey(byte[] salt, SecretKey secretKey) throws StorageCryptoException {
        if (secretKey.isKey()) {
            return secretKey.getSecret();
        }
        return generateStrongPasswordHash(secretKey.getSecret(), salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);
    }

    private static byte[] generateRandomBytes(int length) {
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        randomSecureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    @Override
    public boolean isCurrent() {
        return true;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
}
