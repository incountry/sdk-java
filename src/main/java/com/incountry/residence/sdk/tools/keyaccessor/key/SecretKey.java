package com.incountry.residence.sdk.tools.keyaccessor.key;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;

public class SecretKey {
    private static final Logger LOG = LogManager.getLogger(SecretKey.class);

    private static final int KEY_LENGTH = 32;

    private static final String MSG_ERR_VERSION = "Version must be >= 0";
    private static final String MSG_ERR_NULL_SECRET = "Secret can't be null";
    private static final String MSG_ERR_OPTION = "SecretKey can have either 'isKey' or 'isForCustomEncryption' set to True, not both";
    private static final String MSG_ERR_KEY_LEN = "Wrong key length for secret key with 'isKey==true'. Should be "
            + KEY_LENGTH + "-byte array";

    private final byte[] secret;
    private final int version;
    private final boolean isKey;
    private final boolean isForCustomEncryption;

    /**
     * Creates a secret key
     *
     * @param secret  secret/key as byte array from base64 String
     * @param version secret version, should be a non-negative integer
     * @param isKey   should be True only for user-defined encryption keys
     * @throws StorageClientException when parameter validation fails
     */
    public SecretKey(byte[] secret, int version, boolean isKey) throws StorageClientException {
        validateSecretKey(secret, version, isKey, false);
        this.version = version;
        this.secret = Arrays.copyOf(secret, secret.length);
        this.isKey = isKey;
        this.isForCustomEncryption = false;
    }

    /**
     * @param secret                secret/key as byte array from base64 String
     * @param version               secret version, should be a non-negative integer
     * @param isKey                 should be True only for user-defined encryption keys
     * @param isForCustomEncryption should be True for using this key in custom encryption implementations.
     *                              Either ({@link #isKey} or {@link #isForCustomEncryption}) can be True at the same moment, not both
     * @throws StorageClientException when parameter validation fails
     */
    public SecretKey(byte[] secret, int version, boolean isKey, boolean isForCustomEncryption) throws StorageClientException {
        validateSecretKey(secret, version, isKey, isForCustomEncryption);
        this.version = version;
        this.secret = Arrays.copyOf(secret, secret.length);
        this.isKey = isKey;
        this.isForCustomEncryption = isForCustomEncryption;
    }

    public byte[] getSecret() {
        return Arrays.copyOf(secret, secret.length);
    }

    public int getVersion() {
        return version;
    }

    public boolean isKey() {
        return isKey;
    }

    public boolean isForCustomEncryption() {
        return isForCustomEncryption;
    }

    public static void validateSecretKey(byte[] secret, int version, boolean isKey, boolean isForCustomEncryption) throws StorageClientException {
        if (version < 0) {
            LOG.error(MSG_ERR_VERSION);
            throw new StorageClientException(MSG_ERR_VERSION);
        }
        if (secret == null || secret.length == 0) {
            LOG.error(MSG_ERR_NULL_SECRET);
            throw new StorageClientException(MSG_ERR_NULL_SECRET);
        }
        if (isKey && secret.length != KEY_LENGTH) {
            LOG.error(MSG_ERR_KEY_LEN);
            throw new StorageClientException(MSG_ERR_KEY_LEN);
        }
        if (isKey && isForCustomEncryption) {
            LOG.error(MSG_ERR_OPTION);
            throw new StorageClientException(MSG_ERR_OPTION);
        }
    }

    @Override
    public String toString() {
        return "SecretKey{" +
                "secret=HASH[" + Arrays.hashCode(secret) + ']' +
                ", version=" + version +
                ", isKey=" + isKey +
                ", isForCustomEncryption=" + isForCustomEncryption +
                '}';
    }
}
