package com.incountry.keyaccessor.impl;

import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import com.incountry.keyaccessor.utils.SecretKeyUtils;

import java.util.ArrayList;
import java.util.List;

public class SecretKeyAccessorImpl implements SecretKeyAccessor {
    private SecretKeysData secretKeysData;

    public SecretKeyAccessorImpl(String secret) {
        secretKeysData = new SecretKeysData();
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(0);
        List<SecretKey> secrets = new ArrayList<>();
        secrets.add(secretKey);
        secretKeysData.setSecrets(secrets);
        secretKeysData.setCurrentVersion(0);
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