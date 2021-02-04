package com.incountry.residence.sdk.tools.crypto;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.crypto.EncryptionKey;
import com.incountry.residence.sdk.tools.crypto.EncryptionSecret;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.nio.cs.UTF_8;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

///**
// * Generator for {@link com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData}
// */
public class SecretsDataGenerator {

    private static final Logger LOG = LogManager.getLogger(SecretsDataGenerator.class);

    public static final int DEFAULT_VERSION = 0;
    private static final String MSG_ERR_INCORRECT_SECRETS = "Incorrect JSON with SecretsData";
    private static final String MSG_ERR_NULL_SECRETS = "Incorrect JSON with SecretsData";
    private static final String MSG_ERR_BASE64_SECRET = "Secret key must be base64-encoded string";
    private static final String MSG_ERR_OPTION = "SecretKey can have either 'isKey' or 'isForCustomEncryption' set to true, not both";

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
        EncryptionSecret secret = new EncryptionSecret(DEFAULT_VERSION, password.getBytes(StandardCharsets.UTF_8));
        List secretKeys = new ArrayList() {{
            add(secret);
        }};
        return new SecretsData(secretKeys, secret);
    }

    /**
     * create SecretsData with SecretsData as JSON String
     *
     * @param secretsDataJson SecretsData in JSON String
     * @return SecretsData
     * @throws StorageClientException when parameter validation fails
     */
    public static SecretsData fromJson(String secretsDataJson) throws StorageClientException {
        try {
            SecretsDataContainer container = new Gson().fromJson(secretsDataJson, SecretsDataContainer.class);
            Integer currentSecretVersion = container.currentVersion;
            List<Secret> secrets = new ArrayList<>();
            if (container.secrets == null) {
                LOG.error(MSG_ERR_NULL_SECRETS);
                throw new StorageClientException(MSG_ERR_NULL_SECRETS);
            }
            Secret currentSecret = null;
            for (SecretKeyContainer secret : container.secrets) {
                if (secret.isKey && secret.isForCustomEncryption) {
                    LOG.error(MSG_ERR_OPTION);
                    throw new StorageClientException(MSG_ERR_OPTION);
                }
                Secret newSecret;
                if (secret.isForCustomEncryption) {
                    newSecret = new CustomEncryptionKey(secret.version, secret.secret.getBytes(StandardCharsets.UTF_8));
                }
                else if (secret.isKey) {
                    newSecret = new EncryptionKey(secret.version,secret.secret.getBytes(StandardCharsets.UTF_8));
                }
                else {
                    newSecret = new EncryptionSecret(secret.version, secret.secret.getBytes(StandardCharsets.UTF_8));
                }
                secrets.add(newSecret);
                if (secret.version == currentSecretVersion) {
                    currentSecret = newSecret;
                }
            }
            return new SecretsData(secrets, currentSecret);
        } catch (JsonSyntaxException | NullPointerException e) {
            throw new StorageClientException(MSG_ERR_INCORRECT_SECRETS, e);
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
