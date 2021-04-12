package com.incountry.residence.sdk;

import com.incountry.residence.sdk.crypto.testimpl.FernetCipher;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageConfigTest {
    @Test
    void getAuthEndPointsPositive() {
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
    void getCustomEncryptionConfigsListPositive() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider(new FernetCipher("fernet"));
        StorageConfig config = new StorageConfig()
                .setCryptoProvider(provider);
        assertEquals(provider, config.getCryptoProvider());
        assertNull(new StorageConfig().getCryptoProvider());
    }

    @Test
    void getOauthTokenAccessor() throws StorageClientException {
        String token = "token_" + UUID.randomUUID().toString();
        StorageConfig config = new StorageConfig()
                .setOauthToken(token);
        assertEquals(token, config.getOauthTokenAccessor().getToken());

        String token2 = "token_" + UUID.randomUUID().toString();
        config = new StorageConfig()
                .setOauthTokenAccessor(() -> token2);
        assertEquals(token2, config.getOauthTokenAccessor().getToken());

        StorageClientException ex = assertThrows(StorageClientException.class, () -> new StorageConfig().setOauthToken(null));
        assertEquals("OAuth2 token is null or empty", ex.getMessage());
    }
}
