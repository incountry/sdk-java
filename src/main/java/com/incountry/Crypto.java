package com.incountry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto
{
    private String SECRET;
    private static final int AUTH_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 64;
    private static final int PBKDF2_ITERATIONS_COUNT = 10000;

    public Crypto(String secret)
    {
        SECRET = secret;
    }

    public static void main(String[] args) throws Exception
    {
        Crypto crypto = new Crypto("123");

        String original = "I am the very model of a modern major general";
        System.out.println(original);

        String enc = crypto.encrypt(original);
        System.out.println(enc);


        String output = crypto.decrypt(enc);
        System.out.println(output);

        String hash = crypto.hash(output);
        System.out.println(hash);

    }

    public String hash(String output) throws GeneralSecurityException, IOException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(SECRET.getBytes("UTF-8"));
        byte[] bytes = digest.digest();
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(bytes, "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return toHex(sha256_HMAC.doFinal(output.getBytes("UTF-8")));
    }


    public String encrypt(String plainText) throws GeneralSecurityException, IOException
    {
        byte[] clean = plainText.getBytes();
        byte[] salt = getSalt();
        byte[] strong = generateStrongPasswordHash(SECRET, salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);

        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        randomSecureRandom.nextBytes(iv);

        SecretKeySpec secretKeySpec = new SecretKeySpec(strong, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH * 8, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(salt);
        outputStream.write(iv);
        outputStream.write(encrypted);

        byte[] res = outputStream.toByteArray( );

        return toHex(res);
    }


    public String decrypt(String cipherText) throws GeneralSecurityException
    {
        byte[] parts = fromHex(cipherText);
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


    public static String toHex(byte[] ba)
    {
        String s = "";
        for (int i=0; i<ba.length; i++)
        {
            String b = Integer.toHexString(0xFF & ba[i]);
            if (b.length()<2) b = "0"+b;
            s += b;
        }
        return s;
    }

    public static byte[] fromHex(String hex)
    {
        int n = hex.length();
        byte[] ba = new byte[n/2];
        int i = 0;
        int j = 0;
        while (i<n)
        {
            String s = hex.substring(i, i+2);
            ba[j++] = (byte)Integer.parseInt(s, 16);
            i += 2;
        }
        return ba;
    }

    private static byte[] generateStrongPasswordHash(String password, byte[] salt, int iterations, int length) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        return skf.generateSecret(spec).getEncoded();
    }

    private byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[SALT_LENGTH];
        sr.nextBytes(salt);
        return salt;
    }

}