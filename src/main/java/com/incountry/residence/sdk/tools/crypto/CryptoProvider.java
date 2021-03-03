package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.crypto.AbstractCipher;
import com.incountry.residence.sdk.crypto.CustomEncryptionKey;
import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.crypto.SecretsData;
import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.crypto.cipher.AesGcmPbkdf10kBase64Cipher;
import com.incountry.residence.sdk.tools.crypto.cipher.AesGcmPbkdf10kHexCipher;
import com.incountry.residence.sdk.tools.crypto.cipher.Cipher;
import com.incountry.residence.sdk.tools.crypto.cipher.Ciphertext;
import com.incountry.residence.sdk.tools.crypto.cipher.PlainTextBase64Cipher;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CryptoProvider {
    private static final Logger LOG = LogManager.getLogger(CryptoProvider.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final String MSG_ERR_INVALID_CIPHER_NAME = "Custom cipher has null name";
    private static final String MSG_ERR_NULL_CIPHER = "Custom cipher is null";
    private static final String MSG_ERR_CIPHER_EXISTS = "Custom cipher with name %s is already registered";
    private static final String MSG_ERR_UNREGISTER_DEFAULT = "Can't unregister default cipher with name %s";
    private static final String MSG_ERR_UNKNOWN_CIPHER = "Unknown cipher format";
    private static final String MSG_ERR_NO_CUSTOM_ENC_KEY = "There is no custom encryption key for the custom ciphers";
    private static final String MSG_ERR_INVALID_CIPHER = "Validation failed for custom cipher with version '%s'";
    private static final String MSG_ERR_UNEXPECTED = "Unexpected exception";

    private static final String VALIDATION_TEXT = "This is test message for enc/dec {Louis César de La Baume Le Blanc}" +
            "_!@#$%^&*()_+|?.,~//\\=-' " + UUID.randomUUID().toString();

    private final Cipher defaultCipher;
    private final Map<String, AbstractCipher> customCipherMap = new HashMap<>();
    private final Map<String, Cipher> defaultCipherMap = new HashMap<>();

    public CryptoProvider(AbstractCipher currentCipher) throws StorageClientException {
        Cipher tempCipher = new AesGcmPbkdf10kBase64Cipher(CHARSET);
        addDefaultCiphers(new PlainTextBase64Cipher(), new AesGcmPbkdf10kHexCipher(CHARSET), tempCipher);
        if (currentCipher == null) {
            defaultCipher = tempCipher;
            return;
        }
        boolean invalidCipher = currentCipher.getName() == null || currentCipher.getName().isEmpty();
        HELPER.check(StorageClientException.class, invalidCipher, MSG_ERR_INVALID_CIPHER_NAME);
        customCipherMap.put(currentCipher.getNameBase64(), currentCipher);
        defaultCipher = currentCipher;
    }

    public CryptoProvider registerCipher(AbstractCipher cipher) throws StorageClientException {
        HELPER.check(StorageClientException.class, cipher == null, MSG_ERR_NULL_CIPHER);
        boolean nameExists = customCipherMap.containsKey(cipher.getNameBase64());
        HELPER.check(StorageClientException.class, nameExists, MSG_ERR_CIPHER_EXISTS, cipher.getName());
        customCipherMap.put(cipher.getNameBase64(), cipher);
        return this;
    }

    public boolean unregisterCipher(AbstractCipher cipher) throws StorageClientException {
        if (cipher == null || cipher.getName() == null) {
            return false;
        }
        boolean isDefault = defaultCipher.getName().equals(cipher.getName());
        HELPER.check(StorageClientException.class, isDefault, MSG_ERR_UNREGISTER_DEFAULT, cipher.getName());
        return customCipherMap.remove(cipher.getNameBase64()) == null;
    }

    public Ciphertext encrypt(String text, SecretsData secretsData) throws StorageClientException, StorageCryptoException {
        Cipher cipher = defaultCipher;
        boolean nullSecrets = secretsData == null;
        if (nullSecrets) {
            cipher = defaultCipherMap.get(PlainTextBase64Cipher.CIPHER_NAME);
        }
        try {
            return cipher.encrypt(text, nullSecrets ? null : secretsData.getCurrentSecret());
        } catch (StorageClientException | StorageCryptoException se) {
            throw se;
        } catch (Exception ex) {
            LOG.error(MSG_ERR_UNEXPECTED, ex);
            throw new StorageCryptoException(MSG_ERR_UNEXPECTED, ex);
        }
    }

    public String decrypt(String cipherText, SecretsData secretsData, int decryptKeyVersion) throws StorageCryptoException, StorageClientException {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }

        String[] cipherTextParts = cipherText.split(":", 2);
        HELPER.check(StorageCryptoException.class, cipherTextParts.length != 2,
                MSG_ERR_UNKNOWN_CIPHER);
        Secret secret = secretsData != null ? secretsData.getSecret(decryptKeyVersion) : null;
        try {
            Cipher cipher = detectCipher(cipherTextParts[0]);
            return cipher.decrypt(cipherTextParts[1], secret);
        } catch (StorageClientException | StorageCryptoException se) {
            throw se;
        } catch (Exception ex) {
            LOG.error(MSG_ERR_UNEXPECTED, ex);
            throw new StorageCryptoException(MSG_ERR_UNEXPECTED, ex);
        }
    }

    @SuppressWarnings("java:S2259")
    public void validateCustomCiphers(SecretsData secretsData) throws StorageClientException, StorageCryptoException {
        if (customCipherMap.isEmpty()) {
            return;
        }
        boolean containsCustomEncryptionKey = false;
        if (secretsData != null) {
            containsCustomEncryptionKey = secretsData.getSecrets().stream().anyMatch(CustomEncryptionKey.class::isInstance);
        }
        HELPER.check(StorageClientException.class, !containsCustomEncryptionKey, MSG_ERR_NO_CUSTOM_ENC_KEY);
        CustomEncryptionKey customEncryptionKey = (secretsData.getCurrentSecret() instanceof CustomEncryptionKey)
                ? (CustomEncryptionKey) secretsData.getCurrentSecret()
                : new CustomEncryptionKey(0, getRandomEncryptionKey());
        for (AbstractCipher cipher : customCipherMap.values()) {
            validateCipher(cipher, customEncryptionKey);
        }
    }

    private void addDefaultCiphers(Cipher... ciphers) {
        for (Cipher cipher : ciphers) {
            defaultCipherMap.put(cipher.getName(), cipher);
        }
    }

    private Cipher detectCipher(String cipherName) throws StorageCryptoException {
        if (defaultCipherMap.containsKey(cipherName)) {
            return defaultCipherMap.get(cipherName);
        }
        boolean invalidCipher = !customCipherMap.containsKey(cipherName);
        HELPER.check(StorageCryptoException.class, invalidCipher, MSG_ERR_UNKNOWN_CIPHER);
        return customCipherMap.get(cipherName);
    }

    private byte[] getRandomEncryptionKey() {
        return (UUID.randomUUID().toString() + UUID.randomUUID()
                .toString())
                .replace("-", "")
                .substring(0, 32)
                .getBytes(CHARSET);
    }

    private void validateCipher(AbstractCipher cipher, CustomEncryptionKey encryptionKey) throws StorageClientException, StorageCryptoException {
        try {
            String encryptedText = cipher.encrypt(VALIDATION_TEXT.getBytes(CHARSET), encryptionKey);
            String decryptedText = cipher.decrypt(encryptedText.getBytes(CHARSET), encryptionKey);

            boolean wrongDecryption = !VALIDATION_TEXT.equals(decryptedText);
            HELPER.check(StorageClientException.class, wrongDecryption,
                    MSG_ERR_INVALID_CIPHER, cipher.getName());
        } catch (StorageException se) {
            throw se;
        } catch (Exception ex) {
            String message = String.format(MSG_ERR_INVALID_CIPHER, cipher.getName());
            LOG.error(message, ex);
            throw new StorageClientException(message, ex);
        }
    }
}
