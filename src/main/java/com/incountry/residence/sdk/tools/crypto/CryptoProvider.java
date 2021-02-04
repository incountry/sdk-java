package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.crypto.Ciphers.impl.AesGcmPbkdf10kBase64Cipher;
import com.incountry.residence.sdk.tools.crypto.Ciphers.impl.AesGcmPbkdf10kHexCipher;
import com.incountry.residence.sdk.tools.crypto.Ciphers.Cipher;
import com.incountry.residence.sdk.tools.crypto.Ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.Ciphers.DefaultCipher;
import com.incountry.residence.sdk.tools.crypto.Ciphers.impl.PlainTextBase64Cipher;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CryptoProvider {

    private static final Logger LOG = LogManager.getLogger(CryptoProvider.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final String MSG_ERR_CUSTOM_ENCRYPTION_NAME = "Custom encryption has null name";
    private static final String MSG_ERR_CUSTOM_ENCRYPTION = "Custom encryption is null";
    private static final String MSG_ERR_DECRYPTION_FORMAT = "Unknown cipher format";
    private static final String MSG_ERR_UNEXPECTED = "Unexpected exception";
    private static final String MSG_ERR_CURRENT_CIPHER = "There is no current cipher at registered cipher list";
    private static final String MSG_ERR_NO_CUSTOM_KEY = "There is no any secret for custom encryption";
    private static final String MSG_ERROR_INCORRECT_CUSTOM_CRYPTO = "Validation failed for custom encryption config with version %s";
    private static final String MSG_ERR_DECRYPTION = "Unknown custom encryption version: %s";

    public static final String PREFIX_CUSTOM_ENCRYPTION = "c";
    private static final String TEST_ENCRYPTION_TEXT = "This is test message for enc/dec_!@#$%^&*()_+|?.,~//\\=-' "
            + UUID.randomUUID().toString();

    private Cipher currentCipher;
    private String defaultCipherVersion;
    private Map<String, Cipher> customCiphers = new HashMap<>();
    private Map<String, DefaultCipher> defaultCiphers = new HashMap<>();

    public CryptoProvider() throws StorageClientException {
        this(null);
    }

    public CryptoProvider(Cipher cipher) throws StorageClientException {
        addDefaultCiphers(
                new PlainTextBase64Cipher(CHARSET),
                new AesGcmPbkdf10kHexCipher(CHARSET),
                new AesGcmPbkdf10kBase64Cipher(CHARSET)
        );
        defaultCipherVersion = AesGcmPbkdf10kBase64Cipher.CIPHER_CODE;
        if (cipher == null) {
            return;
        }
        if (cipher.getName() == null || cipher.getName().isEmpty()) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION_NAME);
            throw new StorageClientException(MSG_ERR_CUSTOM_ENCRYPTION_NAME);
        }
        customCiphers.put(getHashedEncryptionVersion(cipher.getName()), cipher);
        currentCipher = cipher;
    }

    private void addDefaultCiphers(DefaultCipher... ciphers) {
        for (DefaultCipher cipher : ciphers) {
            defaultCiphers.put(cipher.getCode(), cipher);
        }
    }

    public CryptoProvider registerCipher(Cipher cipher) throws StorageCryptoException {
        if (cipher == null) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION);
            throw new StorageCryptoException(MSG_ERR_CUSTOM_ENCRYPTION);
        }
        if (cipher.getName() == null || cipher.getName().isEmpty()) {
            LOG.error(MSG_ERR_CUSTOM_ENCRYPTION_NAME);
            throw new StorageCryptoException(MSG_ERR_CUSTOM_ENCRYPTION_NAME);
        }
        customCiphers.put(getHashedEncryptionVersion(cipher.getName()), cipher);
        return this;
    }

    public Boolean unregisterCipher(Cipher cipher) {
        if (cipher.getName() == null || cipher.getName().isEmpty()) {
            return null;
        }
        String key = getHashedEncryptionVersion(cipher.getName());
        return customCiphers.remove(key) != null;
    }

    public CipherText encrypt(String text, SecretsData secretsData) throws StorageCryptoException {
        if (secretsData == null) {
            return defaultCiphers.get(PlainTextBase64Cipher.PREFIX_PLAIN_TEXT_VERSION).encrypt(text, null);
        }
        return currentCipher != null ? encryptCustom(currentCipher, text, secretsData.getCurrentSecret())
                : defaultCiphers.get(defaultCipherVersion).encrypt(text, secretsData.getCurrentSecret());
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
        DefaultCipher defaultCipher = defaultCiphers.get(cipherTextParts[0]);
        try {

            if (defaultCipher != null) {
                return defaultCipher.decrypt(cipherTextParts[1], secret);
            }
            return cipherTextParts[0].equals(PlainTextBase64Cipher.PREFIX_PLAIN_TEXT_VERSION)
                    ? defaultCiphers.get(PlainTextBase64Cipher.PREFIX_PLAIN_TEXT_VERSION).decrypt(cipherTextParts[1], null)
                    : decryptCustom(cipherTextParts[0], cipherTextParts[1], decryptKeyVersion);

        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.error(MSG_ERR_UNEXPECTED, ex);
            throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
        }
    }

    public void validateCustomCiphers(SecretsData secretsData) throws StorageClientException {
        if (customCiphers.size() == 0)
        {
            return;
        }
        if (secretsData != null) {
            for (Secret secret : secretsData.getSecrets()) {
                if (secret instanceof CustomEncryptionKey) {
                    String message = String.format(MSG_ERR_DECRYPTION, secret.getVersion());
                    LOG.error(message);
                    throw new StorageClientException(message);
                }
            }
        }
        if (customCiphers.get(getHashedEncryptionVersion(currentCipher.getName())) != currentCipher) {
            LOG.error(MSG_ERR_CURRENT_CIPHER);
            throw new StorageClientException(MSG_ERR_CURRENT_CIPHER);
        }

        CustomEncryptionKey customEncryptionKey = secretsData.getCurrentSecret() instanceof CustomEncryptionKey
                ? (CustomEncryptionKey)secretsData.getCurrentSecret()
                : new CustomEncryptionKey(0, getRandomEncryptionKey());

        for (Cipher cipher : customCiphers.values()) {
            validateCipher(cipher, customEncryptionKey);
        }
    }

    private static byte[] getRandomEncryptionKey() {
        byte[] resultKey = new byte[32];
        System.arraycopy(UUID.randomUUID().toString().getBytes(), 0, resultKey, 0, 16);
        System.arraycopy(UUID.randomUUID().toString().getBytes(), 0, resultKey, 16, 16);
        return resultKey;
    }

    private void validateCipher(Cipher cipher, CustomEncryptionKey encryptionKey) throws StorageClientException {
        if (encryptionKey.getSecretBytes() == null || encryptionKey.getSecretBytes().length == 0) {
            LOG.error(MSG_ERR_NO_CUSTOM_KEY);
            throw new StorageClientException(MSG_ERR_NO_CUSTOM_KEY);
        }
        try {
            String encryptedText = cipher.encrypt(TEST_ENCRYPTION_TEXT.getBytes(CHARSET), encryptionKey);
            String decryptedText = cipher.decrypt(encryptedText.getBytes(CHARSET), encryptionKey);
            if (!TEST_ENCRYPTION_TEXT.equals(decryptedText)) {
                String message = String.format(MSG_ERROR_INCORRECT_CUSTOM_CRYPTO, cipher.getName());
                LOG.error(message);
                throw new StorageClientException(message);
            }
        } catch (StorageCryptoException ex) {
            String message = String.format(MSG_ERROR_INCORRECT_CUSTOM_CRYPTO, cipher.getName());
            LOG.error(message, ex);
            throw new StorageClientException(message, ex);
        }
    }

// TODO: 22.01.2021 ?
    private static CipherText encryptCustom(Cipher cipher, String text, Secret secret) {
        return null;
    }

    //
    private String decryptCustom(String decryptVersion, String cipherText, Integer decryptKeyVersion) throws StorageCryptoException {
        if (!decryptVersion.startsWith(PREFIX_CUSTOM_ENCRYPTION)) {
            throw new StorageCryptoException(MSG_ERR_DECRYPTION_FORMAT);
        }
        return null;
    }


//    private Map.Entry<String, Integer> encryptCustom(String text) throws StorageClientException, StorageCryptoException {
//        SecretKey secretKey = getSecret(null, true);
//        try {
//            String cipherText = currentCrypto.encrypt(text, secretKey);
//            String cipherTextBase64 = new String(Base64.getEncoder().encode(cipherText.getBytes(CHARSET)), CHARSET);
//            return new AbstractMap.SimpleEntry<>(currentCryptoVersion + ":" + cipherTextBase64, secretKey.getVersion());
//        } catch (StorageCryptoException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            LOG.error(MSG_ERR_UNEXPECTED, ex);
//            throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
//        }
//        return null;
//    }

    private String getHashedEncryptionVersion(String version) {
        return PREFIX_CUSTOM_ENCRYPTION + new String(Base64.getEncoder().encode(version.getBytes(CHARSET)), CHARSET);
    }


}
