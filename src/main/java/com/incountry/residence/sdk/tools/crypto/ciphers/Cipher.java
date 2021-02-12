package com.incountry.residence.sdk.tools.crypto.ciphers;

import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

public interface Cipher {

    /**
     * encrypts data with secret
     *
     * @param text data for encryption
     * @param secret secret
     * @return encrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    CipherText encrypt(String text, Secret secret) throws StorageClientException, StorageCryptoException;

    /**
     * decrypts data with Secret
     *
     * @param cipherText encrypted data
     * @param secret  secret
     * @return decrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String decrypt(String cipherText, Secret secret) throws StorageClientException, StorageCryptoException;

    /**
     * get cipher name
     *
     * @return cipher name
     */
    String getCode();

}
