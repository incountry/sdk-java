package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.crypto.ciphers.Cipher;
import com.incountry.residence.sdk.tools.crypto.ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.ciphers.impl.AesGcmPbkdf10kBase64Cipher;
import com.incountry.residence.sdk.tools.crypto.ciphers.impl.AesGcmPbkdf10kHexCipher;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.EncryptionKey;
import com.incountry.residence.sdk.tools.crypto.EncryptionSecret;
import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultCipherTest {

    private static final byte[] S_PASSWORD_AS_KEY = "123456789_123456789_123456789_12".getBytes(StandardCharsets.UTF_8);

    private static final String CIPHER_MESSAGE_V2 =
            "wJWrPwk0uSuhejJnN4NqG8i0dS1qY8KNMFChBzAonXczp+yFwwTiydQnr3A8Usv0fO++TKPmtirqNDmIFiX2sMGl/AG5JOVBD0hMmD/NMkOLqCSaGZlyd/SD0dt8Vap0MX6uky9AEcdaNWc7XguT4o8NsSgioimOR9btJ6Ra9hjymIMa";

    private static final String CIPHER_MESSAGE_V1 =
            "8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";

    @Test
    void aesGcmBase64CipherTest() throws StorageException {
        String text = UUID.randomUUID().toString();
        Secret secret = new EncryptionKey(1, S_PASSWORD_AS_KEY);
        Cipher cipher = new AesGcmPbkdf10kBase64Cipher(StandardCharsets.UTF_8);
        CipherText cipherMessage = cipher.encrypt(text, secret);
        assertNotNull(cipherMessage);
        String decryptedMessage = cipher.decrypt(cipherMessage.getData().substring(2), secret);
        assertEquals(text, decryptedMessage);
    }

    @Test
    void decryptFromOtherSdkTest() throws StorageException {
        Secret secret = new EncryptionKey(1, S_PASSWORD_AS_KEY);
        Cipher cipher = new AesGcmPbkdf10kBase64Cipher(StandardCharsets.UTF_8);
        String decryptedMessage = cipher.decrypt(CIPHER_MESSAGE_V2, secret);
        assertEquals("SomeSecretBody!234567=!@#$%^&**()_+|\"{}'", decryptedMessage);
    }

    @Test
    void aesGcmBase64CipherNegativeTest() throws StorageClientException {
        Secret wrongKey = new EncryptionSecret(1, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        Cipher cipher = new AesGcmPbkdf10kBase64Cipher(StandardCharsets.UTF_8);

        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> cipher.decrypt(CIPHER_MESSAGE_V2, wrongKey));
        assertEquals("Invalid cipher for decryption", ex.getMessage());

        ex = assertThrows(StorageCryptoException.class, () -> cipher.decrypt(CIPHER_MESSAGE_V2, null));
        assertEquals("No secret provided", ex.getMessage());

        ex = assertThrows(StorageCryptoException.class, () -> cipher.decrypt(null, wrongKey));
        assertEquals("Encrypted text is incorrect", ex.getMessage());
    }

    @Test
    void aesGcm10KHexCipherTest() throws StorageClientException, StorageCryptoException {
        Secret secretKey = new EncryptionSecret(0, "password".getBytes(StandardCharsets.UTF_8));
        Cipher cipher = new AesGcmPbkdf10kHexCipher(StandardCharsets.UTF_8);
        String decryptedMessage = cipher.decrypt(CIPHER_MESSAGE_V1, secretKey);
        assertEquals("InCountry", decryptedMessage);
    }

    @Test
    void decryptV1NullTest() throws StorageClientException {
        Secret secretKey = new EncryptionSecret(0, "password".getBytes(StandardCharsets.UTF_8));
        Cipher cipher = new AesGcmPbkdf10kHexCipher(StandardCharsets.UTF_8);
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> cipher.decrypt(null, secretKey));
        assertEquals("Encrypted text is incorrect", ex.getMessage());
    }

    @Test
    void defaultCurrentCipherTest() throws StorageException {
        int secretVersion = 7;
        Secret secret = new EncryptionSecret(secretVersion, "password".getBytes(StandardCharsets.UTF_8));
        Cipher cipher = new AesGcmPbkdf10kBase64Cipher(StandardCharsets.UTF_8);
        CryptoProvider cryptoProvider = new CryptoProvider();
        String text = UUID.randomUUID().toString();
        List<Secret> secrets = new ArrayList<>();
        secrets.add(secret);
        CipherText encryptResult = cryptoProvider.encrypt(text, new SecretsData(secrets, secret));
        assertNotNull(encryptResult);
        assertEquals(secretVersion, encryptResult.getKeyVersion());
        String decrypted = cipher.decrypt(encryptResult.getData().substring(2), secret);
        assertEquals(text, decrypted);
    }

    @Test
    void aesGcmDecryptNegativeTest() throws StorageClientException {
        Secret secret = new EncryptionSecret(1, "password".getBytes(StandardCharsets.UTF_8));
        Cipher cipher = new AesGcmPbkdf10kBase64Cipher(StandardCharsets.UTF_8);
        String wrongCipheredText = "Nw5G/Ut36NVnt8+6EHg9iOYWX194";
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> cipher.decrypt(wrongCipheredText, secret));
        assertEquals("Encrypted text is incorrect", ex.getMessage());
    }

}
