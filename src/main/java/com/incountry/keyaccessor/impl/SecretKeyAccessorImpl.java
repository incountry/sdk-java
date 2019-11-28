package com.incountry.keyaccessor.impl;

import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.keyaccessor.key.SecretKey;
import com.incountry.keyaccessor.key.SecretKeysData;
import com.incountry.keyaccessor.utils.SecretKeyUtils;
import com.sun.jdi.InvalidTypeException;

import java.util.ArrayList;

public class SecretKeyAccessorImpl implements SecretKeyAccessor {
    private SecretKeysData secretKeysData;

    public SecretKeyAccessorImpl(String secret) {
        secretKeysData = new SecretKeysData();
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret(secret);
        secretKey.setVersion(0);
        secretKeysData.setSecrets(new ArrayList<SecretKey>() {{
            add(secretKey);
        }});
        secretKeysData.setCurrentVersion(0);
    }

    public SecretKeyAccessorImpl(SecretKeyGenerator secretKeyGenerator) throws InvalidTypeException {
        Object secretKey = secretKeyGenerator.generate();
        if (secretKey instanceof String) {
            secretKeysData = SecretKeyUtils.convertStringToSecretKeyData((String) secretKey);
        } else if (secretKey instanceof SecretKeysData) {
            secretKeysData = (SecretKeysData) secretKey;
        } else {
            throw new InvalidTypeException("SecretKeyGenerator returns invalid type. Type must be String or SecretKeysData.");
        }
    }

    @Override
    public SecretKeysData getKey() {
        return secretKeysData;
    }
}
