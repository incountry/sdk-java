package com.incountry.residence.sdk.tools.keyaccessor;

import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;

/**
 * Accessor to secrets. Used only during initialising of {@link com.incountry.residence.sdk.Storage}
 */
public interface SecretKeyAccessor {

    /**
     * get your container with secrets
     *
     * @return SecretsData
     */
    SecretsData getSecretsData();
}
