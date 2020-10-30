package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.Storage;
import com.incountry.residence.sdk.StorageConfig;
import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.crypto.testimpl.CryptoStub;
import com.incountry.residence.sdk.crypto.testimpl.DefaultCryptoWithCustomVersion;
import com.incountry.residence.sdk.crypto.testimpl.InvalidCrypto;
import com.incountry.residence.sdk.crypto.testimpl.PseudoCustomCrypto;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.crypto.DefaultCrypto;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomCryptoTest {

    private static final String CUSTOM_PASSWORD_1 = "123456789_123456789_123456789_12";
    private static final String CUSTOM_PASSWORD_2 = "123456789!123456789_123456789%Ab";
    private static final String ENV_ID = "envId";
    private static final String FAKE_ENDPOINT = "http://localhost";
    private static final String API_KEY = "apiKey";
    private static final String BODY_FOR_ENCRYPTION = "SomeSecretBody!234567=!@#$%^&**()_+|";

    @Test
    void positiveStorageInitTest() throws StorageClientException, StorageServerException {
        List<Crypto> cryptoList = Arrays.asList(new CryptoStub(true), new PseudoCustomCrypto(false));
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretKey key2 = new SecretKey(CUSTOM_PASSWORD_2, 2, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1, key2), 2);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        Storage storage = StorageImpl.getInstance(config);
        assertNotNull(storage);
        storage = StorageImpl.getInstance(config.copy().setSecretKeyAccessor(null).setCustomEncryptionConfigsList(null));
        assertNotNull(storage);
        storage = StorageImpl.getInstance(config.copy().setSecretKeyAccessor(null).setCustomEncryptionConfigsList(new ArrayList<>()));
        assertNotNull(storage);
    }

    @Test
    void customEncryptionTestPositive() throws StorageClientException {
        List<Crypto> cryptoList = Collections.singletonList(new CryptoStub(true));
        int keyVersion = 1;
        SecretKey secretKey = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData secretsData = new SecretsData(Collections.singletonList(secretKey), keyVersion);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        CryptoManager cryptoManager = new CryptoManager(secretKeyAccessor, ENV_ID, cryptoList, false, true);
        assertNotNull(cryptoManager);
    }

    @Test
    void customEncryptionTestNegative() throws StorageClientException {
        List<Crypto> cryptoList = Collections.singletonList(new InvalidCrypto(true));
        int keyVersion = 1;
        SecretKey secretKey = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData secretsData = new SecretsData(Collections.singletonList(secretKey), keyVersion);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new CryptoManager(secretKeyAccessor, ENV_ID, cryptoList, false, true));
        assertTrue(ex.getMessage().startsWith("Validation failed for custom encryption config with version"));
    }

    @Test
    void negativeNullCryptoVersionTest() throws StorageClientException {
        List<Crypto> cryptoList1 = Collections.singletonList(new DefaultCryptoWithCustomVersion(null));
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList1);
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Custom encryption has null version", ex1.getMessage());
        List<Crypto> cryptoList2 = Collections.singletonList(new DefaultCryptoWithCustomVersion(""));
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config.copy().setCustomEncryptionConfigsList(cryptoList2)));
        assertEquals("Custom encryption has null version", ex2.getMessage());
    }

    @Test
    void negativeNullCryptoTest() throws StorageClientException {
        List<Crypto> cryptoList = Arrays.asList(null, new PseudoCustomCrypto(true));
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Custom encryption list contains null", ex.getMessage());
    }

    @Test
    void negativeTestWithoutEncWithCustomCrypto() {
        List<Crypto> cryptoList = Collections.singletonList(new PseudoCustomCrypto(true));
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(null)
                .setCustomEncryptionConfigsList(cryptoList);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Custom encryption can be used only with not null SecretKeyAccessor", ex.getMessage());
    }

    @Test
    void negativeTestCustomCryptoSameVersions() throws StorageClientException {
        String cryptoVersion = "someVersion";
        Crypto crypto1 = new DefaultCryptoWithCustomVersion(cryptoVersion, true);
        Crypto crypto2 = new DefaultCryptoWithCustomVersion(cryptoVersion, false);
        List<Crypto> cryptoList = Arrays.asList(crypto1, crypto2);
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals(String.format("Custom encryption versions are not unique: %s", cryptoVersion), ex.getMessage());
    }

    @Test
    void negativeTestCustomCryptoMultipleCurrent() throws StorageClientException {
        Crypto crypto1 = new DefaultCryptoWithCustomVersion("first", true);
        Crypto crypto2 = new CryptoStub(true);
        List<Crypto> cryptoList = Arrays.asList(crypto1, crypto2);
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("There are more than one custom encryption with flag 'current == true': [CryptoStub , first]", ex.getMessage());
    }

    @Test
    void negativeTestCustomCryptoWithoutKey() throws StorageClientException {
        Crypto crypto = new CryptoStub(true);
        List<Crypto> cryptoList = Collections.singletonList(crypto);
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, false);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("There is no any SecretKey for custom encryption", ex.getMessage());
    }

    @Test
    void positiveEncryptDecrypt() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new CryptoStub(true);
        List<Crypto> cryptoList = Collections.singletonList(crypto);
        int keyVersion = 1;
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false, true);
        String text = BODY_FOR_ENCRYPTION;
        Map.Entry<String, Integer> encryptionResult = manager.encrypt(text);
        assertEquals(keyVersion, encryptionResult.getValue());
        String text2 = manager.decrypt(encryptionResult.getKey(), encryptionResult.getValue());
        assertEquals(text, text2);
    }

    @Test
    void negativeDecryptFormat() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new CryptoStub(true);
        List<Crypto> cryptoList = Collections.singletonList(crypto);
        int keyVersion = 1;
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false, true);

        String wrongCipherText1 = CryptoManager.PREFIX_CUSTOM_ENCRYPTION + UUID.randomUUID() + ":123";
        StorageCryptoException ex1 = assertThrows(StorageCryptoException.class, () -> manager.decrypt(wrongCipherText1, keyVersion));
        assertTrue(ex1.getMessage().startsWith("Unexpected exception during custom decryption - failed to parse custom encryption version:"));

        String randomUUID = UUID.randomUUID() + ":123";
        String wrongCipherText2 = randomUUID.replaceFirst("c", "d");
        StorageCryptoException ex2 = assertThrows(StorageCryptoException.class, () -> manager.decrypt(wrongCipherText2, keyVersion));
        assertEquals("Unknown cipher format", ex2.getMessage());

        Crypto anotherCrypto = new PseudoCustomCrypto(true);
        cryptoList = Collections.singletonList(anotherCrypto);
        CryptoManager anotherManager = new CryptoManager(accessor, ENV_ID, cryptoList, false, true);
        String encryptedAnother = anotherManager.encrypt(BODY_FOR_ENCRYPTION).getKey();
        StorageCryptoException ex3 = assertThrows(StorageCryptoException.class, () -> manager.decrypt(encryptedAnother, keyVersion));
        assertEquals("Unknown custom encryption version: PseudoCustomCrypto", ex3.getMessage());
    }

    @Test
    void negativeTestWithCryptoExceptions() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new PseudoCustomCrypto(true, 2, 1, true);
        List<Crypto> cryptoList = Collections.singletonList(crypto);
        int keyVersion = 1;
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false, true);
        String text = BODY_FOR_ENCRYPTION;
        Map.Entry<String, Integer> result = manager.encrypt(text);
        StorageCryptoException ex1 = assertThrows(StorageCryptoException.class, () -> manager.encrypt(text));
        assertTrue(ex1.getMessage().isEmpty());
        StorageCryptoException ex2 = assertThrows(StorageCryptoException.class, () -> manager.decrypt(result.getKey(), keyVersion));
        assertTrue(ex2.getMessage().isEmpty());
    }

    @Test
    void negativeTestWithCryptoExceptionsInInit() throws StorageClientException {
        Crypto crypto = new PseudoCustomCrypto(true, 0, 0, true);
        List<Crypto> cryptoList = Collections.singletonList(crypto);
        int keyVersion = 1;
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        StorageClientException ex = assertThrows(StorageClientException.class, () -> new CryptoManager(accessor, ENV_ID, cryptoList, false, true));
        assertTrue(ex.getMessage().startsWith("Validation failed for custom encryption config with version"));
    }

    @Test
    void negativeTestWithUnexpectedExceptions() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new PseudoCustomCrypto(true, 2, 1, false);
        List<Crypto> cryptoList = Collections.singletonList(crypto);
        int keyVersion = 1;
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false, true);
        String text = BODY_FOR_ENCRYPTION;
        Map.Entry<String, Integer> result = manager.encrypt(text);
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> manager.encrypt(text));
        assertEquals("Unexpected exception", ex1.getMessage());
        assertEquals(NullPointerException.class, ex1.getCause().getClass());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> manager.decrypt(result.getKey(), keyVersion));
        assertEquals("Unexpected exception", ex2.getMessage());
        assertEquals(NullPointerException.class, ex2.getCause().getClass());
    }

    @Test
    void negativeTestWithWrongCipher() throws StorageClientException {
        Crypto crypto = new PseudoCustomCrypto(true);
        List<Crypto> cryptoList = Collections.singletonList(crypto);
        int keyVersion = 1;
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false, true);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> manager.decrypt(BODY_FOR_ENCRYPTION, keyVersion));
        assertEquals("Unexpected exception", ex.getMessage());
        assertEquals(ArrayIndexOutOfBoundsException.class, ex.getCause().getClass());
        assertEquals("Index 1 out of bounds for length 1", ex.getCause().getMessage());
    }

    @Test
    void negativeDecryptWrongVersion() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new CryptoStub(true);
        List<Crypto> cryptoList = Collections.singletonList(crypto);
        int keyVersion = 1;
        SecretKey secretKey1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Collections.singletonList(secretKey1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false, true);
        Map.Entry<String, Integer> result = manager.encrypt(BODY_FOR_ENCRYPTION);
        assertEquals(keyVersion, result.getValue());
        StorageClientException ex = assertThrows(StorageClientException.class, () -> manager.decrypt(result.getKey(), keyVersion + 1));
        assertEquals("Unexpected exception", ex.getMessage());
        assertEquals(StorageClientException.class, ex.getCause().getClass());
        assertEquals("Secret not found for 'version'=2 with 'isForCustomEncryption'=true", ex.getCause().getMessage());
    }

    @Test
    void defaultCryptoTest() {
        DefaultCrypto crypto = new DefaultCrypto(StandardCharsets.UTF_8);
        assertTrue(crypto.isCurrent());
    }

    @Test
    void negativeTestPteAndCustomEnc() {
        List<Crypto> cryptoList = Arrays.asList(new CryptoStub(true), new PseudoCustomCrypto(false));
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setCustomEncryptionConfigsList(cryptoList);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        assertEquals("Custom encryption can be used only with not null SecretKeyAccessor", ex.getMessage());
    }
}
