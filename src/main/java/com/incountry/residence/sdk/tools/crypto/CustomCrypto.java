package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;

/**
 * Use it for custom encryption
 */
public interface CustomCrypto {

    /**
     * encrypts data with secret
     *
     * @param text      data for encryption
     * @param secretKey secret
     * @return encrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String encrypt(String text, SecretKey secretKey) throws StorageClientException, StorageCryptoException;

    /**
     * decrypts data with Secret
     *
     * @param cipherText encrypted data
     * @param secretKey  secret
     * @return decrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String decrypt(String cipherText, SecretKey secretKey) throws StorageClientException, StorageCryptoException;

    /**
     * version of encryption algorithm as String
     *
     * @return version
     */
    String getVersion();

    /**
     * only one CustomCrypto can be current. This parameter used only during {@link com.incountry.residence.sdk.Storage}
     * initialisation. Changing this parameter will be ignored after initialization
     *
     * @return is current or not
     */
    boolean isCurrent();
}
