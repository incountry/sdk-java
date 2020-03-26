package com.incountry.storage.sdk.tools.crypto;

import com.incountry.storage.sdk.tools.exceptions.StorageCryptoException;
import org.javatuples.Pair;

public interface Crypto {
    Pair<String, Integer> encrypt(String plainText) throws StorageCryptoException;
    String decrypt(String cipherText, Integer decryptKeyVersion) throws StorageCryptoException;
    String createKeyHash(String key);
    int getCurrentSecretVersion();
}
