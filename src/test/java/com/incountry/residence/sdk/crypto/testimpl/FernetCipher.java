package com.incountry.residence.sdk.crypto.testimpl;

import com.incountry.residence.sdk.crypto.AbstractCipher;
import com.incountry.residence.sdk.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.macasaet.fernet.Key;
import com.macasaet.fernet.PayloadValidationException;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

/**
 * Example of custom implementation of {@link AbstractCipher} using Fernet algorithm
 */
public class FernetCipher extends AbstractCipher {

    private Validator<String> validator;


    public FernetCipher(String name) throws StorageClientException {
        super(name);
        this.validator = new StringValidator() {
            public TemporalAmount getTimeToLive() {
                return Duration.ofHours(4);
            }
        };
    }

    @Override
    public String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) throws StorageCryptoException {
        try {
            Key key = new Key(secretKey.getSecretBytes());
            Token result = Token.generate(key, textBytes);
            return result.serialise();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            throw new StorageCryptoException("Encryption error", ex);
        }
    }

    @Override
    public String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey) throws StorageCryptoException {
        try {
            Key key = new Key(secretKey.getSecretBytes());
            Token result = Token.fromString(new String(cipherTextBytes, charset));
            return result.validateAndDecrypt(key, validator);
        } catch (PayloadValidationException ex) {
            throw new StorageCryptoException("Decryption error", ex);
        }
    }
}
