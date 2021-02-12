package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptoProviderTest {

    @Test
    void illegalPtEncryptedVersionTest() throws StorageClientException {
        CryptoProvider cryptoManager = new CryptoProvider();
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> cryptoManager.decrypt("someCypherText", null, 100500));
        assertEquals("Unknown cipher format", ex.getMessage());
    }

    @Test
    void decryptPtNullTest() throws StorageException {
        CryptoProvider cryptoManager = new CryptoProvider();
        assertNull(cryptoManager.decrypt("", null, 100500));
    }

    @Test
    void decryptAesGcm10KHexTest() throws StorageException {
        String cipherMessageV1 =
            "1:8b02d29be1521e992b49a9408f2777084e9d8195e4a3392c68c70545eb559670b70ec928c8eeb2e34f118d32a23d77abdcde38446241efacb71922579d1dcbc23fca62c1f9ec5d97fbc3a9862c0a9e1bb630aaa3585eac160a65b24a96af5becef3cdc2b29";
        CryptoProvider cryptoManager = new CryptoProvider();
        String decryptedMessage =
                cryptoManager.decrypt(cipherMessageV1, SecretsDataGenerator.fromPassword("password"), 0);
        assertEquals("InCountry", decryptedMessage);
    }

    @Test
    void decryptUnknownTest() throws StorageClientException {
        String cipherMessageV1 = "unknown";
        CryptoProvider cryptoManager = new CryptoProvider();
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> cryptoManager.decrypt(cipherMessageV1, SecretsDataGenerator.fromPassword("password"), 0));
        assertEquals("Unknown cipher format", ex.getMessage());
    }
}
