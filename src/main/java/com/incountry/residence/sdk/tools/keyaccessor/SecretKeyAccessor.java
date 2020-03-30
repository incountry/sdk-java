package com.incountry.residence.sdk.tools.keyaccessor;

import com.incountry.residence.sdk.tools.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.impl.SecretKeyAccessorImpl;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;

public interface SecretKeyAccessor {

    SecretKeysData getKey();

    static SecretKeyAccessorImpl getAccessor(String secretKeysData) {
        return new SecretKeyAccessorImpl(secretKeysData);
    }

    static SecretKeyAccessorImpl getAccessor(SecretKeyGenerator secretKeyGenerator) {
        return new SecretKeyAccessorImpl(secretKeyGenerator);
    }
}
