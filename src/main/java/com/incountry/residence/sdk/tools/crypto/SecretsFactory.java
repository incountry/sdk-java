package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class SecretsFactory {

    private static final Logger LOG = LogManager.getLogger(SecretsFactory.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final String MSG_ERR_NULL_SECRET = "Secret is null";
    private static final String MSG_ERR_NO_ALGORITHM = "Unable to generate secret - cannot find PBKDF2WithHmacSHA512 algorithm. Please, check your JVM configuration";
    private static final String MSG_ERR_GEN_SECRET = "Secret generation exception";

    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int KEY_LENGTH = 32;

    private SecretsFactory() {
    }

    public static byte[] getKey(byte[] salt, Secret secret, int pbkdf2Iterations) throws StorageCryptoException {
        if (secret == null) {
            LOG.error(MSG_ERR_NULL_SECRET);
            throw new StorageCryptoException(MSG_ERR_NULL_SECRET);
        }

        return secret instanceof EncryptionSecret
                ? getPbkdf2WithHmacSha512(secret.getSecretBytes(), salt, pbkdf2Iterations, KEY_LENGTH)
                : secret.getSecretBytes();
    }

    private static byte[] getPbkdf2WithHmacSha512(byte[] password, byte[] salt, int iterations, int length) throws StorageCryptoException {
        byte[] strongPasswordHash;
        try {
            CharBuffer charBuffer = CHARSET.decode(ByteBuffer.wrap(password));
            char[] chars = charBuffer.array();
            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
            strongPasswordHash = skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            throw new StorageCryptoException(MSG_ERR_NO_ALGORITHM, ex);
        } catch (Exception ex) {
            throw new StorageCryptoException(MSG_ERR_GEN_SECRET, ex);
        }
        return strongPasswordHash;
    }

}
