package com.incountry.residence.sdk;

import com.incountry.residence.sdk.crypto.testimpl.FernetCipher;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;


class StorageConfigTest {

    @Test
    void testGetAuthEndPoints() {
        Map<String, String> authEndpoints = new HashMap<>();
        String key = "<key>";
        String value = "<value>";
        authEndpoints.put(key, value);
        StorageConfig config = new StorageConfig()
                .setAuthEndpoints(authEndpoints);
        assertNotSame(authEndpoints, config.getAuthEndpoints());
        assertEquals(authEndpoints, config.getAuthEndpoints());
    }

    @Test
    void testGetCustomEncryptionConfigsList() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider(new FernetCipher("fernet"));
        StorageConfig config = new StorageConfig()
                .setCryptoProvider(provider);
        assertEquals(provider, config.getCryptoProvider());
        assertNull(new StorageConfig().getCryptoProvider());
    }
}
