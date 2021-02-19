package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;

import java.util.HashMap;
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

    private String environmentId;
    private String apiKey;
    private String endPoint;
    private SecretKeyAccessor secretKeyAccessor;
    private CryptoProvider cryptoProvider;
    private String clientId;
    private String clientSecret;
    private boolean normalizeKeys;
    private String endpointMask;
    private String countriesEndpoint;
    private Integer httpTimeout;
    private Map<String, String> authEndpoints;
    private String defaultAuthEndpoint;
    private Integer maxHttpPoolSize;
    private Integer maxHttpConnectionsPerRoute;
    private boolean hashSearchKeys = true;

    public String getEnvironmentId() {
        return environmentId;
    }

    /**
     * sets environment ID
     *
     * @param environmentId environment ID
     * @return StorageConfig
     */
    public StorageConfig setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
        return this;
    }

    /**
     * load envId from env variable 'INC_ENVIRONMENT_ID'
     *
     * @return StorageConfig
     */
    public StorageConfig useEnvIdFromEnv() {
        this.environmentId = loadFromEnv(PARAM_ENV_ID);
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
     * Optional. Defines custom API URL
     *
     * @param endPoint API URL. Default endpoint will be used if this param is null
     * @return StorageConfig
     */
    public StorageConfig setEndPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    /**
     * Load endPoint from env variable 'INC_ENDPOINT'
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

    public CryptoProvider getCryptoProvider() {
        return cryptoProvider;
    }

    /**
     * Optional. Provider of encryption ciphers. Allows to register custom ciphers for encrypting stored data.
     * If null - default AES GCM cipher will be used
     *
     * @param cryptoProvider
     * @return StorageConfig
     */
    public StorageConfig setCryptoProvider(CryptoProvider cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
        return this;
    }

    public boolean isNormalizeKeys() {
        return normalizeKeys;
    }

    /**
     * Optional. If true - all keys will be stored as lower cased. Default is false
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
     * Optional. Set custom endpoints regional map to use for fetching oAuth tokens
     * Can be used only with {@link #setDefaultAuthEndpoint(String)}
     * Format: key = region, value = authorization server URL for region
     *
     * @param authEndpoints custom endpoints regional map
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
     * Optional. Set custom oAuth authorization server URL, will be used as default one.
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
     * Optional. Set HTTP requests timeout. Should be greater than 0.
     * Default value is 30 seconds.
     *
     * @param httpTimeout timeout in seconds
     * @return StorageConfig
     */
    public StorageConfig setHttpTimeout(Integer httpTimeout) {
        this.httpTimeout = httpTimeout;
        return this;
    }

    public Integer getMaxHttpPoolSize() {
        return maxHttpPoolSize;
    }

    /**
     * Optional. Set HTTP connections pool size. Expected value - null or positive integer. Defaults to 20.
     *
     * @param maxHttpPoolSize pool size
     * @return StorageConfig
     */
    public StorageConfig setMaxHttpPoolSize(Integer maxHttpPoolSize) {
        this.maxHttpPoolSize = maxHttpPoolSize;
        return this;
    }

    public String getEndpointMask() {
        return endpointMask;
    }

    public Integer getMaxHttpConnectionsPerRoute() {
        return maxHttpConnectionsPerRoute;
    }

    /**
     * Optional. Set maximum count of HTTP connections per route. Expected value - null or positive integer.
     * Default value == {@link #maxHttpPoolSize}.
     *
     * @param maxHttpConnectionsPerRoute pool size
     * @return StorageConfig
     */
    public StorageConfig setMaxHttpConnectionsPerRoute(Integer maxHttpConnectionsPerRoute) {
        this.maxHttpConnectionsPerRoute = maxHttpConnectionsPerRoute;
        return this;
    }

    /**
     * Optional. Parameter endpointMask is used for switching from `default` InCountry host family (-mt-01.api.incountry.io) to a different one.
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
     * Optional. Set custom endpoint for loading countries list.
     *
     * @param countriesEndpoint custom endpoint for countries loading
     * @return StorageConfig
     */
    public StorageConfig setCountriesEndpoint(String countriesEndpoint) {
        this.countriesEndpoint = countriesEndpoint;
        return this;
    }

    public boolean isHashSearchKeys() {
        return hashSearchKeys;
    }

    /**
     * Optional. If false - key1-key10 will be not hashed. Default is true
     *
     * @param hashSearchKeys value
     * @return StorageConfig
     */
    public StorageConfig setHashSearchKeys(boolean hashSearchKeys) {
        this.hashSearchKeys = hashSearchKeys;
        return this;
    }

    public StorageConfig copy() {
        StorageConfig newInstance = new StorageConfig();
        newInstance.setEnvironmentId(getEnvironmentId());
        newInstance.setApiKey(getApiKey());
        newInstance.setEndPoint(getEndPoint());
        newInstance.setSecretKeyAccessor(getSecretKeyAccessor());
        newInstance.setCryptoProvider(getCryptoProvider());
        newInstance.setNormalizeKeys(isNormalizeKeys());
        newInstance.setClientId(getClientId());
        newInstance.setClientSecret(getClientSecret());
        newInstance.setEndpointMask(getEndpointMask());
        newInstance.setCountriesEndpoint(getCountriesEndpoint());
        newInstance.setHttpTimeout(getHttpTimeout());
        newInstance.setAuthEndpoints(getAuthEndpoints());
        newInstance.setDefaultAuthEndpoint(getDefaultAuthEndpoint());
        newInstance.setMaxHttpPoolSize(getMaxHttpPoolSize());
        newInstance.setHashSearchKeys(isHashSearchKeys());
        return newInstance;
    }

    @Override
    public String toString() {
        return "StorageConfig{" +
                "envId='" + hideParam(environmentId) + '\'' +
                ", apiKey='" + hideParam(apiKey) + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", secretKeyAccessor=" + secretKeyAccessor +
                ", cryptoProvider=" + cryptoProvider +
                ", ignoreKeyCase=" + normalizeKeys +
                ", clientId='" + hideParam(clientId) + '\'' +
                ", clientSecret='" + hideParam(clientSecret) + '\'' +
                ", endpointMask='" + endpointMask + '\'' +
                ", countriesEndpoint='" + countriesEndpoint + '\'' +
                ", authEndpointMap='" + authEndpoints + '\'' +
                ", defaultAuthEndpoint='" + defaultAuthEndpoint + '\'' +
                ", httpTimeout='" + httpTimeout + '\'' +
                ", httpPoolSize='" + maxHttpPoolSize + '\'' +
                ", ignoreKeysHashing='" + hashSearchKeys + '\'' +
                '}';
    }

    private String hideParam(String param) {
        return param != null ? String.format(MSG_SECURE, param.hashCode()) : null;
    }

    private static String loadFromEnv(String key) {
        return System.getenv(key);
    }
}
