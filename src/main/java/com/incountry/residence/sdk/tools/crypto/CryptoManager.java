package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Manager of crypto functions and secrets
 */
public class CryptoManager {
    private static final Logger LOG = LogManager.getLogger(CryptoManager.class);

    private static final String MSG_ERR_NO_SECRET = "No secret provided. Cannot decrypt record: ";
    private static final String MSG_ERR_VERSION = "Secret not found for 'version'=%d with 'isForCustomEncryption'=%b";
    private static final String MSG_ERR_DECRYPTION_FORMAT = "Unknown cipher format";
    private static final String MSG_ERR_DECRYPTION = "Unknown crypto version requested: %s";
    private static final String MSG_ERR_DECRYPTION_BASE64 = "Unexpected exception while getting crypto version from BASE64: %s";
    private static final String MSG_ERR_ENCRYPTION_OFF = "Encryption is turned off, but custom crypto list isn't empty";
    private static final String MSG_ERR_UNIQ_CRYPTO = "Custom crypto versions are not unique: %s";
    private static final String MSG_ERR_NULL_CRYPTO = "Custom crypto list contains null";
    private static final String MSG_ERR_NULL_CRYPTO_VERSION = "Custom crypto has null version";
    private static final String MSG_ERR_MANY_CURRENT_CRYPTO = "There are more than one custom crypto with mark 'current': [%s , %s]";
    private static final String MSG_ERROR_INCORRECT_CUSTOM_CRYPTO = "Custom crypto with version %s is invalid, test encryption is incorrect";
    private static final String MSG_ERR_UNEXPECTED = "Unexpected exception";
    private static final String MSG_ERR_NO_CUSTOM_KEY = "There is no any SecretKey for custom encryption";
    private static final String MSG_WARN_NEGATIVE_VERSION = "Record key version is negative, try to use SecretKey with version = 0";

    private static final String TEST_ENCRYPTION_TEXT = "This is test message for enc/dec_!@#$%^&*()_+|?.,~//\\=-' "
            + UUID.randomUUID().toString();

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static final String PREFIX_PLAIN_TEXT_VERSION = "pt";
    public static final String PREFIX_CUSTOM_ENCRYPTION = "c";

    private SecretKeyAccessor keyAccessor;
    private Map<String, Crypto> cryptoMap;
    private Crypto currentCrypto;
    private String currentCryptoVersion;
    private final DefaultCrypto defaultCrypto = new DefaultCrypto(CHARSET);
    private String envId;
    private boolean usePTEncryption;


    public CryptoManager(String envId) {
        this.envId = envId;
        usePTEncryption = true;
    }

    public CryptoManager(SecretKeyAccessor keyAccessor, String envId)
            throws StorageClientException {
        initFields(keyAccessor, envId);
        if (!usePTEncryption) {
            SecretsData secretsData = getSecretsDataOrException();
            getSecret(secretsData.getCurrentVersion(), false, secretsData);
        }
    }

    public CryptoManager(SecretKeyAccessor keyAccessor, String envId, List<Crypto> cryptoList)
            throws StorageClientException, StorageCryptoException {
        initFields(keyAccessor, envId);
        fillCustomCryptoMap(cryptoList);
        if (!usePTEncryption) {
            SecretsData secretsData = getSecretsDataOrException();
            getSecret(secretsData.getCurrentVersion(), currentCrypto != null, secretsData);
        }
    }

    private void initFields(SecretKeyAccessor keyAccessor, String envId) {
        this.usePTEncryption = keyAccessor == null;
        this.keyAccessor = keyAccessor;
        this.envId = envId;
    }

    private void fillCustomCryptoMap(List<Crypto> cryptoList) throws StorageClientException, StorageCryptoException {
        if (usePTEncryption && (cryptoList != null && !cryptoList.isEmpty())) {
            LOG.error(MSG_ERR_ENCRYPTION_OFF);
            throw new StorageClientException(MSG_ERR_ENCRYPTION_OFF);
        }
        Map<String, Crypto> result = new HashMap<>();
        if (cryptoList != null && !cryptoList.isEmpty()) {
            SecretsData secretsData = keyAccessor.getSecretsData();
            for (Crypto crypto : cryptoList) {
                validateAndAddOneCrypto(crypto, secretsData, result);
            }
        }
        this.cryptoMap = result;
    }

    private void validateAndAddOneCrypto(Crypto one, SecretsData secretsData, Map<String, Crypto> result)
            throws StorageClientException, StorageCryptoException {
        if (one == null) {
            LOG.error(MSG_ERR_NULL_CRYPTO);
            throw new StorageClientException(MSG_ERR_NULL_CRYPTO);
        }
        if (one.getVersion() == null || one.getVersion().isEmpty()) {
            LOG.error(MSG_ERR_NULL_CRYPTO_VERSION);
            throw new StorageClientException(MSG_ERR_NULL_CRYPTO_VERSION);
        }
        if (one.isCurrent()) {
            if (currentCrypto != null) {
                String message = String.format(MSG_ERR_MANY_CURRENT_CRYPTO, one.getVersion(), currentCrypto.getVersion());
                LOG.error(message);
                throw new StorageClientException(message);
            }
            currentCrypto = one;
            currentCryptoVersion = getHashedEncVersion(one.getVersion());
        }
        if (result.get(getHashedEncVersion(one.getVersion())) != null) {
            String message = String.format(MSG_ERR_UNIQ_CRYPTO, one.getVersion());
            LOG.error(message);
            throw new StorageClientException(message);
        }
        testEncryption(one, secretsData);
        String key = getHashedEncVersion(one.getVersion());
        result.put(key, one);
    }

    private String getHashedEncVersion(String version) {
        return PREFIX_CUSTOM_ENCRYPTION + new String(Base64.getEncoder().encode(version.getBytes(CHARSET)), CHARSET);
    }

    private void testEncryption(Crypto crypto, SecretsData secretsData) throws StorageCryptoException, StorageClientException {
        Optional<SecretKey> optional = secretsData.getSecrets().stream().filter(SecretKey::isForCustomEncryption).findFirst();
        if (!optional.isPresent()) {
            LOG.error(MSG_ERR_NO_CUSTOM_KEY);
            throw new StorageClientException(MSG_ERR_NO_CUSTOM_KEY);
        }
        SecretKey key = optional.get();
        String encryptedText = crypto.encrypt(TEST_ENCRYPTION_TEXT, key);
        String decryptedText = crypto.decrypt(encryptedText, key);
        if (!TEST_ENCRYPTION_TEXT.equals(decryptedText)) {
            String message = String.format(MSG_ERROR_INCORRECT_CUSTOM_CRYPTO, crypto.getVersion());
            LOG.error(message);
            throw new StorageCryptoException(message);
        }
    }

    public Map.Entry<String, Integer> encrypt(String text) throws StorageClientException, StorageCryptoException {
        if (usePTEncryption) {
            return encryptBase64(text);
        } else if (currentCrypto != null) {
            return encryptCustom(text);
        } else {
            return encryptDefault(text);
        }
    }

    private Map.Entry<String, Integer> encryptBase64(String text) {
        byte[] ptEncoded = Base64.getEncoder().encode(text.getBytes(CHARSET));
        return new AbstractMap.SimpleEntry<>(PREFIX_PLAIN_TEXT_VERSION + ":" + new String(ptEncoded, CHARSET), null);
    }

    private Map.Entry<String, Integer> encryptCustom(String text) throws StorageClientException, StorageCryptoException {
        SecretsData secretsData = getSecretsDataOrException();
        SecretKey secretKey = getSecret(secretsData.getCurrentVersion(), true, secretsData);
        try {
            String cipherText = currentCrypto.encrypt(text, secretKey);
            String cipherTextBase64 = new String(Base64.getEncoder().encode(cipherText.getBytes(CHARSET)), CHARSET);
            return new AbstractMap.SimpleEntry<>(currentCryptoVersion + ":" + cipherTextBase64, secretKey.getVersion());
        } catch (StorageCryptoException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.error(MSG_ERR_UNEXPECTED, ex);
            throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
        }
    }

    private Map.Entry<String, Integer> encryptDefault(String text) throws StorageClientException, StorageCryptoException {
        SecretKey secretKey = getSecret(null, false, null);
        String cipher = defaultCrypto.encrypt(text, secretKey);
        return new AbstractMap.SimpleEntry<>(defaultCrypto.getVersion() + ":" + cipher, secretKey.getVersion());
    }

    private static String createHash(String stringToHash) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(stringToHash);
    }

    private SecretKey getSecret(final Integer version, boolean isForCustomEncryption, SecretsData secretsData) throws StorageClientException {
        if (secretsData == null) {
            secretsData = getSecretsDataOrException();
        }
        Integer usedVersion = version;
        if (version == null) {
            usedVersion = secretsData.getCurrentVersion();
        }
        if (usedVersion < 0) {
            LOG.warn(MSG_WARN_NEGATIVE_VERSION);
            usedVersion = 0;
        }
        SecretKey secret = null;
        for (SecretKey item : secretsData.getSecrets()) {
            if (item.getVersion() == usedVersion && !Boolean.logicalXor(isForCustomEncryption, item.isForCustomEncryption())) {
                secret = item;
                break;
            }
        }
        if (secret == null) {
            String message = String.format(MSG_ERR_VERSION, version, isForCustomEncryption);
            LOG.error(message);
            throw new StorageClientException(message);
        }
        return secret;
    }

    public Integer getCurrentSecretVersion() throws StorageClientException {
        if (keyAccessor != null) {
            SecretsData secretsData = getSecretsDataOrException();
            return secretsData.getCurrentVersion();
        }
        return null;
    }

    private SecretsData getSecretsDataOrException() throws StorageClientException {
        SecretsData result;
        try {
            result = keyAccessor.getSecretsData();
        } catch (StorageClientException clientEx) {
            throw clientEx;
        } catch (Exception ex) {
            throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
        }
        if (result == null) {
            throw new StorageClientException("SecretKeyAccessor returns null secret");
        }
        return result;
    }

    public String createKeyHash(String key) {
        if (key == null) {
            return null;
        }
        String stringToHash = key + ":" + envId;
        return createHash(stringToHash);
    }

    public String decrypt(String cipherText, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }
        String[] parts = cipherText.split(":", 2);
        if (parts[0].equals(PREFIX_PLAIN_TEXT_VERSION)) {
            return decryptBase64(parts[1]);
        } else if (usePTEncryption) {
            String message = MSG_ERR_NO_SECRET + cipherText;
            throw new StorageCryptoException(message);
        }
        try {
            switch (parts[0]) {
                case "1":
                    return decryptV1(parts[1], decryptKeyVersion);
                case "2":
                    return decryptV2(parts[1], decryptKeyVersion);
                default:
                    return decryptCustom(parts[0], parts[1], decryptKeyVersion);
            }
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.error(MSG_ERR_UNEXPECTED, ex);
            throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
        }
    }

    private String decryptV1(String cipherText, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException {
        SecretKey secretKey = getSecret(decryptKeyVersion, false, null);
        return defaultCrypto.decryptV1(cipherText, secretKey);
    }

    private String decryptV2(String cipherText, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException {
        SecretKey secretKey = getSecret(decryptKeyVersion, false, null);
        return defaultCrypto.decrypt(cipherText, secretKey);
    }

    private String decryptCustom(String decryptVersion, String cipherText, Integer decryptKeyVersion) throws StorageCryptoException, StorageClientException {
        if (!decryptVersion.startsWith(PREFIX_CUSTOM_ENCRYPTION)) {
            throw new StorageCryptoException(MSG_ERR_DECRYPTION_FORMAT);
        }
        Crypto crypto = cryptoMap.get(decryptVersion);
        if (crypto == null) {
            try {
                String version = new String(Base64.getDecoder().decode(decryptVersion.substring(1).getBytes(CHARSET)), CHARSET);
                String message = String.format(MSG_ERR_DECRYPTION, version);
                throw new StorageCryptoException(message);
            } catch (IllegalArgumentException iex) {
                String message = String.format(MSG_ERR_DECRYPTION_BASE64, decryptVersion.substring(1));
                throw new StorageCryptoException(message, iex);
            }
        }
        try {
            return crypto.decrypt(decryptBase64(cipherText), getSecret(decryptKeyVersion, true, null));
        } catch (StorageCryptoException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.error(MSG_ERR_UNEXPECTED, ex);
            throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
        }
    }

    private String decryptBase64(String cipherText) {
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        return new String(decodedBytes, CHARSET);
    }
}