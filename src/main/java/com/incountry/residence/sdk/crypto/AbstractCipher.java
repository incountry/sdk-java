package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.crypto.cipher.Cipher;
import com.incountry.residence.sdk.tools.crypto.cipher.Ciphertext;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class AbstractCipher implements Cipher {

    private static final ValidationHelper HELPER = new ValidationHelper(LogManager.getLogger(AbstractCipher.class));
    private static final String PREFIX_CUSTOM_ENCRYPTION = "c";

    private static final String MSG_ERR_INVALID_KEY_CLASS = "Used key from secrets data is not instance of CustomEncryptionKey";
    private static final String MSG_ERR_NULL_CIPHER_NAME = "Cipher has null name";

    private final String name;
    private final String nameBase64;

    protected Charset charset = StandardCharsets.UTF_8;

    @SuppressWarnings("java:S2259")
    protected AbstractCipher(String name) throws StorageClientException {
        boolean invalidName = name == null || name.isEmpty();
        HELPER.check(StorageClientException.class, invalidName, MSG_ERR_NULL_CIPHER_NAME);
        this.name = name;
        this.nameBase64 = base64(name);
    }

    private String base64(String name) {
        String base64String = Base64.getEncoder().encodeToString(name.getBytes(charset));
        return PREFIX_CUSTOM_ENCRYPTION + base64String;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getNameBase64() {
        return nameBase64;
    }

    /**
     * encrypts data with secretKey
     *
     * @param textBytes data for encryption
     * @param secretKey secret
     * @return encrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when encryption fails
     */
    public abstract String encrypt(byte[] textBytes, CustomEncryptionKey secretKey) throws StorageClientException, StorageCryptoException;

    public Ciphertext encrypt(String text, Secret secret) throws StorageCryptoException, StorageClientException {
        boolean correctSecretType = secret instanceof CustomEncryptionKey;
        HELPER.check(StorageCryptoException.class, !correctSecretType, MSG_ERR_INVALID_KEY_CLASS);
        String cipheredText = encrypt(text.getBytes(charset), (CustomEncryptionKey) secret);
        return new Ciphertext(nameBase64 + ":" + Base64.getEncoder().encodeToString(cipheredText.getBytes(charset)),
                secret.getVersion());
    }

    /**
     * decrypts data with Secret
     *
     * @param cipherTextBytes encrypted data
     * @param secretKey       secret
     * @return decrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    public abstract String decrypt(byte[] cipherTextBytes, CustomEncryptionKey secretKey) throws StorageClientException, StorageCryptoException;

    public String decrypt(String cipherText, Secret secret) throws StorageCryptoException, StorageClientException {
        boolean correctSecretType = secret instanceof CustomEncryptionKey;
        HELPER.check(StorageCryptoException.class, !correctSecretType, MSG_ERR_INVALID_KEY_CLASS);
        return decrypt(Base64.getDecoder().decode(cipherText), (CustomEncryptionKey) secret);
    }
}
