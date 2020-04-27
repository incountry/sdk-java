package com.incountry.residence.sdk.tools.keyaccessor;

import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;

/**
 * Accessor to secrets. Method {@link SecretKeyAccessor#getSecretsData()} invokes in each encryption/decryption.
 * You can specify secrets rotation in implementation
 */
public interface SecretKeyAccessor {

    /**
     * get your container with secrets
     *
     * @return SecretsData
     */
    SecretsData getSecretsData();
}
