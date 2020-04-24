package com.incountry.residence.sdk.tools.keyaccessor.impl;

import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * default implementation of SecretKeyAccessor
 */
public class SecretKeyAccessorImpl implements SecretKeyAccessor {

    public static final int DEFAULT_VERSION = 0;

    private SecretsData secretsData;

    private SecretKeyAccessorImpl(String secret) throws StorageClientException {
        secretsData = getSecretsDataFromString(secret);
    }

    private SecretKeyAccessorImpl(SecretsData secretsData) {
        this.secretsData = secretsData;
    }

    /**
     * create SecretKeyAccessor with password as String
     *
     * @param password simple password
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    public static SecretKeyAccessor getInstance(String password) throws StorageClientException {
        return new SecretKeyAccessorImpl(password);
    }

    /**
     * create SecretKeyAccessor with SecretsData as JSON String
     *
     * @param secretsDataJson SecretsData in JSON String
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    public static SecretKeyAccessor getInstanceWithJson(String secretsDataJson) throws StorageClientException {
        SecretsData secretsData = getSecretsDataFromJson(secretsDataJson);
        return new SecretKeyAccessorImpl(secretsData);
    }

    /**
     * Convert string to SecretsData object
     *
     * @param secretKeyString simple string or json
     * @return SecretsData object which contain secret keys and there versions
     */
    private static SecretsData getSecretsDataFromString(String secretKeyString) throws StorageClientException {
        SecretKey secretKey = new SecretKey(secretKeyString, DEFAULT_VERSION, false);
        List<SecretKey> secretKeys = new ArrayList<>();
        secretKeys.add(secretKey);
        return new SecretsData(secretKeys, DEFAULT_VERSION);
    }

    /**
     * Convert string to SecretsData object
     *
     * @param secretKeyString simple string or json
     * @return SecretsData object which contain secret keys and there versions
     */
    private static SecretsData getSecretsDataFromJson(String secretKeyString) throws StorageClientException {
        SecretsData data = JsonUtils.getSecretsDataFromJson(secretKeyString);
        SecretsData.validate(data.getSecrets(), data.getCurrentVersion());
        return data;
    }

    @Override
    public SecretsData getSecretsData() {
        return secretsData;
    }
}
