package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;

import java.util.List;

/**
 * container with Storage configuration, using pattern 'builder'
 */
public class StorageConfig {
    private String envId;
    private String apiKey;
    private String endPoint;
    private SecretKeyAccessor secretKeyAccessor;
    private List<Crypto> customCryptoList;
    private boolean ignoreKeyCase;

    public String getEnvId() {
        return envId;
    }

    /**
     * sets environment ID
     *
     * @param envId environment ID
     * @return StorageConfig
     */
    public StorageConfig setEnvId(String envId) {
        this.envId = envId;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * sets API key
     *
     * @param apiKey API key
     * @return StorageConfig
     */
    public StorageConfig setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getEndPoint() {
        return endPoint;
    }

    /**
     * Defines PoP API URL
     *
     * @param endPoint API URL. Default endpoint will be used if this param is null
     * @return StorageConfig
     */
    public StorageConfig setEndPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public SecretKeyAccessor getSecretKeyAccessor() {
        return secretKeyAccessor;
    }

    /**
     * Set accessor for fetching encryption secret.
     *
     * @param secretKeyAccessor Instance of SecretKeyAccessor class.
     * @return StorageConfig
     */
    public StorageConfig setSecretKeyAccessor(SecretKeyAccessor secretKeyAccessor) {
        this.secretKeyAccessor = secretKeyAccessor;
        return this;
    }

    public List<Crypto> getCustomCryptoList() {
        return customCryptoList;
    }

    /**
     * for custom encryption
     *
     * @param customCryptoList List with custom encryption functions
     * @return StorageConfig
     */
    public StorageConfig setCustomCryptoList(List<Crypto> customCryptoList) {
        this.customCryptoList = customCryptoList;
        return this;
    }

    public boolean isIgnoreKeyCase() {
        return ignoreKeyCase;
    }

    /**
     * if true - all keys will be stored as lower cased. default is false
     *
     * @param ignoreKeyCase value
     * @return StorageConfig
     */
    public StorageConfig setIgnoreKeyCase(boolean ignoreKeyCase) {
        this.ignoreKeyCase = ignoreKeyCase;
        return this;
    }

    public StorageConfig copy() {
        StorageConfig newInstance = new StorageConfig();
        newInstance.setEnvId(getEnvId());
        newInstance.setApiKey(getApiKey());
        newInstance.setEndPoint(getEndPoint());
        newInstance.setSecretKeyAccessor(getSecretKeyAccessor());
        newInstance.setCustomCryptoList(getCustomCryptoList());
        newInstance.setIgnoreKeyCase(isIgnoreKeyCase());
        return newInstance;
    }
}
