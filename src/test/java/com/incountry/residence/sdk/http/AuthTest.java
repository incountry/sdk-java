package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.impl.DefaultTokenGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthTest {

    @Test
    public void tokenGeneratorTest() throws StorageServerException {
        DefaultTokenGenerator generator = new DefaultTokenGenerator(new FakeAuthClient(0));
        for (int i = 0; i < 1_000; i++) {
            assertNotNull(generator.getToken());
        }
        generator.refreshToken(false);
        generator.refreshToken(true);
    }
}
