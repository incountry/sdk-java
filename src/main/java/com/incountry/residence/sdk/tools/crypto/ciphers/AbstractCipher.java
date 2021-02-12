package com.incountry.residence.sdk.tools.crypto.ciphers;

import com.incountry.residence.sdk.tools.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.util.Base64;

public abstract class AbstractCipher implements Cipher {

    private static final Logger LOG = LogManager.getLogger(AbstractCipher.class);

    private static final String MSG_ERR_CUSTOM_ENCRYPTION_KEY = "Used key from secrets data is not instance of CustomEncryptionKey";

    public static final String PREFIX_CUSTOM_ENCRYPTION = "c";

    private String code;
    private Charset cipherCharset;
    private String codeBase64;

    public AbstractCipher(String code, Charset cipherEncoding) {
        this.code = code;
        this.cipherCharset = cipherEncoding;
        this.codeBase64 = base64(code);
    }

    public abstract String encrypt(byte[] textBytes, CustomEncryptionKey secretKey);

    public CipherText encrypt(String text, Secret secret) throws StorageCryptoException {
        if (!(secret instanceof CustomEncryptionKey)) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION_KEY);
            throw new StorageCryptoException(MSG_ERR_CUSTOM_ENCRYPTION_KEY);
        }
        String cipheredText = encrypt(text.getBytes(cipherCharset), (CustomEncryptionKey) secret);
        return new CipherText(codeBase64 + ":" + toBase64String(cipheredText), secret.getVersion());
    }

    public abstract String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey);

    public String decrypt(String cipherText, Secret secret) throws StorageCryptoException {
        if (!(secret instanceof CustomEncryptionKey)) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION_KEY);
            throw new StorageCryptoException(MSG_ERR_CUSTOM_ENCRYPTION_KEY);
        }
        return decrypt(Base64.getDecoder().decode(cipherText), (CustomEncryptionKey) secret);
    }

    private String base64(String code) {
        return PREFIX_CUSTOM_ENCRYPTION + toBase64String(code);
    }

    private String toBase64String(String code) {
        return new String(Base64.getEncoder().encode(code.getBytes(cipherCharset)), cipherCharset);
    }

    public String getCode() {
        return code;
    }

    public Charset getCipherCharset() {
        return cipherCharset;
    }

    public String getCodeBase64() {
        return codeBase64;
    }
}

