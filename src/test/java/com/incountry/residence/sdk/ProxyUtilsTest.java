package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.models.RequestParameters;
import com.incountry.residence.sdk.tools.models.ApiResponse;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProxyUtilsTest {

    static class FakeHttpAgent implements HttpAgent {
        @Override
        public ApiResponse request(String url, String body, String audience, String region, int retryCount, RequestParameters requestParameters) throws StorageServerException, StorageClientException {
            doNothing();
            throw new NullPointerException();
        }

        private void doNothing() {
        }
    }

    @Test
    void testProxyException() {
        HttpAgent agent = ProxyUtils.createLoggingProxyForPublicMethods(new FakeHttpAgent());
        assertThrows(NullPointerException.class, () -> agent.request(null, null, null, null, 0, null));
    }
}
