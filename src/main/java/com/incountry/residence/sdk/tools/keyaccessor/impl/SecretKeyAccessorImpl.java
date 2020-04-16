package com.incountry.residence.sdk.tools.keyaccessor.impl;

import com.incountry.residence.sdk.tools.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.utils.SecretKeyUtils;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SecretKeyAccessorImpl implements SecretKeyAccessor {

    private static final Logger LOG = LogManager.getLogger(SecretKeyAccessorImpl.class);

    private static final String MSG_ERROR = "SecretKeyGenerator returns invalid type. Type must be String or SecretKeysData";
    private static final String MSG_NULL_GENETATOR = "SecretKeyGenerator is null";
    private static final String MSG_NULL_KEY = "SecretKeyGenerator returns null key";

    private SecretKeysData secretKeysData;

    public SecretKeyAccessorImpl(String secret) {
        secretKeysData = SecretKeyUtils.getSecretKeyDataFromString(secret);
    }

    public SecretKeyAccessorImpl(SecretKeyGenerator secretKeyGenerator) {
        if (secretKeyGenerator == null) {
            LOG.error(MSG_NULL_GENETATOR);
            throw new IllegalArgumentException(MSG_NULL_GENETATOR);
        }
        Object secretKey = secretKeyGenerator.generate();
        if (secretKey == null) {
            LOG.error(MSG_NULL_KEY);
            throw new IllegalArgumentException(MSG_NULL_KEY);
        } else if (secretKey instanceof String) {
            secretKeysData = SecretKeyUtils.getSecretKeyDataFromString((String) secretKey);
        } else if (secretKey instanceof SecretKeysData) {
            SecretKeysData temp = (SecretKeysData) secretKey;
            SecretKeyUtils.validateSecretKeysData(temp);
            secretKeysData = temp;
        } else {
            LOG.error(MSG_ERROR);
            throw new IllegalArgumentException(MSG_ERROR);
        }
    }

    @Override
    public SecretKeysData getKey() {
        return secretKeysData;
    }
}
