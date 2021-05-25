package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.crypto.cipher.AesGcmPbkdf10kBase64Cipher;
import com.incountry.residence.sdk.tools.crypto.cipher.AesGcmPbkdf10kHexCipher;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultCipherTest {
    @Test
    void aesGcmPbkdf10kHexCipherNegative() throws StorageClientException {
        AesGcmPbkdf10kHexCipher cipher = new AesGcmPbkdf10kHexCipher(null);
        Secret secret = new EncryptionSecret("password".getBytes(StandardCharsets.UTF_8), 1);
        String shortEncryptedText = "123456789012345678901234567890AB";
        String encryptedText = "123456789012345678901234567890AB123456789012345678901234567890AB123456789012345678901" +
                "123456789012345678901234567890AB123456789012345678901234567890AB123456789012345678901";
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () ->
                cipher.decrypt(shortEncryptedText, secret));
        assertEquals("Encrypted text is incorrect", ex.getMessage());
        ex = assertThrows(StorageCryptoException.class, () ->
                cipher.decrypt(encryptedText, secret));
        assertEquals("Charset is null", ex.getMessage());
        ex = assertThrows(StorageCryptoException.class, () ->
                cipher.encrypt(encryptedText, secret));
        assertEquals("Cipher doesn't support encryption", ex.getMessage());
    }

    @Test
    void aesGcmPbkdf10kBase64CipherNegative() throws StorageClientException {
        AesGcmPbkdf10kBase64Cipher cipher = new AesGcmPbkdf10kBase64Cipher(StandardCharsets.UTF_8);
        Secret secret = new EncryptionSecret("password".getBytes(StandardCharsets.UTF_8), 1);
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> cipher.decrypt(null, secret));
        assertEquals("Encrypted text is incorrect", ex.getMessage());
        ex = assertThrows(StorageCryptoException.class, () -> cipher.decrypt("", secret));
        assertEquals("Encrypted text is incorrect", ex.getMessage());
    }
}
