package com.incountry.residence.sdk.tools.keyaccessor.key;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Generator for {@link com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData}
 */
public class SecretsDataGenerator {

    private static final Logger LOG = LogManager.getLogger(SecretsDataGenerator.class);

    public static final int DEFAULT_VERSION = 0;
    private static final String MSG_ERR_INCORRECT_SECRETS = "Incorrect JSON with SecretsData";
    private static final String MSG_ERR_BASE64_SECRET = "Secret must be base64";

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
        SecretKey secretKey = new SecretKey(password.getBytes(StandardCharsets.UTF_8), DEFAULT_VERSION, false);
        List<SecretKey> secretKeys = new ArrayList<>();
        secretKeys.add(secretKey);
        return new SecretsData(secretKeys, DEFAULT_VERSION);
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
            List<SecretKey> secrets = new ArrayList<>();
            if (container.secrets != null) {
                for (SecretKeyContainer key : container.secrets) {
                    byte[] byteKey = key.secret.getBytes(StandardCharsets.UTF_8);
                    base64Validation(key.isKey, byteKey);
                    secrets.add(new SecretKey(byteKey, key.version, key.isKey, key.isForCustomEncryption));
                }
            }
            result = new SecretsData(secrets, container.currentVersion);
        } catch (JsonSyntaxException | NullPointerException e) {
            throw new StorageClientException(MSG_ERR_INCORRECT_SECRETS, e);
        }
        return result;
    }

    private static void base64Validation(boolean isKey, byte[] byteKey) throws StorageClientException {
        if (isKey && !Base64.isBase64(byteKey)) {
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
