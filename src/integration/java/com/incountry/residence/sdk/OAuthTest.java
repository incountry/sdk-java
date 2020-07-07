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
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.incountry.residence.sdk.StorageIntegrationTest.INTEGR_ENV_KEY_COUNTRY;
import static com.incountry.residence.sdk.StorageIntegrationTest.INTEGR_ENV_KEY_ENDPOINT;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_COUNTRIES_LIST_ENDPOINT;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_CLIENT_ID;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_CLIENT_SECRET;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_DEFAULT_AUTH_ENDPOINT;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_ENPOINT_MASK;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_ENVIRONMENT_ID_OAUTH;
import static com.incountry.residence.sdk.StorageIntegrationTest.loadFromEnv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OAuthTest {
    private static final String INT_MINIPOP_COUNTRY = "INT_MINIPOP_COUNTRY";

    private static final String DEFAULT_AUTH_ENDPOINT = loadFromEnv(INT_INC_DEFAULT_AUTH_ENDPOINT);
    private static final String CLIENT_ID = loadFromEnv(INT_INC_CLIENT_ID);
    private static final String SECRET = loadFromEnv(INT_INC_CLIENT_SECRET);
    private static final String END_POINT = loadFromEnv(INTEGR_ENV_KEY_ENDPOINT);
    private static final String ENV_ID = loadFromEnv(INT_INC_ENVIRONMENT_ID_OAUTH);
    private static final String COUNTRY = loadFromEnv(INTEGR_ENV_KEY_COUNTRY);
    private static final String ENDPOINT_MASK = loadFromEnv(INT_INC_ENPOINT_MASK);
    private static final String MINIPOP_COUNTRY = loadFromEnv(INT_MINIPOP_COUNTRY);
    private static final String COUNTRIES_LIST_ENDPOINT = loadFromEnv(INT_COUNTRIES_LIST_ENDPOINT);

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
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setEnvId(ENV_ID)
                .setSecretKeyAccessor(accessor)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT);
        return StorageImpl.getInstance(config);
    }

    private CloseableHttpClient initHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(5);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(HTTP_TIMEOUT)
                .setSocketTimeout(HTTP_TIMEOUT)
                .build();
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Test
    public void testStorageWithAuthClient() throws StorageServerException, StorageClientException, StorageCryptoException {
        Storage storage = initStorage();
        String key = UUID.randomUUID().toString();
        String body = "body " + key;
        Record record = new Record(key, body);
        storage.write(COUNTRY, record);
        assertEquals(record, storage.read(COUNTRY, key));

        String key2 = UUID.randomUUID().toString();
        String body2 = "body " + key2;
        Record record2 = new Record(key2, body2);
        storage.write(MINIPOP_COUNTRY, record2);
        assertEquals(record2, storage.read(MINIPOP_COUNTRY, key2));
    }

    @Test
    public void positiveAuthTest() throws StorageServerException, StorageClientException {
        TokenClient tokenClient = ProxyUtils.createLoggingProxyForPublicMethods(new OAuthTokenClient(DEFAULT_AUTH_ENDPOINT, null, ENV_ID, CLIENT_ID, SECRET, initHttpClient()));
        assertNotNull(tokenClient.getToken(END_POINT, null));
        assertNotNull(tokenClient.getToken(END_POINT, null));
        assertNotNull(tokenClient.refreshToken(true, END_POINT, null));
        assertNotNull(tokenClient.refreshToken(true, END_POINT, null));
    }

    @Test
    public void authRegionTest() throws StorageServerException, StorageClientException {
        Map<String, String> authEndpoints = new HashMap<>();
        authEndpoints.put("emea", "emea.localhost");
        authEndpoints.put("apac", "apac.localhost");
        StorageConfig config = new StorageConfig()
                .setEnvId("envId")
                .setClientId("clientId")
                .setClientSecret("clientSecret")
                .setEndpointMask("-localhost.localhost:8765")
                .setAuthEndpoints(authEndpoints)
                .setDefaultAuthEndpoint("emea.localhost");

        Storage prodStorage = StorageImpl.getInstance(config);
        String errorMessage = "Unexpected exception during authorization";
        Record record = new Record("someKey", "someBody");

        //IN mid APAC -> APAC auth
        StorageServerException ex = assertThrows(StorageServerException.class, () -> prodStorage.write("IN", record));
        assertEquals(errorMessage, ex.getMessage());
        assertEquals(ClientProtocolException.class, ex.getCause().getClass());
        assertEquals(ProtocolException.class, ex.getCause().getCause().getClass());
        assertEquals("Target host is not specified", ex.getCause().getCause().getMessage());

        //AE mid EMEA -> EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("AE", record));
        assertEquals(errorMessage, ex.getMessage());
        assertEquals(ClientProtocolException.class, ex.getCause().getClass());
        assertEquals(ProtocolException.class, ex.getCause().getCause().getClass());
        assertEquals("Target host is not specified", ex.getCause().getCause().getMessage());

        //US mid AMER -> EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("US", record));
        assertEquals(errorMessage, ex.getMessage());
        assertEquals(ClientProtocolException.class, ex.getCause().getClass());
        assertEquals(ProtocolException.class, ex.getCause().getCause().getClass());
        assertEquals("Target host is not specified", ex.getCause().getCause().getMessage());

        //Minipop - > EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("SOME_MINIPOP_COUNTRY", record));
        assertEquals(errorMessage, ex.getMessage());
        assertEquals(ClientProtocolException.class, ex.getCause().getClass());
        assertEquals(ProtocolException.class, ex.getCause().getCause().getClass());
        assertEquals("Target host is not specified", ex.getCause().getCause().getMessage());
    }
}
