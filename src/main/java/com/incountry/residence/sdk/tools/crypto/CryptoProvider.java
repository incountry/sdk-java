package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.crypto.ciphers.AbstractCipher;
import com.incountry.residence.sdk.tools.crypto.ciphers.impl.AesGcmPbkdf10kBase64Cipher;
import com.incountry.residence.sdk.tools.crypto.ciphers.impl.AesGcmPbkdf10kHexCipher;
import com.incountry.residence.sdk.tools.crypto.ciphers.Cipher;
import com.incountry.residence.sdk.tools.crypto.ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.ciphers.impl.PlainTextBase64Cipher;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CryptoProvider {

    private static final Logger LOG = LogManager.getLogger(CryptoProvider.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final String MSG_ERR_CUSTOM_ENCRYPTION_NAME = "Custom cipher has null name";
    private static final String MSG_ERR_CUSTOM_ENCRYPTION = "Custom cipher is null";
    private static final String MSG_ERR_DECRYPTION_FORMAT = "Unknown cipher format";
    private static final String MSG_ERR_UNEXPECTED = "Unexpected exception";
    private static final String MSG_ERR_EXCEPTION_UNEXPECTED = "Unexpected exception during encryption";
    private static final String MSG_ERR_CURRENT_CIPHER = "There is no current cipher at registered cipher list";
    private static final String MSG_ERROR_INCORRECT_CUSTOM_CRYPTO = "Validation failed for custom cipher config with version %s";
    private static final String MSG_ERR_DECRYPTION = "Unknown custom encryption version";

    public static final String PREFIX_CUSTOM_ENCRYPTION = "c";
    private static final String TEST_ENCRYPTION_TEXT = "This is test message for enc/dec_!@#$%^&*()_+|?.,~//\\=-' "
            + UUID.randomUUID().toString();

    private AbstractCipher currentCipher;
    private Cipher defaultCipher;
    private Map<String, AbstractCipher> customCiphers = new HashMap<>();
    private Map<String, Cipher> defaultCiphers = new HashMap<>();

    public CryptoProvider() throws StorageClientException {
        this(null);
    }

    public CryptoProvider(AbstractCipher currentCipher) throws StorageClientException {
        defaultCipher = new AesGcmPbkdf10kBase64Cipher(CHARSET);

        addDefaultCiphers(
                new PlainTextBase64Cipher(CHARSET),
                new AesGcmPbkdf10kHexCipher(CHARSET),
                defaultCipher
        );
        if (currentCipher == null) {
            return;
        }
        if (currentCipher.getCode() == null || currentCipher.getCode().isEmpty()) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION_NAME);
            throw new StorageClientException(MSG_ERR_CUSTOM_ENCRYPTION_NAME);
        }
        customCiphers.put(currentCipher.getCodeBase64(), currentCipher);
        this.currentCipher = currentCipher;
    }

    private void addDefaultCiphers(Cipher... ciphers) {
        for (Cipher cipher : ciphers) {
            defaultCiphers.put(cipher.getCode(), cipher);
        }
    }

    public CryptoProvider registerCipher(AbstractCipher cipher) throws StorageClientException {
        if (cipher == null) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION);
            throw new StorageClientException(MSG_ERR_CUSTOM_ENCRYPTION);
        }
        if (cipher.getCode() == null || cipher.getCode().isEmpty()) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION_NAME);
            throw new StorageClientException(MSG_ERR_CUSTOM_ENCRYPTION_NAME);
        }
        customCiphers.put(cipher.getCodeBase64(), cipher);
        return this;
    }

    public Boolean unregisterCipher(AbstractCipher cipher) {
        if (cipher == null || cipher.getCode() == null || cipher.getCode().isEmpty()) {
            return false;
        }
        return customCiphers.remove(cipher.getCodeBase64()) != null;
    }

    public CipherText encrypt(String text, SecretsData secretsData) throws StorageCryptoException, StorageClientException {
        Cipher cipher = currentCipher != null ? currentCipher : defaultCipher;
        if (secretsData == null) {
            cipher = defaultCiphers.get(PlainTextBase64Cipher.CIPHER_CODE);
        }
        try {
            return cipher.encrypt(text, secretsData != null ? secretsData.getCurrentSecret() : null);
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.error(MSG_ERR_EXCEPTION_UNEXPECTED, ex);
            throw new StorageCryptoException(MSG_ERR_EXCEPTION_UNEXPECTED, ex);
        }
    }

    public String decrypt(String cipherText, SecretsData secretsData, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException  {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }
        String[] cipherTextParts = cipherText.split(":", 2);
        if (cipherTextParts.length != 2) {
            LOG.error(MSG_ERR_DECRYPTION_FORMAT);
            throw new StorageCryptoException(MSG_ERR_DECRYPTION_FORMAT);
        }
        Secret secret = secretsData != null ? secretsData.getSecret(decryptKeyVersion) : null;
        try {

            Cipher cipher = detectCipher(cipherTextParts[0]);
            return cipher.decrypt(cipherTextParts[1], secret);
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.error(MSG_ERR_UNEXPECTED, ex);
            throw new StorageCryptoException(MSG_ERR_UNEXPECTED, ex);
        }
    }

    private Cipher detectCipher(String cipherCode) throws StorageCryptoException {
        if (defaultCiphers.containsKey(cipherCode)) {
            return defaultCiphers.get(cipherCode);
        }
        if (!customCiphers.containsKey(cipherCode)) {
            LOG.error(MSG_ERR_DECRYPTION_FORMAT);
            throw new StorageCryptoException(MSG_ERR_DECRYPTION_FORMAT);
        }
        return customCiphers.get(cipherCode);
    }

    @SuppressWarnings("java:S2259")
    public void validateCustomCiphers(SecretsData secretsData) throws StorageClientException {
        if (customCiphers.size() == 0) {
            return;
        }
        if (secretsData != null) {
            List<Secret> customSecrets = secretsData.getSecrets().stream()
                    .filter(secret -> secret instanceof CustomEncryptionKey).collect(Collectors.toList());
            if (customSecrets.isEmpty()) {
                LOG.error(MSG_ERR_DECRYPTION);
                throw new StorageClientException(MSG_ERR_DECRYPTION);
            }
        }
        if (customCiphers.get(currentCipher.getCodeBase64()) != currentCipher) {
            LOG.error(MSG_ERR_CURRENT_CIPHER);
            throw new StorageClientException(MSG_ERR_CURRENT_CIPHER);
        }

        CustomEncryptionKey customEncryptionKey = secretsData.getCurrentSecret() instanceof CustomEncryptionKey
                ? (CustomEncryptionKey) secretsData.getCurrentSecret()
                : new CustomEncryptionKey(0, getRandomEncryptionKey());

        for (AbstractCipher cipher : customCiphers.values()) {
            validateCipher(cipher, customEncryptionKey);
        }
    }

    private static byte[] getRandomEncryptionKey() {
        byte[] resultKey = new byte[32];
        System.arraycopy(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8), 0, resultKey, 0, 16);
        System.arraycopy(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8), 0, resultKey, 16, 16);
        return resultKey;
    }

    private void validateCipher(AbstractCipher cipher, CustomEncryptionKey encryptionKey) throws StorageClientException {
        try {
            String encryptedText = cipher.encrypt(TEST_ENCRYPTION_TEXT.getBytes(CHARSET), encryptionKey);
            String decryptedText = cipher.decrypt(encryptedText.getBytes(CHARSET), encryptionKey);
            if (!TEST_ENCRYPTION_TEXT.equals(decryptedText)) {
                String message = String.format(MSG_ERROR_INCORRECT_CUSTOM_CRYPTO, cipher.getCode());
                LOG.error(message);
                throw new StorageClientException(message);
            }
        } catch (Exception ex) {
            String message = String.format(MSG_ERROR_INCORRECT_CUSTOM_CRYPTO, cipher.getCode());
            LOG.error(message);
            throw new StorageClientException(message, ex);
        }
    }
}
