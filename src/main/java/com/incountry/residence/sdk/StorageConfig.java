package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * container with Storage configuration, using pattern 'builder'
 */
public class StorageConfig {

    public static final String MSG_SECURE = "[SECURE[%s]]";
    //params from OS env
    public static final String PARAM_ENV_ID = "INC_ENVIRONMENT_ID";
    public static final String PARAM_API_KEY = "INC_API_KEY";
    public static final String PARAM_ENDPOINT = "INC_ENDPOINT";
    public static final String PARAM_CLIENT_ID = "INC_CLIENT_ID";
    public static final String PARAM_CLIENT_SECRET = "INC_CLIENT_SECRET";

    private String envId;
    private String apiKey;
    private String endPoint;
    private SecretKeyAccessor secretKeyAccessor;
    private List<Crypto> customEncryptionConfigsList;
    private boolean normalizeKeys;
    private String clientId;
    private String clientSecret;
    private String endpointMask;
    private String countriesEndpoint;
    private Integer httpTimeout;
    private Map<String, String> authEndpoints;
    private String defaultAuthEndpoint;

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

    /**
     * load envId from env variable 'INC_ENVIRONMENT_ID'
     *
     * @return StorageConfig
     */
    public StorageConfig useEnvIdFromEnv() {
        this.envId = loadFromEnv(PARAM_ENV_ID);
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

    /**
     * load apiKey from env variable 'INC_API_KEY'
     *
     * @return StorageConfig
     */
    public StorageConfig useApiKeyFromEnv() {
        this.apiKey = loadFromEnv(PARAM_API_KEY);
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

    /**
     * load endPoint from env variable 'INC_ENDPOINT'
     *
     * @return StorageConfig
     */
    public StorageConfig useEndPointFromEnv() {
        this.endPoint = loadFromEnv(PARAM_ENDPOINT);
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
        return customEncryptionConfigsList == null ? null : new ArrayList<>(customEncryptionConfigsList);
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
     * Set login for oAuth authorization, can be also set via environment variable INC_CLIENT_ID.
     * Alternative way for authorisation - to use {@link #setApiKey(String)}
     *
     * @param clientId login
     * @return StorageConfig
     */
    public StorageConfig setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * load clientId from env variable 'INC_CLIENT_ID'
     *
     * @return StorageConfig
     */
    public StorageConfig useClientIdFromEnv() {
        this.clientId = loadFromEnv(PARAM_CLIENT_ID);
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Set user secret for oAuth authorization, can be also set via environment variable INC_CLIENT_SECRET.
     * Alternative way for authorisation - to use {@link #setApiKey(String)}
     *
     * @param clientSecret password
     * @return StorageConfig
     */
    public StorageConfig setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * load clientSecret from env variable 'INC_CLIENT_SECRET'
     *
     * @return StorageConfig
     */
    public StorageConfig useClientSecretFromEnv() {
        this.clientSecret = loadFromEnv(PARAM_CLIENT_SECRET);
        return this;
    }

    public Map<String, String> getAuthEndpoints() {
        return authEndpoints != null ? new HashMap<>(authEndpoints) : null;
    }

    /**
     * Set custom OAuth authorization server URLs for different regions
     * Can be used only with {@link #setDefaultAuthEndpoint(String)}
     * Format: key = region, value = authorization server URL for region
     *
     * @param authEndpoints map with custom authorisation endpoints for different regions
     * @return StorageConfig
     */
    public StorageConfig setAuthEndpoints(Map<String, String> authEndpoints) {
        this.authEndpoints = authEndpoints;
        return this;
    }

    public String getDefaultAuthEndpoint() {
        return defaultAuthEndpoint;
    }

    /**
     * Set custom oAuth authorization server URL, will be used as default one.
     * Can't be null when {@link #setAuthEndpoints(Map)} is used
     *
     * @param defaultAuthEndpoint custom authorisation endpoint
     * @return StorageConfig
     */
    public StorageConfig setDefaultAuthEndpoint(String defaultAuthEndpoint) {
        this.defaultAuthEndpoint = defaultAuthEndpoint;
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

    public String getEndpointMask() {
        return endpointMask;
    }

    /**
     * Parameter endpointMask is used for switching from `default` InCountry host family (-mt-01.api.incountry.io) to a different one.
     *
     * @param endpointMask template
     * @return StorageConfig
     */
    public StorageConfig setEndpointMask(String endpointMask) {
        this.endpointMask = endpointMask;
        return this;
    }

    public String getCountriesEndpoint() {
        return countriesEndpoint;
    }

    /**
     * Set custom endpoint for loading countries list. Parameter is optional.
     *
     * @param countriesEndpoint custom endpoint for countries loading
     * @return StorageConfig
     */
    public StorageConfig setCountriesEndpoint(String countriesEndpoint) {
        this.countriesEndpoint = countriesEndpoint;
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
        newInstance.setEndpointMask(getEndpointMask());
        newInstance.setCountriesEndpoint(getCountriesEndpoint());
        newInstance.setHttpTimeout(getHttpTimeout());
        newInstance.setAuthEndpoints(getAuthEndpoints());
        newInstance.setDefaultAuthEndpoint(getDefaultAuthEndpoint());
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
                ", endpointMask='" + endpointMask + '\'' +
                ", countriesEndpoint='" + countriesEndpoint + '\'' +
                ", authEndpointMap='" + authEndpoints + '\'' +
                ", defaultAuthEndpoint='" + defaultAuthEndpoint + '\'' +
                ", httpTimeout='" + httpTimeout + '\'' +
                '}';
    }

    private String hideParam(String param) {
        return param != null ? String.format(MSG_SECURE, param.hashCode()) : null;
    }

    private static String loadFromEnv(String key) {
        return System.getenv(key);
    }
}
