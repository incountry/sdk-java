package com.incountry.residence.sdk;

/**
 * Gets credentials for integration tests from environment
 */
public class CredentialsHelper {
    private static final String MID_POP_COUNTRY = "INT_INC_COUNTRY";
    private static final String MID_POP_COUNTRY_2 = "INT_INC_COUNTRY_2";
    private static final String MINIPOP_COUNTRY = "INT_MINIPOP_COUNTRY";

    private static final String AUTH_ENDPOINT = "INT_INC_DEFAULT_AUTH_ENDPOINT";
    private static final String CLIENT_ID = "INT_INC_CLIENT_ID";
    private static final String CLIENT_SECRET = "INT_INC_CLIENT_SECRET";
    private static final String ENVIRONMENT_ID = "INT_INC_ENVIRONMENT_ID_OAUTH";
    private static final String ENDPOINT_MASK = "INT_INC_ENDPOINT_MASK";
    private static final String COUNTRIES_LIST_ENDPOINT = "INT_COUNTRIES_LIST_ENDPOINT";
    public static final String INT_INC_HTTP_POOL_SIZE = "INT_INC_HTTP_POOL_SIZE";
    private static final Integer HTTP_POOL_SIZE = Integer.valueOf(loadFromEnv(INT_INC_HTTP_POOL_SIZE, "4"));

    public static StorageConfig getConfigWithOauth() {
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(loadFromEnv(ENVIRONMENT_ID))
                .setClientId(loadFromEnv(CLIENT_ID))
                .setClientSecret(loadFromEnv(CLIENT_SECRET))
                .setDefaultAuthEndpoint(loadFromEnv(AUTH_ENDPOINT))
                .setCountriesEndpoint(loadFromEnv(COUNTRIES_LIST_ENDPOINT))
                .setEndpointMask(loadFromEnv(ENDPOINT_MASK))
                .setMaxHttpPoolSize(HTTP_POOL_SIZE)
                .setMaxHttpConnectionsPerRoute(HTTP_POOL_SIZE / 2);
        return config;
    }

    public static String getMidPopCountry(boolean primary) {
        return loadFromEnv(primary ? MID_POP_COUNTRY : MID_POP_COUNTRY_2);
    }

    public static String getMiniPopCountry() {
        return loadFromEnv(MINIPOP_COUNTRY);
    }

    public static String loadFromEnv(String key) {
        return System.getenv(key);
    }

    public static String loadFromEnv(String key, String defaultValue) {
        String value = loadFromEnv(key);
        return value == null ? defaultValue : value;
    }
}
