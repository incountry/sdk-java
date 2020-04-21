package com.incountry.residence.sdk.tools.keyaccessor;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.generator.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.keyaccessor.impl.SecretKeyAccessorImpl;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;

public interface SecretKeyAccessor {

    SecretsData getSecretsData();

    /**
     * create SecretKeyAccessor from String
     *
     * @param secretsDataString simple password or SecretsData in JSON
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    static SecretKeyAccessorImpl getAccessor(String secretsDataString) throws StorageClientException {
        return new SecretKeyAccessorImpl(secretsDataString);
    }

    /**
     * create SecretKeyAccessor with SecretsDataGenerator
     *
     * @param secretsDataGenerator non-empty generator of SecretsData
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    static SecretKeyAccessorImpl getAccessor(SecretsDataGenerator secretsDataGenerator) throws StorageClientException {
        return new SecretKeyAccessorImpl(secretsDataGenerator);
    }
}
