package com.incountry.residence.sdk.tools.crypto.Ciphers;

import com.incountry.residence.sdk.tools.crypto.Ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

public interface Cipher {

    /**
     * encrypts data with secret
     *
     * @param textBytes data for encryption
     * @param secretKey secret
     * @return encrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) throws StorageClientException, StorageCryptoException;

    /**
     * decrypts data with Secret
     *
     * @param cipherTextBytes encrypted data
     * @param secretKey  secret
     * @return decrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey) throws StorageClientException, StorageCryptoException;

    /**
     * get cipher name
     *
     * @return cipher name
     */
    String getName();

}
