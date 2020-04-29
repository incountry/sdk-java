package com.incountry.residence.sdk.tools.keyaccessor;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;

/**
 * Secrets accessor. Method {@link SecretKeyAccessor#getSecretsData()} is invoked on each encryption/decryption.
 */
public interface SecretKeyAccessor {

    /**
     * get your container with secrets
     *
     * @return SecretsData
     * @throws StorageClientException when something goes wrong during getting secrets
     */
    SecretsData getSecretsData() throws StorageClientException;
}
