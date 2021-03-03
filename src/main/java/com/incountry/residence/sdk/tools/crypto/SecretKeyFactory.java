package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.crypto.EncryptionSecret;
import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.spec.PBEKeySpec;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class SecretKeyFactory {

    private static final Logger LOG = LogManager.getLogger(SecretKeyFactory.class);

    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA512";

    private static final String MSG_ERR_NULL_SECRET = "Secret is null";
    private static final String MSG_ERR_GEN_SECRET = "Secret generation exception";
    private static final String MSG_ERR_NO_ALGORITHM = "Unable to generate secret - cannot find PBKDF2WithHmacSHA512 algorithm. Please, check your JVM configuration";

    private static final int KEY_LENGTH = 32;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private SecretKeyFactory() {
    }

    public static byte[] getKey(byte[] salt, Secret secret, int pbkdf2Iterations) throws StorageCryptoException {
        if (secret != null) {
            return (secret instanceof EncryptionSecret)
                    ? getPbkdf2WithHmacSha512(secret.getSecretBytes(), salt, pbkdf2Iterations, KEY_LENGTH)
                    : secret.getSecretBytes();
        }
        LOG.error(MSG_ERR_NULL_SECRET);
        throw new StorageCryptoException(MSG_ERR_NULL_SECRET);
    }

    private static byte[] getPbkdf2WithHmacSha512(byte[] passwordBytes, byte[] salt, int iterations, int length) throws StorageCryptoException {
        CharBuffer charBuffer = CHARSET.decode(ByteBuffer.wrap(passwordBytes));
        char[] chars = charBuffer.array();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, length * 8);
        byte[] strongPasswordHash;
        try {
            javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
            strongPasswordHash = skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new StorageCryptoException(MSG_ERR_NO_ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throw new StorageCryptoException(MSG_ERR_GEN_SECRET, e);
        }
        return strongPasswordHash;
    }

}
