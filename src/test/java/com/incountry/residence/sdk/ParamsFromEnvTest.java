package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static com.incountry.residence.sdk.StorageConfig.PARAM_API_KEY;
import static com.incountry.residence.sdk.StorageConfig.PARAM_AUTH_ENDPOINT;
import static com.incountry.residence.sdk.StorageConfig.PARAM_CLIENT_ID;
import static com.incountry.residence.sdk.StorageConfig.PARAM_CLIENT_SECRET;
import static com.incountry.residence.sdk.StorageConfig.PARAM_ENDPOINT;
import static com.incountry.residence.sdk.StorageConfig.PARAM_ENV_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParamsFromEnvTest {

    @Test
    void loadFromEnv() {
        String envId = "<env>";
        setEnv(PARAM_ENV_ID, envId);
        String apiKey = "<apikey>";
        setEnv(PARAM_API_KEY, apiKey);
        String endPoint = "<endPoint>";
        setEnv(PARAM_ENDPOINT, endPoint);
        String clientId = "<clientId>";
        setEnv(PARAM_CLIENT_ID, clientId);
        String clientSecret = "<clientSecret>";
        setEnv(PARAM_CLIENT_SECRET, clientSecret);
        String authEndPoint = "<authEndpoint>";
        setEnv(PARAM_AUTH_ENDPOINT, authEndPoint);
        StorageConfig config = new StorageConfig()
                .useEnvIdFromEnv()
                .useApiKeyFromEnv()
                .useEndPointFromEnv()
                .useClientIdFromEnv()
                .useClientSecretFromEnv()
                .useAuthEndPointFromEnv();
        assertEquals(envId, config.getEnvId());
        assertEquals(apiKey, config.getApiKey());
        assertEquals(endPoint, config.getEndPoint());
        assertEquals(clientId, config.getClientId());
        assertEquals(clientSecret, config.getClientSecret());
        assertEquals(authEndPoint, config.getAuthEndPoint());
        unsetEnv(PARAM_ENV_ID);
        unsetEnv(PARAM_API_KEY);
        unsetEnv(PARAM_ENDPOINT);
        unsetEnv(PARAM_CLIENT_ID);
        unsetEnv(PARAM_CLIENT_SECRET);
        unsetEnv(PARAM_AUTH_ENDPOINT);
    }

    @Test
    void getStorageWithParamsFromEnv() throws StorageClientException, StorageServerException {
        String envId = "<env>";
        setEnv(PARAM_ENV_ID, envId);
        String apiKey = "<apikey>";
        setEnv(PARAM_API_KEY, apiKey);
        String endPoint = "<endPoint>";
        setEnv(PARAM_ENDPOINT, endPoint);
        Storage storage = StorageImpl.getInstance();
        assertNotNull(storage);
        unsetEnv(PARAM_ENV_ID);
        unsetEnv(PARAM_API_KEY);
        unsetEnv(PARAM_ENDPOINT);
    }

    private static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }

    private static void unsetEnv(String key) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.remove(key);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to unset environment variable", e);
        }
    }
}
