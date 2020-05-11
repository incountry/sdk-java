package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;

import java.util.List;

/**
 * container with Storage configuration, using pattern 'builder'
 */
public class StorageConfig {

    public static final String LOG_SECURE2 = "[SECURE[";

    private String envId;
    private String apiKey;
    private String endPoint;
    private SecretKeyAccessor secretKeyAccessor;
    private List<Crypto> customEncryptionList;
    private boolean ignoreKeyCase;
    private String clientId;
    private String clientSecret;
    private String authEndPoint;

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

    public List<Crypto> getCustomEncryptionList() {
        return customEncryptionList;
    }

    /**
     * for custom encryption
     *
     * @param customEncryptionList List with custom encryption functions
     * @return StorageConfig
     */
    public StorageConfig setCustomEncryptionList(List<Crypto> customEncryptionList) {
        this.customEncryptionList = customEncryptionList;
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

    public String getClientId() {
        return clientId;
    }

    /**
     * Set login for authorisation.
     * Alternative way for authorisation - to use {@link #setApiKey(String)}
     *
     * @param clientId login
     * @return StorageConfig
     */
    public StorageConfig setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Set user secret for authorisation.
     * Alternative way for authorisation - to use {@link #setApiKey(String)}
     *
     * @param clientSecret password
     * @return StorageConfig
     */
    public StorageConfig setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getAuthEndPoint() {
        return authEndPoint;
    }

    /**
     * Set custom authorization server URL. If null - default authorization server will be used.
     * Alternative way for authorisation - to use {@link #setApiKey(String)}
     *
     * @param authEndPoint custom authorization server URL
     * @return StorageConfig
     */
    public StorageConfig setAuthEndPoint(String authEndPoint) {
        this.authEndPoint = authEndPoint;
        return this;
    }

    public StorageConfig copy() {
        StorageConfig newInstance = new StorageConfig();
        newInstance.setEnvId(getEnvId());
        newInstance.setApiKey(getApiKey());
        newInstance.setEndPoint(getEndPoint());
        newInstance.setSecretKeyAccessor(getSecretKeyAccessor());
        newInstance.setCustomEncryptionList(getCustomEncryptionList());
        newInstance.setIgnoreKeyCase(isIgnoreKeyCase());
        newInstance.setClientId(getClientId());
        newInstance.setClientSecret(getClientSecret());
        newInstance.setAuthEndPoint(getAuthEndPoint());
        return newInstance;
    }

    @Override
    public String toString() {
        return "StorageConfig{" +
                "envId='" + hideParam(envId) + '\'' +
                ", apiKey='" + hideParam(apiKey) + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", secretKeyAccessor=" + secretKeyAccessor +
                ", customEncryptionList=" + customEncryptionList +
                ", ignoreKeyCase=" + ignoreKeyCase +
                ", clientId='" + hideParam(clientId) + '\'' +
                ", clientSecret='" + hideParam(clientSecret) + '\'' +
                ", authEndPoint='" + authEndPoint + '\'' +
                '}';
    }

    private String hideParam(String param) {
        return param != null ? LOG_SECURE2 + param.hashCode() + "]]" : null;
    }
}
