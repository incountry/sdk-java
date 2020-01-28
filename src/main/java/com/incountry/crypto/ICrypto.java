package com.incountry.crypto;

import com.incountry.exceptions.StorageDecryptionException;
import org.javatuples.Pair;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface ICrypto {
    Pair<String, Integer> encrypt(String plainText) throws GeneralSecurityException, IOException;
    String decrypt(String cipherText, Integer decriptKeyVersion) throws GeneralSecurityException, StorageDecryptionException;
}
