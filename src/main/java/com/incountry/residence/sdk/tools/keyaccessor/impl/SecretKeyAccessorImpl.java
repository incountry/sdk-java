package com.incountry.residence.sdk.tools.keyaccessor.impl;

import com.incountry.residence.sdk.tools.keyaccessor.generator.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.utils.SecretsDataUtils;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SecretKeyAccessorImpl implements SecretKeyAccessor {

    private static final Logger LOG = LogManager.getLogger(SecretKeyAccessorImpl.class);

    private static final String MSG_ERROR = "SecretsDataGenerator returns invalid type. Type must be String or SecretsData";
    private static final String MSG_NULL_GENETATOR = "SecretsDataGenerator is null";
    private static final String MSG_NULL_KEY = "SecretsDataGenerator returns null key";

    private SecretsData secretsData;

    public SecretKeyAccessorImpl(String secret) {
        secretsData = SecretsDataUtils.getSecretsDataFromString(secret);
    }

    public SecretKeyAccessorImpl(SecretsDataGenerator secretsDataGenerator) {
        if (secretsDataGenerator == null) {
            LOG.error(MSG_NULL_GENETATOR);
            throw new IllegalArgumentException(MSG_NULL_GENETATOR);
        }
        Object secretKey = secretsDataGenerator.generate();
        if (secretKey == null) {
            LOG.error(MSG_NULL_KEY);
            throw new IllegalArgumentException(MSG_NULL_KEY);
        } else if (secretKey instanceof String) {
            secretsData = SecretsDataUtils.getSecretsDataFromString((String) secretKey);
        } else if (secretKey instanceof SecretsData) {
            SecretsData temp = (SecretsData) secretKey;
            SecretsDataUtils.validateSecretsData(temp);
            secretsData = temp;
        } else {
            LOG.error(MSG_ERROR);
            throw new IllegalArgumentException(MSG_ERROR);
        }
    }

    @Override
    public SecretsData getSecretsData() {
        return secretsData;
    }
}
