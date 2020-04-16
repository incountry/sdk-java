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

    private SecretKeysData secretKeysData;

    public SecretKeyAccessorImpl(String secret) {
        secretKeysData = SecretKeyUtils.convertStringToSecretKeyData(secret);
    }

    public SecretKeyAccessorImpl(SecretKeyGenerator secretKeyGenerator) {
        Object secretKey = secretKeyGenerator.generate();
        if (secretKey instanceof String) {
            secretKeysData = SecretKeyUtils.convertStringToSecretKeyData((String) secretKey);
        } else if (secretKey instanceof SecretKeysData) {
            secretKeysData = (SecretKeysData) secretKey;
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
