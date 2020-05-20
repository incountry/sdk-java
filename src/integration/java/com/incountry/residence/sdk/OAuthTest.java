package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.impl.HttpDaoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.AuthClient;
import com.incountry.residence.sdk.tools.http.impl.DefaultTokenGenerator;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import com.incountry.residence.sdk.tools.http.impl.DefaultAuthClient;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static com.incountry.residence.sdk.StorageIntegrationTest.INTEGR_ENV_KEY_COUNTRY;
import static com.incountry.residence.sdk.StorageIntegrationTest.INTEGR_ENV_KEY_ENDPOINT;
import static com.incountry.residence.sdk.StorageIntegrationTest.INTEGR_ENV_KEY_ENVID;
import static com.incountry.residence.sdk.StorageIntegrationTest.loadFromEnv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private final SecretKeyAccessor accessor;

    public OAuthTest() throws StorageClientException {
        SecretsData secretsData = SecretsDataGenerator.fromPassword("password");
        accessor = () -> secretsData;
    }

    private Storage initStorage(AuthClient authClient) throws StorageServerException, StorageClientException {
        authClient.setCredentials(CLIENT_ID, SECRET, AUTH_URL, "envId");
        Dao dao = new HttpDaoImpl(END_POINT, new HttpAgentImpl(ENV_ID, StandardCharsets.UTF_8), new DefaultTokenGenerator(authClient));
        return StorageImpl.getInstance(ENV_ID, accessor, dao);
    }

    @Test
    public void testStorageWithAuthClient() throws StorageServerException, StorageClientException, StorageCryptoException {
        AuthClient authClient = new DefaultAuthClient();
        Storage storage = initStorage(authClient);
        String key = UUID.randomUUID().toString();
        String body = "body " + key;
        Record record = new Record(key, body);
        storage.write(COUNTRY, record);
        assertEquals(record, storage.read(COUNTRY, key));
    }

    @Test
    public void positiveAuthTest() throws StorageServerException {
        AuthClient authClient = new DefaultAuthClient();
        authClient.setCredentials(CLIENT_ID, SECRET, AUTH_URL, ENV_ID);
        Map.Entry<String, Long> token = authClient.newToken(END_POINT);
        assertNotNull(token.getValue());
        assertTrue(System.currentTimeMillis() < token.getValue());
    }
}
