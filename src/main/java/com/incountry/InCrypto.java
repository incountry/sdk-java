package com.incountry;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class InCrypto 
{
	private String SECRET;
	
	public InCrypto(String secret) 
	{
		SECRET = secret;
	}

	public static void main(String[] args) throws Exception 
	{
		InCrypto crypto = new InCrypto("supersecret");
		
//		System.out.println(crypto.decrypt("d7ea667a3fea3d53cd40ec1154c929f2fa3941a723a3bf377c8223eb538a049a342cbc591347ad63e1cb4b1656a81293dad09dee41e6df500dd97f38bdce2525"));
		
		String original = "I am the very model of a modern major general";
		System.out.println(original);

		String enc = crypto.encrypt(original);
		System.out.println(enc);

		String output = crypto.decrypt(enc);
		System.out.println(output);

		String hash = crypto.hash(output);
		System.out.println(hash);
	}

	public String hash(String output) throws Exception 
	{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(SECRET.getBytes("UTF-8"));
        byte[] bytes = digest.digest();
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(bytes, "HmacSHA256");
		sha256_HMAC.init(secret_key);

		return toHex(sha256_HMAC.doFinal(output.getBytes("UTF-8")));	
	}

	public String decrypt(String enc) throws Exception 
	{
		return decrypt(fromHex(enc), SECRET);
	}

	public String encrypt(String original) throws Exception 
	{
		return toHex(encrypt(original, SECRET));
	}

	public static byte[] encrypt(String plainText, String key) throws Exception {
        byte[] clean = plainText.getBytes();
        int keySize = 16;

        // Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key.getBytes("UTF-8"));
        byte[] longkey = digest.digest();
        byte[] keyBytes = new byte[keySize];
        System.arraycopy(longkey, 0, keyBytes, 0, keySize);
        byte[] ivBytes = new byte[keySize];
        System.arraycopy(longkey, keySize, ivBytes, 0, keySize);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);
        return encrypted;
    }

    public static String decrypt(byte[] encryptedBytes, String key) throws Exception {
        int keySize = 16;

        // Hash key.
        byte[] keyBytes = new byte[keySize];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key.getBytes("UTF-8"));
        byte[] longkey = md.digest();
        System.arraycopy(longkey, 0, keyBytes, 0, keySize);
        byte[] ivBytes = new byte[keySize];
        System.arraycopy(longkey, keySize, ivBytes, 0, keySize);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return new String(decrypted);
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
}
