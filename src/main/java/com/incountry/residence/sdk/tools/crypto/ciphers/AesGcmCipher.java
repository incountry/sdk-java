package com.incountry.residence.sdk.tools.crypto.ciphers;

import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.crypto.SecretsFactory;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@SuppressWarnings("java:S1118")
public abstract class AesGcmCipher {

    private static final Logger LOG = LogManager.getLogger(AesGcmCipher.class);

    private static final String MSG_ERR_ENCRYPTED_TEXT = "Encrypted text is incorrect";
    private static final String MSG_ERR_NO_SECRET = "No secret provided";
    private static final String MSG_ERR_NO_CHARSET = "Charset is null";
    private static final String MSG_ERR_DECRYPTION = "Invalid cipher for decryption";

    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    protected static final int AUTH_TAG_LENGTH_IN_BITS = 128;
    protected static final int SALT_LENGTH = 64;
    protected static final int IV_LENGTH = 12;
    private static final int META_INFO_LENGTH = SALT_LENGTH + IV_LENGTH;

    protected static String decodeBytes(byte[] decodedBytes, Secret secret, int pbkdf2Iterations, Charset charset) throws StorageCryptoException {
        if (decodedBytes.length < META_INFO_LENGTH) {
            LOG.error(MSG_ERR_ENCRYPTED_TEXT);
            throw new StorageCryptoException(MSG_ERR_ENCRYPTED_TEXT);
        }
        if (secret == null) {
            LOG.error(MSG_ERR_NO_SECRET);
            throw new StorageCryptoException(MSG_ERR_NO_SECRET);
        }
        if (charset == null) {
            LOG.error(MSG_ERR_NO_CHARSET);
            throw new StorageCryptoException(MSG_ERR_NO_CHARSET);
        }

        byte[] salt = Arrays.copyOfRange(decodedBytes, 0, SALT_LENGTH);
        byte[] iv = Arrays.copyOfRange(decodedBytes, SALT_LENGTH, META_INFO_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(decodedBytes, META_INFO_LENGTH, decodedBytes.length);

        byte[] decryptedText;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            byte[] key = SecretsFactory.getKey(salt, secret, pbkdf2Iterations);

            SecretKeySpec keySpec = new SecretKeySpec(key, SECRET_KEY_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH_IN_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            decryptedText = cipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throw new StorageCryptoException(MSG_ERR_DECRYPTION, e);
        }
        return new String(decryptedText, charset);
    }
}
