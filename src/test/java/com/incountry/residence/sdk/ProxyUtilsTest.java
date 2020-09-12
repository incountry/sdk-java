package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.models.PopApiResponse;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProxyUtilsTest {

    static class FakeHttpAgent implements HttpAgent {
        @Override
        public PopApiResponse request(String url, String method, String body, Map<Integer, ApiResponse> codeMap, String audience, String region, int retryCount, String contentType) throws StorageServerException, StorageClientException {
            doNothing();
            throw new NullPointerException();
        }

        private void doNothing() {
        }
    }

    @Test
    void testProxyException() {
        HttpAgent agent = ProxyUtils.createLoggingProxyForPublicMethods(new FakeHttpAgent());
        assertThrows(NullPointerException.class, () -> agent.request(null, null, null, null, null, null, 0, null));
    }
}
