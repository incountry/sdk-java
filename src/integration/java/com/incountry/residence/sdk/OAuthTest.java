package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.incountry.residence.sdk.StorageIntegrationTest.INTEGR_ENV_KEY_COUNTRY;
import static com.incountry.residence.sdk.StorageIntegrationTest.INTEGR_ENV_KEY_ENDPOINT;
import static com.incountry.residence.sdk.StorageIntegrationTest.INTEGR_ENV_KEY_ENVID;
import static com.incountry.residence.sdk.StorageIntegrationTest.loadFromEnv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("waiting for QA auth server")
public class OAuthTest {

    private static final String INTEGR_ENC_AUTH_ENDPOINT = "INT_INC_AUTH_ENDPOINT";
    private static final String INTEGR_ENC_CLIENT_ID = "INT_INC_CLIENT_ID";
    private static final String INTEGR_ENC_CLIENT_SECRET = "INT_INC_CLIENT_SECRET";

    private static final String AUTH_URL = loadFromEnv(INTEGR_ENC_AUTH_ENDPOINT);
    private static final String CLIENT_ID = loadFromEnv(INTEGR_ENC_CLIENT_ID);
    private static final String SECRET = loadFromEnv(INTEGR_ENC_CLIENT_SECRET);
    private static final String END_POINT = loadFromEnv(INTEGR_ENV_KEY_ENDPOINT);
    private static final String ENV_ID = loadFromEnv(INTEGR_ENV_KEY_ENVID);
    private static final String COUNTRY = loadFromEnv(INTEGR_ENV_KEY_COUNTRY);

    private static final Integer HTTP_TIMEOUT = 30_000;

    private final SecretKeyAccessor accessor;

    public OAuthTest() throws StorageClientException {
        SecretsData secretsData = SecretsDataGenerator.fromPassword("password");
        accessor = () -> secretsData;
    }

    private Storage initStorage() throws StorageServerException, StorageClientException {
        StorageConfig config = new StorageConfig()
                .setClientId(CLIENT_ID)
                .setClientSecret(SECRET)
                .setAuthEndPoint(AUTH_URL)
                .setEnvId(ENV_ID)
                .setEndPoint(END_POINT)
                .setSecretKeyAccessor(accessor);
        return StorageImpl.getInstance(config);
    }

    @Test
    public void testStorageWithAuthClient() throws StorageServerException, StorageClientException, StorageCryptoException {
        Storage storage = initStorage();
        String key = UUID.randomUUID().toString();
        String body = "body " + key;
        Record record = new Record(key, body);
        storage.write(COUNTRY, record);
        assertEquals(record, storage.read(COUNTRY, key));
    }

    @Test
    public void positiveAuthTest() throws StorageServerException {
        TokenClient tokenClient = ProxyUtils.createLoggingProxyForPublicMethods(new OAuthTokenClient(AUTH_URL, ENV_ID, CLIENT_ID, SECRET, HTTP_TIMEOUT));
        assertNotNull(tokenClient.getToken(END_POINT));
        assertNotNull(tokenClient.refreshToken(true, END_POINT));
    }
}
