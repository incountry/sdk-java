package com.incountry.keyaccessor;

import com.incountry.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.keyaccessor.impl.SecretKeyAccessorImpl;
import com.incountry.keyaccessor.key.SecretKeysData;

public interface SecretKeyAccessor {

    SecretKeysData getKey();

    static SecretKeyAccessorImpl getAccessor(String secretKeysData) {
        return new SecretKeyAccessorImpl(secretKeysData);
    }

    static SecretKeyAccessorImpl getAccessor(SecretKeyGenerator secretKeyGenerator) {
        return new SecretKeyAccessorImpl(secretKeyGenerator);
    }
}
