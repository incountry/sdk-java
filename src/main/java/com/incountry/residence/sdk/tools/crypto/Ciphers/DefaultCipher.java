package com.incountry.residence.sdk.tools.crypto.Ciphers;

import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

public interface DefaultCipher {

    String getCode();

    CipherText encrypt(String text, Secret secretKey) throws StorageCryptoException;

    String decrypt(String cipherText, Secret secret) throws StorageCryptoException;

}
