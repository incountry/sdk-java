package com.incountry.residence.sdk.tools.crypto.cipher;

import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

public interface Cipher {
    String getName();

    Ciphertext encrypt(String text, Secret secret) throws StorageCryptoException, StorageClientException;

    String decrypt(String cipherText, Secret secret) throws StorageCryptoException, StorageClientException;
}
