package com.incountry.residence.sdk.tools.keyaccessor.utils;

import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class SecretsDataUtils {

    private static final Logger LOG = LogManager.getLogger(SecretsDataUtils.class);

    private static final String MSG_ERR_NULL_SECRETS = "Secrets in SecretData are null";

    public static final int DEFAULT_VERSION = 0;

    private SecretsDataUtils() {
    }

    /**
     * Convert string to SecretsData object
     *
     * @param secretKeyString simple string or json
     * @return SecretsData object which contain secret keys and there versions
     */
    public static SecretsData getSecretsDataFromString(String secretKeyString) {
        SecretsData data = JsonUtils.getSecretsDataFromJson(secretKeyString);
        if (data != null) {
            validateSecretsData(data);
            return data;
        }
        SecretKey secretKey = new SecretKey(secretKeyString, DEFAULT_VERSION, false);
        List<SecretKey> secretKeys = new ArrayList<>();
        secretKeys.add(secretKey);
        SecretsData secretsData = new SecretsData();
        secretsData.setSecrets(secretKeys);
        secretsData.setCurrentVersion(DEFAULT_VERSION);
        return secretsData;
    }

    public static void validateSecretsData(SecretsData data) {
        data.validateVersion(data.getCurrentVersion());
        if (data.getSecrets() == null || data.getSecrets().isEmpty()) {
            LOG.error(MSG_ERR_NULL_SECRETS);
            throw new IllegalArgumentException(MSG_ERR_NULL_SECRETS);
        }
        data.getSecrets().forEach(one ->
                SecretKey.validateSecretKey(one.getSecret(), one.getVersion(), one.getIsKey())
        );
    }
}
