package com.incountry.residence.sdk;

import com.incountry.residence.sdk.crypto.cipher.CipherStub;
import com.incountry.residence.sdk.tools.crypto.ciphers.Cipher;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;


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
    void testGetCustomEncryptionConfigsList() {
        List<Cipher> cryptoList = new ArrayList<>();
        cryptoList.add(new CipherStub("code"));
        StorageConfig config = new StorageConfig()
                .setCustomEncryptionConfigsList(cryptoList);
        assertNotSame(cryptoList, config.getCustomEncryptionConfigsList());
        assertEquals(cryptoList, config.getCustomEncryptionConfigsList());
    }
}
