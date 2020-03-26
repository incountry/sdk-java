package com.incountry.storage.sdk.tools.keyaccessor;

import com.incountry.storage.sdk.tools.keyaccessor.generator.SecretKeyGenerator;
import com.incountry.storage.sdk.tools.keyaccessor.impl.SecretKeyAccessorImpl;
import com.incountry.storage.sdk.tools.keyaccessor.key.SecretKeysData;

public interface SecretKeyAccessor {

    SecretKeysData getKey();

    static SecretKeyAccessorImpl getAccessor(String secretKeysData) {
        return new SecretKeyAccessorImpl(secretKeysData);
    }

    static SecretKeyAccessorImpl getAccessor(SecretKeyGenerator secretKeyGenerator) {
        return new SecretKeyAccessorImpl(secretKeyGenerator);
    }
}
