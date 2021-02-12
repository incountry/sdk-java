package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.crypto.HashUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HashProviderTest {

    @Test
    void normalizeKeyTest() {
        String key = "SomeKey";
        String environmentId = "environmentId";
        HashUtils hashProvider = new HashUtils(environmentId, false);
        String hash1 = hashProvider.getSha256Hash(key);
        assertNotNull(hash1);
        HashUtils hashProvider2 = new HashUtils(environmentId, true);
        String hash2 = hashProvider2.getSha256Hash(key);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2);
    }

}
