package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.crypto.HashUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HashUtilsTest {
    @Test
    void testNormalizeKeys() {
        String someKey = "FilterValue123~!@#$%^&*()_+";
        HashUtils utils = new HashUtils("envId", true);
        assertEquals(utils.getSha256Hash(someKey), utils.getSha256Hash(someKey.toLowerCase()));
        assertEquals(utils.getSha256Hash(someKey), utils.getSha256Hash(someKey.toUpperCase()));
    }
}
