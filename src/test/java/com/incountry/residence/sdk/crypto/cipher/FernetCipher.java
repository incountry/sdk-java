package com.incountry.residence.sdk.crypto.cipher;

import com.incountry.residence.sdk.tools.crypto.ciphers.AbstractCipher;
import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;
import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.nio.charset.StandardCharsets;

/**
 * Example of custom implementation of {@link AbstractCipher} using Fernet algorithm
 */
@SuppressFBWarnings({"UUF_UNUSED_FIELD", "UWF_UNWRITTEN_FIELD"})
public class FernetCipher extends AbstractCipher {

    private static final String VERSION = "fernet custom encryption";
    private boolean current;
    private Validator<String> validator;

    public FernetCipher(String code) {
        super(code, StandardCharsets.UTF_8);
    }

    @Override
    public String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) {
            Key key = new Key(secretKey.getSecretBytes());
            Token result = Token.generate(key, textBytes);
            return result.serialise();
    }

    @Override
    public String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey) {
        Key key = new Key(secretKey.getSecretBytes());
        Token result = Token.fromBytes(cipherTextBytes);
        return result.validateAndDecrypt(key, validator);
    }
}
