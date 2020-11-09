package com.incountry.residence.sdk.tools.keyaccessor.key;

import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Generator for {@link com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData}
 */
public class SecretsDataGenerator {

    public static final int DEFAULT_VERSION = 0;

    private SecretsDataGenerator() {
    }

    /**
     * create SecretKeyAccessor with password as String
     *
     * @param password simple password
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    public static SecretsData fromPassword(String password) throws StorageClientException {
        SecretKey secretKey = new SecretKey(password.getBytes(StandardCharsets.UTF_8), DEFAULT_VERSION, false);
        List<SecretKey> secretKeys = new ArrayList<>();
        secretKeys.add(secretKey);
        return new SecretsData(secretKeys, DEFAULT_VERSION);
    }

    /**
     * create SecretKeyAccessor with SecretsData as JSON String
     *
     * @param secretsDataJson SecretsData in JSON String
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    public static SecretsData fromJson(String secretsDataJson) throws StorageClientException {
        return JsonUtils.getSecretsDataFromJson(secretsDataJson);
    }
}
