package com.incountry.residence.sdk.tools.keyaccessor.impl;

import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.keyaccessor.generator.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class SecretKeyAccessorImpl implements SecretKeyAccessor {

    private static final Logger LOG = LogManager.getLogger(SecretKeyAccessorImpl.class);

    private static final String MSG_ERROR = "SecretsDataGenerator returns invalid type. Type must be String or SecretsData";
    private static final String MSG_NULL_GENETATOR = "SecretsDataGenerator is null";
    private static final String MSG_NULL_KEY = "SecretsDataGenerator returns null key";

    public static final int DEFAULT_VERSION = 0;

    private SecretsData secretsData;

    public SecretKeyAccessorImpl(String secret) {
        secretsData = getSecretsDataFromString(secret);
    }

    public SecretKeyAccessorImpl(SecretsDataGenerator secretsDataGenerator) {
        if (secretsDataGenerator == null) {
            LOG.error(MSG_NULL_GENETATOR);
            throw new IllegalArgumentException(MSG_NULL_GENETATOR);
        }
        Object someSecret = secretsDataGenerator.generate();
        if (someSecret == null) {
            LOG.error(MSG_NULL_KEY);
            throw new IllegalArgumentException(MSG_NULL_KEY);
        } else if (someSecret instanceof String) {
            secretsData = getSecretsDataFromString((String) someSecret);
        } else if (someSecret instanceof SecretsData) {
            SecretsData temp = (SecretsData) someSecret;
            SecretsData.validate(temp.getSecrets(), temp.getCurrentVersion());
            secretsData = temp;
        } else {
            LOG.error(MSG_ERROR);
            throw new IllegalArgumentException(MSG_ERROR);
        }
    }

    /**
     * Convert string to SecretsData object
     *
     * @param secretKeyString simple string or json
     * @return SecretsData object which contain secret keys and there versions
     */
    private static SecretsData getSecretsDataFromString(String secretKeyString) {
        SecretsData data = JsonUtils.getSecretsDataFromJson(secretKeyString);
        if (data != null) {
            SecretsData.validate(data.getSecrets(), data.getCurrentVersion());
            return data;
        }
        SecretKey secretKey = new SecretKey(secretKeyString, DEFAULT_VERSION, false);
        List<SecretKey> secretKeys = new ArrayList<>();
        secretKeys.add(secretKey);
        return new SecretsData(secretKeys, DEFAULT_VERSION);
    }

    @Override
    public SecretsData getSecretsData() {
        return secretsData;
    }
}
