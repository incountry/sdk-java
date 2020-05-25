package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;

import java.util.List;

/**
 * container with Storage configuration, using pattern 'builder'
 */
public class StorageConfig {

    public static final String MSG_SECURE = "[SECURE[%s]]";

    private String envId;
    private String apiKey;
    private String endPoint;
    private SecretKeyAccessor secretKeyAccessor;
    private List<Crypto> customEncryptionConfigsList;
    private boolean normalizeKeys;
    private String clientId;
    private String clientSecret;
    private String authEndPoint;
    private Integer httpTimeout;

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

    public List<Crypto> getCustomEncryptionConfigsList() {
        return customEncryptionConfigsList;
    }

    /**
     * for custom encryption
     *
     * @param customEncryptionConfigsList List with custom encryption functions
     * @return StorageConfig
     */
    public StorageConfig setCustomEncryptionConfigsList(List<Crypto> customEncryptionConfigsList) {
        this.customEncryptionConfigsList = customEncryptionConfigsList;
        return this;
    }

    public boolean isNormalizeKeys() {
        return normalizeKeys;
    }

    /**
     * if true - all keys will be stored as lower cased. default is false
     *
     * @param normalizeKeys value
     * @return StorageConfig
     */
    public StorageConfig setNormalizeKeys(boolean normalizeKeys) {
        this.normalizeKeys = normalizeKeys;
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

    public Integer getHttpTimeout() {
        return httpTimeout;
    }

    /**
     * Set HTTP requests timeout. Parameter is optional. Should be greater than 0.
     * Default value is 30 seconds.
     *
     * @param httpTimeout timeout in seconds
     * @return StorageConfig
     */
    public StorageConfig setHttpTimeout(Integer httpTimeout) {
        this.httpTimeout = httpTimeout;
        return this;
    }

    public StorageConfig copy() {
        StorageConfig newInstance = new StorageConfig();
        newInstance.setEnvId(getEnvId());
        newInstance.setApiKey(getApiKey());
        newInstance.setEndPoint(getEndPoint());
        newInstance.setSecretKeyAccessor(getSecretKeyAccessor());
        newInstance.setCustomEncryptionConfigsList(getCustomEncryptionConfigsList());
        newInstance.setNormalizeKeys(isNormalizeKeys());
        newInstance.setClientId(getClientId());
        newInstance.setClientSecret(getClientSecret());
        newInstance.setAuthEndPoint(getAuthEndPoint());
        newInstance.setHttpTimeout(getHttpTimeout());
        return newInstance;
    }

    @Override
    public String toString() {
        return "StorageConfig{" +
                "envId='" + hideParam(envId) + '\'' +
                ", apiKey='" + hideParam(apiKey) + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", secretKeyAccessor=" + secretKeyAccessor +
                ", customEncryptionConfigsList=" + customEncryptionConfigsList +
                ", ignoreKeyCase=" + normalizeKeys +
                ", clientId='" + hideParam(clientId) + '\'' +
                ", clientSecret='" + hideParam(clientSecret) + '\'' +
                ", authEndPoint='" + authEndPoint + '\'' +
                '}';
    }

    private String hideParam(String param) {
        return param != null ? String.format(MSG_SECURE, param.hashCode()) : null;
    }
}
