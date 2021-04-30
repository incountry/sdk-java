package com.incountry.residence.sdk.crypto;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Generator for {@link SecretsData}
 */
public class SecretsDataGenerator {

    private static final Logger LOG = LogManager.getLogger(SecretsDataGenerator.class);

    public static final int DEFAULT_VERSION = 0;
    private static final String MSG_ERR_INCORRECT_SECRETS = "Incorrect JSON with SecretsData";
    private static final String MSG_ERR_BASE64_SECRET = "Secret key must be base64-encoded string";

    private SecretsDataGenerator() {
    }

    /**
     * create SecretKeyAccessor with password as String
     *
     * @param password simple password
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    public static SecretsData fromPassword(String password) throws StorageClientException {
        Secret secret = new EncryptionSecret(DEFAULT_VERSION, password != null ? password.getBytes(StandardCharsets.UTF_8) : null);
        List<Secret> secretKeys = new ArrayList<>();
        secretKeys.add(secret);
        return new SecretsData(secretKeys, secret);
    }

    /**
     * create SecretKeyAccessor with SecretsData as JSON String
     *
     * @param secretsDataJson SecretsData in JSON String
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    public static SecretsData fromJson(String secretsDataJson) throws StorageClientException {
        SecretsData result;
        try {
            SecretsDataContainer container = new Gson().fromJson(secretsDataJson, SecretsDataContainer.class);
            List<Secret> secrets = new ArrayList<>();
            Secret currentSecret = null;
            if (container.secrets != null) {
                for (SecretKeyContainer keyContainer : container.secrets) {
                    Secret secret = getSecret(keyContainer);
                    if (container.currentVersion.equals(keyContainer.version)) {
                        currentSecret = secret;
                    }
                    secrets.add(secret);
                }
            }
            result = new SecretsData(secrets, currentSecret);
        } catch (JsonSyntaxException | NullPointerException e) {
            throw new StorageClientException(MSG_ERR_INCORRECT_SECRETS, e);
        }
        return result;
    }

    private static Secret getSecret(SecretKeyContainer keyContainer) throws StorageClientException {
        Secret secret;
        if (keyContainer.isForCustomEncryption || keyContainer.isKey) {
            base64Validation(keyContainer.secret);
            byte[] byteKey = DatatypeConverter.parseBase64Binary(keyContainer.secret);
            secret = keyContainer.isForCustomEncryption
                    ? new CustomEncryptionKey(byteKey, keyContainer.version)
                    : new EncryptionKey(byteKey, keyContainer.version);

        } else {
            byte[] byteKey = keyContainer.secret.getBytes(StandardCharsets.UTF_8);
            secret = new EncryptionSecret(keyContainer.version, byteKey);
        }
        return secret;
    }

    private static void base64Validation(String byteKey) throws StorageClientException {
        if (!Base64.isBase64(byteKey)) {
            LOG.error(MSG_ERR_BASE64_SECRET);
            throw new StorageClientException(MSG_ERR_BASE64_SECRET);
        }
    }

    private static class SecretsDataContainer {
        List<SecretKeyContainer> secrets;
        Integer currentVersion;
    }

    private static class SecretKeyContainer {
        String secret;
        Integer version;
        boolean isKey;
        boolean isForCustomEncryption;
    }
}
