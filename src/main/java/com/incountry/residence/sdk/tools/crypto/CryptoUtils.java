package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.incountry.residence.sdk.tools.crypto.CryptoManager.PREFIX_CUSTOM_ENCRYPTION;

public class CryptoUtils {

    private static final Logger LOG = LogManager.getLogger(CryptoUtils.class);

    private static final String MSG_ERR_UNIQ_CRYPTO = "Custom encryption versions are not unique: %s";
    private static final String MSG_ERR_NULL_CRYPTO = "Custom encryption list contains null";
    private static final String MSG_ERR_NULL_CRYPTO_VERSION = "Custom encryption has null version";
    private static final String MSG_ERR_MANY_CURRENT_CRYPTO = "There are more than one custom encryption with flag 'current == true': [%s , %s]";
    private static final String MSG_ERROR_INCORRECT_CUSTOM_CRYPTO = "Validation failed for custom encryption config with version %s";
    private static final String MSG_ERR_NO_CUSTOM_KEY = "There is no any SecretKey for custom encryption";
    private static final String TEST_ENCRYPTION_TEXT = "This is test message for enc/dec_!@#$%^&*()_+|?.,~//\\=-' "
            + UUID.randomUUID().toString();

    private CryptoUtils() {
    }

    public static void validateCrypto(Crypto crypto, SecretsData secretsData, Map<String, Crypto> result, Charset charset, Crypto currentCrypto)
            throws StorageClientException, StorageCryptoException {
        if (crypto == null) {
            LOG.error(MSG_ERR_NULL_CRYPTO);
            throw new StorageClientException(MSG_ERR_NULL_CRYPTO);
        }
        if (crypto.getVersion() == null || crypto.getVersion().isEmpty()) {
            LOG.error(MSG_ERR_NULL_CRYPTO_VERSION);
            throw new StorageClientException(MSG_ERR_NULL_CRYPTO_VERSION);
        }
        if (crypto.isCurrent() && currentCrypto != null) {
            String message = String.format(MSG_ERR_MANY_CURRENT_CRYPTO, crypto.getVersion(), currentCrypto.getVersion());
            LOG.error(message);
            throw new StorageClientException(message);
        }
        if (result.get(getHashedEncVersion(crypto.getVersion(), charset)) != null) {
            String message = String.format(MSG_ERR_UNIQ_CRYPTO, crypto.getVersion());
            LOG.error(message);
            throw new StorageClientException(message);
        }
        testEncryption(crypto, secretsData);
    }

    private static void testEncryption(Crypto crypto, SecretsData secretsData) throws StorageCryptoException, StorageClientException {
        Optional<SecretKey> secretKeyOptional = secretsData.getSecrets().stream().filter(SecretKey::isForCustomEncryption).findFirst();
        if (!secretKeyOptional.isPresent()) {
            LOG.error(MSG_ERR_NO_CUSTOM_KEY);
            throw new StorageClientException(MSG_ERR_NO_CUSTOM_KEY);
        }
        SecretKey key = secretKeyOptional.get();
        String encryptedText = crypto.encrypt(TEST_ENCRYPTION_TEXT, key);
        String decryptedText = crypto.decrypt(encryptedText, key);
        if (!TEST_ENCRYPTION_TEXT.equals(decryptedText)) {
            String message = String.format(MSG_ERROR_INCORRECT_CUSTOM_CRYPTO, crypto.getVersion());
            LOG.error(message);
            throw new StorageCryptoException(message);
        }
    }

    public static String getHashedEncVersion(String version, Charset charset) {
        return PREFIX_CUSTOM_ENCRYPTION + new String(Base64.getEncoder().encode(version.getBytes(charset)), charset);
    }
}
