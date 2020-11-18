package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.commons.codec.digest.DigestUtils;
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

import static com.incountry.residence.sdk.tools.crypto.CryptoUtils.getHashedEncVersion;
import static com.incountry.residence.sdk.tools.crypto.CryptoUtils.validateCrypto;

/**
 * Manager of crypto functions and secrets
 */
public class CryptoManager {
    private static final Logger LOG = LogManager.getLogger(CryptoManager.class);

    private static final String MSG_ERR_NO_SECRET = "No secret provided. Cannot decrypt record: ";
    private static final String MSG_ERR_VERSION = "Secret not found for 'version'=%d with 'isForCustomEncryption'=%b";
    private static final String MSG_ERR_DECRYPTION_FORMAT = "Unknown cipher format";
    private static final String MSG_ERR_DECRYPTION = "Unknown custom encryption version: %s";
    private static final String MSG_ERR_DECRYPTION_BASE64 = "Unexpected exception during custom decryption - failed to parse custom encryption version: %s";
    private static final String MSG_ERR_UNEXPECTED = "Unexpected exception";
    private static final String MSG_NULL_SECRET = "SecretKeyAccessor returns null secret";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static final String PREFIX_PLAIN_TEXT_VERSION = "pt";
    public static final String PREFIX_CUSTOM_ENCRYPTION = "c";

    private SecretKeyAccessor keyAccessor;
    private Map<String, Crypto> customEncryptionMap;
    private Crypto currentCrypto;
    private String currentCryptoVersion;
    private final DefaultCrypto defaultCrypto = new DefaultCrypto(CHARSET);
    private String envId;
    private boolean usePTEncryption;
    private final boolean normalizeKeys;
    private final boolean hashSearchKeys;

    public CryptoManager(SecretKeyAccessor keyAccessor, String envId, List<Crypto> customEncryptionList, boolean normalizeKeys, boolean hashSearchKeys)
            throws StorageClientException {
        this.normalizeKeys = normalizeKeys;
        this.hashSearchKeys = hashSearchKeys;
        initFields(keyAccessor, envId);
        initCustomEncryptionMap(customEncryptionList);
        if (!usePTEncryption) {
            getSecret(null, currentCrypto != null);
        }
        if (!hashSearchKeys) {
            System.out.println();
        }
    }

    private void initFields(SecretKeyAccessor keyAccessor, String envId) {
        this.usePTEncryption = keyAccessor == null;
        this.keyAccessor = keyAccessor;
        this.envId = envId;
    }

    private void initCustomEncryptionMap(List<Crypto> cryptoList) throws StorageClientException {
        Map<String, Crypto> result = new HashMap<>();
        if (cryptoList != null && !cryptoList.isEmpty()) {
            SecretsData secretsData = keyAccessor.getSecretsData();
            for (Crypto crypto : cryptoList) {
                validateCrypto(crypto, secretsData, result, CHARSET, currentCrypto);
                result.put(getHashedEncVersion(crypto.getVersion(), CHARSET), crypto);
                if (crypto.isCurrent()) {
                    currentCrypto = crypto;
                    currentCryptoVersion = getHashedEncVersion(crypto.getVersion(), CHARSET);
                }
            }
        }
        this.customEncryptionMap = result;
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
        SecretKey secretKey = getSecret(null, true);
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
        SecretKey secretKey = getSecret(null, false);
        String cipher = defaultCrypto.encrypt(text, secretKey);
        return new AbstractMap.SimpleEntry<>(defaultCrypto.getVersion() + ":" + cipher, secretKey.getVersion());
    }

    private SecretKey getSecret(Integer version, boolean isForCustomEncryption) throws StorageClientException {
        SecretsData secretsData = getSecretsDataOrException();
        if (version == null) {
            version = secretsData.getCurrentVersion();
        }
        int usedVersion = version;
        Optional<SecretKey> secretKeyOptional = secretsData.getSecrets().stream()
                .filter(secretKey -> (secretKey.getVersion() == usedVersion) && (isForCustomEncryption == secretKey.isForCustomEncryption()))
                .findFirst();
        if (!secretKeyOptional.isPresent()) {
            String message = String.format(MSG_ERR_VERSION, version, isForCustomEncryption);
            LOG.error(message);
            throw new StorageClientException(message);
        }
        return secretKeyOptional.get();
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
            throw new StorageClientException(MSG_NULL_SECRET);
        }
        return result;
    }

    public String createSearchKeyHash(String key) {
        if (hashSearchKeys) {
            return createKeyHash(key);
        }
        return key;
    }

    public String createKeyHash(String key) {
        if (key == null) {
            return null;
        }
        String stringToHash = key + ":" + envId;
        return DigestUtils.sha256Hex(normalizeKeys ? stringToHash.toLowerCase() : stringToHash);
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
        SecretKey secretKey = getSecret(decryptKeyVersion, false);
        return defaultCrypto.decryptV1(cipherText, secretKey);
    }

    private String decryptV2(String cipherText, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException {
        SecretKey secretKey = getSecret(decryptKeyVersion, false);
        return defaultCrypto.decrypt(cipherText, secretKey);
    }

    private String decryptCustom(String decryptVersion, String cipherText, Integer decryptKeyVersion) throws StorageCryptoException, StorageClientException {
        if (!decryptVersion.startsWith(PREFIX_CUSTOM_ENCRYPTION)) {
            throw new StorageCryptoException(MSG_ERR_DECRYPTION_FORMAT);
        }
        Crypto crypto = customEncryptionMap.get(decryptVersion);
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
            return crypto.decrypt(decryptBase64(cipherText), getSecret(decryptKeyVersion, true));
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

    public boolean isUsePTEncryption() {
        return usePTEncryption;
    }
}
