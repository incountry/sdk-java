package com.incountry.residence.sdk.tools.crypto.impl;

import com.incountry.residence.sdk.tools.crypto.CustomCrypto;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manager of crypto functions and secrets
 */
public class CryptoManager {
    private static final Logger LOG = LogManager.getLogger(CryptoManager.class);

    private static final String MSG_ERR_NO_SECRET = "No secret provided. Cannot decrypt record: ";
    private static final String MSG_ERR_VERSION = "Secret not found for version ";
    private static final String MSG_ERR_DECRYPTION = "Decryption error: Illegal decryption version";
    private static final String MSG_ERR_GEN_SECRET = "Secret generation exception";
    private static final String MSG_ERR_NO_ALGORITHM = "Unable to generate secret - cannot find PBKDF2WithHmacSHA512 algorithm. Please, check your JVM configuration";
    private static final String MSG_ERR_ENCRYPTION = "Data encryption error";
    private static final String MSG_ERR_ALG_EXCEPTION = "AES/GCM/NoPadding algorithm exception";
    private static final String MSG_ERR_ENCRYPTION_OFF = "Encryption is turned off, but custom crypto list isn't empty";
    private static final String MSG_ERR_UNIQ_CRYPTO = "Custom crypto versions are not unique: %s";
    private static final String MSG_ERR_NULL_CRYPTO = "Custom crypto list contains null";
    private static final String MSG_ERR_NULL_CRYPTO_VERSION = "Custom crypto has null version";
    private static final String MSG_ERR_MANY_CURRENT_CRYPTO = "There are more than one custom crypto with mark 'current': %s";
    private static final String MSG_ERROR_INCORRECT_CUSTOM_CRYPTO = "Custom crypto with version %s is invalid, test encryption is incorrect";

    private static final String TEST_ECRYPTION_TEXT = "This is test message for enc/dec_!@#$%^&*()_+|?.,~//\\=-' "
            + UUID.randomUUID().toString();

    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int AUTH_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 64;
    private static final int PBKDF2_ITERATIONS_COUNT = 10000;
    private static final String VERSION = "2";

    public static final String PREFIX_PLAIN_TEXT_VERSION = "pt";
    public static final String PREFIX_CUSTOM_ENCRYPTION = "c";

    private SecretKeyAccessor keyAccessor;
    private String envId;
    private Map<String, CustomCrypto> cryptoMap;
    private CustomCrypto currentCrypto;
    private boolean usePTEncryption = false;


    public CryptoManager(String envId) {
        this.envId = envId;
        usePTEncryption = true;
    }

    public CryptoManager(SecretKeyAccessor keyAccessor, List<CustomCrypto> cryptoList) throws StorageClientException, StorageCryptoException {
        this(keyAccessor, null, cryptoList);
    }

    public CryptoManager(SecretKeyAccessor keyAccessor, String envId, List<CustomCrypto> cryptoList) throws StorageClientException, StorageCryptoException {
        this.usePTEncryption = keyAccessor == null;
        this.keyAccessor = keyAccessor;
        this.envId = envId;
        fillCustomCryptoMap(cryptoList);
    }

    private void fillCustomCryptoMap(List<CustomCrypto> cryptoList) throws StorageClientException, StorageCryptoException {
        if (usePTEncryption && (cryptoList != null && cryptoList.isEmpty())) {
            LOG.error(MSG_ERR_ENCRYPTION_OFF);
            throw new StorageClientException(MSG_ERR_ENCRYPTION_OFF);
        }
        Map<String, CustomCrypto> result = new HashMap<>();
        if (cryptoList != null && !cryptoList.isEmpty()) {
            SecretsData secretsData = keyAccessor.getSecretsData();
            for (CustomCrypto one : cryptoList) {
                validateAndAddOneCypto(one, secretsData, result);
            }
        }
        this.cryptoMap = result;
    }

    private void validateAndAddOneCypto(CustomCrypto one, SecretsData secretsData, Map<String, CustomCrypto> result) throws StorageClientException, StorageCryptoException {
        if (one == null) {
            LOG.error(MSG_ERR_NULL_CRYPTO);
            throw new StorageClientException(MSG_ERR_NULL_CRYPTO);
        }
        if (one.getVersion() == null || one.getVersion().isEmpty()) {
            LOG.error(MSG_ERR_NULL_CRYPTO_VERSION);
            throw new StorageClientException(MSG_ERR_NULL_CRYPTO_VERSION);
        }
        if (one.isCurrent()) {
            if (currentCrypto != null && !currentCrypto.equals(one)) {
                String message = String.format(MSG_ERR_MANY_CURRENT_CRYPTO, one.getVersion());
                LOG.error(message);
                throw new StorageClientException(message);
            }
            currentCrypto = one;
        }
        if (result.get(one.getVersion()) != null) {
            String message = String.format(MSG_ERR_UNIQ_CRYPTO, one.getVersion());
            LOG.error(message);
            throw new StorageClientException(message);
        }
        testEncryption(one, secretsData);
        result.put(one.getVersion(), one);
    }

    private void testEncryption(CustomCrypto customCrypto, SecretsData secretsData) throws StorageCryptoException, StorageClientException {
        SecretKey key = secretsData.getSecrets().stream().filter(SecretKey::isForCustomEncryption).findFirst().get();
        String encryptedText = customCrypto.encrypt(TEST_ECRYPTION_TEXT, key);
        String decryptedText = customCrypto.decrypt(encryptedText, key);
        if (!TEST_ECRYPTION_TEXT.equals(decryptedText)) {
            String message = String.format(MSG_ERROR_INCORRECT_CUSTOM_CRYPTO, customCrypto.getVersion());
            LOG.error(message);
            throw new StorageCryptoException(message);
        }


    }

    public Map.Entry<String, Integer> encrypt(String plainText) throws StorageClientException, StorageCryptoException {
        if (usePTEncryption) {
            byte[] ptEncoded = Base64.getEncoder().encode(plainText.getBytes(CHARSET));
            return new AbstractMap.SimpleEntry<>(PREFIX_PLAIN_TEXT_VERSION + ":" + new String(ptEncoded, CHARSET), null);
        }
        if (currentCrypto != null) {
            //todo
        }
        byte[] clean = plainText.getBytes(CHARSET);
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        SecretKey secretKey = getSecret(null);
        byte[] key = getKey(salt, secretKey);
        byte[] iv = generateRandomBytes(IV_LENGTH);

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH * 8, iv);

        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            encrypted = cipher.doFinal(clean);
        } catch (GeneralSecurityException e) {
            throwStorageCryptoException(MSG_ERR_ALG_EXCEPTION, e);
        }

        byte[] resultByteArray = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.write(encrypted);
            resultByteArray = outputStream.toByteArray();
        } catch (IOException e) {
            throwStorageCryptoException(MSG_ERR_ENCRYPTION, e);
        }

        byte[] encoded = Base64.getEncoder().encode(resultByteArray);
        return new AbstractMap.SimpleEntry<>(VERSION + ":" + new String(encoded, CHARSET), secretKey.getVersion());
    }

    private byte[] getKey(byte[] salt, SecretKey secretKey) throws StorageCryptoException {
        if (secretKey.isForCustomEncryption() != null && secretKey.isForCustomEncryption()) {
            return secretKey.getSecret().getBytes(CHARSET);
        }
        return generateStrongPasswordHash(secretKey.getSecret(), salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);
    }

    private static String createHash(String stringToHash) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(stringToHash);
    }

    private String decryptUnpacked(byte[] parts, Integer decryptKeyVersion) throws StorageCryptoException, StorageClientException {
        byte[] salt = Arrays.copyOfRange(parts, 0, 64);
        byte[] iv = Arrays.copyOfRange(parts, 64, 76);
        byte[] encrypted = Arrays.copyOfRange(parts, 76, parts.length);

        byte[] decryptedText = null;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            byte[] key = getKey(salt, getSecret(decryptKeyVersion));

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            decryptedText = cipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throwStorageCryptoException(MSG_ERR_ENCRYPTION, e);
        }
        return new String(decryptedText, CHARSET);
    }

    private SecretKey getSecret(Integer version) throws StorageClientException {
        SecretsData secretsData = getSecretsDataOrException();
        if (version == null) {
            version = secretsData.getCurrentVersion();
        }
        SecretKey secret = null;
        for (SecretKey item : secretsData.getSecrets()) {
            if (item.getVersion() == version) {
                secret = item;
                break;
            }
        }
        if (secret == null) {
            String message = MSG_ERR_VERSION + version;
            LOG.error(message);
            throw new StorageClientException(message);
        }
        return secret;
    }

    public Integer getCurrentSecretVersion() throws StorageClientException {
        if (keyAccessor != null) {
            SecretsData secretsData = getSecretsDataOrException();
            if (secretsData != null) {
                return secretsData.getCurrentVersion();
            }
        }
        return null;
    }

    private SecretsData getSecretsDataOrException() throws StorageClientException {
        try {
            return keyAccessor.getSecretsData();
        } catch (StorageClientException clientEx) {
            throw clientEx;
        } catch (Exception ex) {
            throw new StorageClientException("Unexpected exception", ex);
        }
    }

    public String createKeyHash(String key) {
        if (key == null) {
            return null;
        }
        String stringToHash = key + ":" + envId;
        return createHash(stringToHash);
    }

    public String decrypt(String cipherText, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException {
        if (cipherText == null) {
            return null;
        }
        String[] parts = cipherText.split(":", 2);
        if (parts[0].equals(PREFIX_PLAIN_TEXT_VERSION)) {
            return decryptVPT(parts[1]);
        } else if (usePTEncryption) {
            String message = MSG_ERR_NO_SECRET + cipherText;
            throwStorageCryptoException(message, null);
        }
        switch (parts[0]) {
            case "1":
                return decryptV1(parts[1], decryptKeyVersion);
            case "2":
                return decryptV2(parts[1], decryptKeyVersion);
            default:
                return decryptCustom(parts[1], decryptKeyVersion);
        }
    }

    private String decryptCustom(String part, Integer decryptKeyVersion) {
        if (part)
            return null;
    }

    private static String throwStorageCryptoException(String message, Exception ex) throws StorageCryptoException {
        if (ex == null) {
            throw new StorageCryptoException(message);
        } else {
            throw new StorageCryptoException(message, ex);
        }

    }

    private String decryptV2(String cipherText, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException {
        byte[] parts = Base64.getDecoder().decode(cipherText);
        return this.decryptUnpacked(parts, decryptKeyVersion);
    }

    private String decryptV1(String cipherText, Integer decryptKeyVersion) throws StorageClientException, StorageCryptoException {
        byte[] parts = DatatypeConverter.parseHexBinary(cipherText);
        return this.decryptUnpacked(parts, decryptKeyVersion);
    }

    private String decryptVPT(String cipherText) {
        byte[] ptBytes = Base64.getDecoder().decode(cipherText);
        return new String(ptBytes, CHARSET);
    }

    private static byte[] generateStrongPasswordHash(String password, byte[] salt, int iterations, int length) throws StorageCryptoException {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, length * 8);
        byte[] strongPasswordHash = null;
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            strongPasswordHash = skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throwStorageCryptoException(MSG_ERR_NO_ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throwStorageCryptoException(MSG_ERR_GEN_SECRET, e);
        }
        return strongPasswordHash;
    }

    private static byte[] generateRandomBytes(int length) {
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        randomSecureRandom.nextBytes(randomBytes);
        return randomBytes;
    }
}