package com.incountry.crypto;

import com.incountry.exceptions.RecordException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface ICrypto {
    String encrypt(String plainText) throws GeneralSecurityException, IOException;
    String decrypt(String cipherText) throws GeneralSecurityException, RecordException;
}
