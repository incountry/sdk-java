package com.incountry.keyaccessor.impl;

import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.keyaccessor.model.SecretKeysData;
import com.incountry.keyaccessor.utils.SecretKeyUtils;

public class SecretKeyAccessorImpl implements SecretKeyAccessor {
    private String secret;
    private SecretKeysData secretKeys;
//    private SecretKeyGenerator secret;

    public SecretKeyAccessorImpl(String secret) {
//        this.secret = secret;
    }

    public SecretKeyAccessorImpl(SecretKeyGenerator secretKeyGenerator) {
        Object secetKey = secretKeyGenerator.generate();
        if (secetKey instanceof String) {
            secretKeys = SecretKeyUtils.convertStringToSecretKeyData((String) secetKey);
        }

    }

//    @Override
//    public SecretKeyGenerator getKey() {
//        return null;
//    }

    @Override
    public String getKey() {
        return this.secret;
    }
}
