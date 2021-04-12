package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.crypto.SecretKeyFactory;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecretKeyFactoryTest {
    @Test
    void getKeyNegative() {
        StorageCryptoException ex = assertThrows(StorageCryptoException.class, () -> SecretKeyFactory.getKey(null, null, 5));
        assertEquals("Secret can't be null", ex.getMessage());
    }
}
