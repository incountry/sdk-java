package com.incountry.residence.sdk.crypto.testimpl;

import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.macasaet.fernet.Key;
import com.macasaet.fernet.PayloadValidationException;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;

/**
 * Example of custom implementation of {@link Crypto} using Fernet algorithm
 */
public class FernetCrypto implements Crypto {

    private static final String VERSION = "fernet custom encryption";
    private boolean current;
    private Validator<String> validator;

    public FernetCrypto(boolean current) {
        this.current = current;
        this.validator = new StringValidator() {
        };
    }

    @Override
    public String encrypt(String text, SecretKey secretKey)
            throws StorageClientException, StorageCryptoException {
        if (isEasySecret(secretKey.getSecret())) {
            throw new StorageClientException("Secret is too easy, use more strong password");
        }
        try {
            Key key = new Key(secretKey.getSecret());
            Token result = Token.generate(key, text);
            return result.serialise();
        } catch (IllegalStateException ex) {
            throw new StorageCryptoException("Encryption error", ex);
        }
    }

    @Override
    public String decrypt(String cipherText, SecretKey secretKey) throws StorageCryptoException {
        try {
            Key key = new Key(secretKey.getSecret());
            Token result = Token.fromString(cipherText);
            return result.validateAndDecrypt(key, validator);
        } catch (PayloadValidationException ex) {
            throw new StorageCryptoException("Decryption error", ex);
        }
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public boolean isCurrent() {
        return current;
    }

    private boolean isEasySecret(String secret) {
        return secret.length() < 7;
    }
}
