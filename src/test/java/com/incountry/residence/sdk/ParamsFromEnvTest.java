package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static com.incountry.residence.sdk.StorageConfig.PARAM_API_KEY;
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
        StorageConfig config = new StorageConfig()
                .useEnvironmentIdFromEnv()
                .useApiKeyFromEnv()
                .useEndPointFromEnv()
                .useClientIdFromEnv()
                .useClientSecretFromEnv();
        assertEquals(envId, config.getEnvironmentId());
        assertEquals(apiKey, config.getApiKey());
        assertEquals(endPoint, config.getEndPoint());
        assertEquals(clientId, config.getClientId());
        assertEquals(clientSecret, config.getClientSecret());
        unsetEnv(PARAM_ENV_ID);
        unsetEnv(PARAM_API_KEY);
        unsetEnv(PARAM_ENDPOINT);
        unsetEnv(PARAM_CLIENT_ID);
        unsetEnv(PARAM_CLIENT_SECRET);
    }

    @Test
    void getStorageWithParamsFromEnv() throws StorageClientException, StorageCryptoException {
        String envId = "<env>";
        setEnv(PARAM_ENV_ID, envId);
        String apiKey = "<apikey>";
        setEnv(PARAM_API_KEY, apiKey);
        String endPoint = "<endPoint>";
        setEnv(PARAM_ENDPOINT, endPoint);
        StorageConfig config = new StorageConfig()
                .useEnvironmentIdFromEnv()
                .useApiKeyFromEnv()
                .useEndPointFromEnv();
        Storage storage = StorageImpl.newStorage(config);
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
