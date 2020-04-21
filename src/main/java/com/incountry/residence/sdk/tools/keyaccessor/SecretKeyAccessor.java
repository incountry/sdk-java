package com.incountry.residence.sdk.tools.keyaccessor;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.generator.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.impl.SecretKeyAccessorImpl;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;

public interface SecretKeyAccessor {

    SecretsData getSecretsData();

    static SecretKeyAccessorImpl getAccessor(String secretsDataString) throws StorageClientException {
        return new SecretKeyAccessorImpl(secretsDataString);
    }

    static SecretKeyAccessorImpl getAccessor(SecretsDataGenerator secretsDataGenerator) throws StorageClientException {
        return new SecretKeyAccessorImpl(secretsDataGenerator);
    }
}
