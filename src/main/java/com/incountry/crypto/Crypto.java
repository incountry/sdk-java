package com.incountry.crypto;

import com.incountry.exceptions.StorageCryptoException;
import org.javatuples.Pair;

public interface Crypto {
    Pair<String, Integer> encrypt(String plainText) throws StorageCryptoException;
    String decrypt(String cipherText, Integer decriptKeyVersion) throws StorageCryptoException;
    String createKeyHash(String key);
    int getCurrentSecretVersion();
}
