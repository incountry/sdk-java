package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

import java.util.Map;

public interface Crypto {
    Map.Entry<String, Integer> encrypt(String plainText) throws StorageClientException, StorageCryptoException;

    String decrypt(String cipherText, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException;

    String createKeyHash(String key);

    Integer getCurrentSecretVersion();
}
