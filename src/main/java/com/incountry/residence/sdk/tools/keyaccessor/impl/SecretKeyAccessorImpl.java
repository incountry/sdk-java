package com.incountry.residence.sdk.tools.keyaccessor.impl;

import com.incountry.residence.sdk.tools.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.utils.SecretKeyUtils;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;

public class SecretKeyAccessorImpl implements SecretKeyAccessor {
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
            throw new IllegalArgumentException("SecretKeyGenerator returns invalid type. Type must be String or SecretKeysData.");
        }
    }

    @Override
    public SecretKeysData getKey() {
        return secretKeysData;
    }
}
