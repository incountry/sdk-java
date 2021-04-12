package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
import com.incountry.residence.sdk.crypto.SecretKeyAccessor;
import com.incountry.residence.sdk.crypto.SecretsData;
import com.incountry.residence.sdk.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_COUNTRY;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_ENDPOINT;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_COUNTRIES_LIST_ENDPOINT;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_CLIENT_ID;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_CLIENT_SECRET;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_DEFAULT_AUTH_ENDPOINT;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_ENDPOINT_MASK;
import static com.incountry.residence.sdk.StorageIntegrationTest.INT_INC_ENVIRONMENT_ID_OAUTH;
import static com.incountry.residence.sdk.StorageIntegrationTest.loadFromEnv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OAuthTest {
    private static final String INT_MINIPOP_COUNTRY = "INT_MINIPOP_COUNTRY";

    private static final String DEFAULT_AUTH_ENDPOINT = loadFromEnv(INT_INC_DEFAULT_AUTH_ENDPOINT);
    private static final String CLIENT_ID = loadFromEnv(INT_INC_CLIENT_ID);
    private static final String SECRET = loadFromEnv(INT_INC_CLIENT_SECRET);
    private static final String END_POINT = loadFromEnv(INT_INC_ENDPOINT);
    private static final String ENV_ID = loadFromEnv(INT_INC_ENVIRONMENT_ID_OAUTH);
    private static final String COUNTRY = loadFromEnv(INT_INC_COUNTRY);
    private static final String ENDPOINT_MASK = loadFromEnv(INT_INC_ENDPOINT_MASK);
    private static final String MINIPOP_COUNTRY = loadFromEnv(INT_MINIPOP_COUNTRY);
    private static final String COUNTRIES_LIST_ENDPOINT = loadFromEnv(INT_COUNTRIES_LIST_ENDPOINT);

    private final SecretKeyAccessor accessor;

    public OAuthTest() throws StorageClientException {
        SecretsData secretsData = SecretsDataGenerator.fromPassword("password");
        accessor = () -> secretsData;
    }

    private Storage initStorage() throws StorageClientException, StorageCryptoException {
        StorageConfig config = new StorageConfig()
                .setClientId(CLIENT_ID)
                .setClientSecret(SECRET)
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setEnvironmentId(ENV_ID)
                .setSecretKeyAccessor(accessor)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT);
        return StorageImpl.newStorage(config);
    }

    @Test
    public void testStorageWithAuthClient() throws StorageServerException, StorageClientException, StorageCryptoException {
        Storage storage = initStorage();
        String key = UUID.randomUUID().toString();
        String body = "body " + key;
        Record record = new Record(key, body);
        storage.write(COUNTRY, record);
        assertEquals(key, storage.read(COUNTRY, key).getRecordKey());

        String key2 = UUID.randomUUID().toString();
        String body2 = "body " + key2;
        Record record2 = new Record(key2, body2);
        storage.write(MINIPOP_COUNTRY, record2);
        assertEquals(key2, storage.read(MINIPOP_COUNTRY, key2).getRecordKey());
    }

    @Test
    public void positiveAuthTest() throws StorageServerException, StorageClientException {
        TokenClient client = new OAuthTokenClient(DEFAULT_AUTH_ENDPOINT, null, ENV_ID, CLIENT_ID, SECRET, HttpClients.createDefault());
        TokenClient tokenClient = ProxyUtils.createLoggingProxyForPublicMethods(client, true);
        assertNotNull(tokenClient.refreshToken(false, END_POINT, null));
        assertNotNull(tokenClient.refreshToken(false, END_POINT, null));
        assertNotNull(tokenClient.refreshToken(true, END_POINT, null));
        assertNotNull(tokenClient.refreshToken(true, END_POINT, null));
    }

    @Test
    public void authRegionTest() throws StorageClientException, StorageCryptoException {
        Map<String, String> authEndpoints = new HashMap<>();
        authEndpoints.put("emea", "https://emea.localhost");
        authEndpoints.put("apac", "https://apac.localhost");
        StorageConfig config = new StorageConfig()
                .setEnvironmentId("envId")
                .setClientId("clientId")
                .setClientSecret("clientSecret")
                .setEndpointMask("-localhost.localhost:8765")
                .setAuthEndpoints(authEndpoints)
                .setDefaultAuthEndpoint("https://emea.localhost");

        Storage prodStorage = StorageImpl.newStorage(config);
        String errorMessage = "Unexpected exception during authorization, params [OAuth URL=";
        Record record = new Record("someKey", "someBody");

        //IN mid APAC -> APAC auth
        StorageServerException ex = assertThrows(StorageServerException.class, () -> prodStorage.write("IN", record));
        assertEquals(errorMessage + "https://apac.localhost, audience=https://in-localhost.localhost:8765]", ex.getMessage());
        List<Class> expectedClasses = Arrays.asList(HttpHostConnectException.class, UnknownHostException.class);
        Assertions.assertTrue(expectedClasses.contains(ex.getCause().getClass()));
        assertTrue(ex.getCause().getMessage().contains("apac.localhost"));

        String errorEmea = "emea.localhost";
        //AE mid EMEA -> EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("AE", record));
        assertEquals(errorMessage + "https://emea.localhost, audience=https://ae-localhost.localhost:8765]", ex.getMessage());
        Assertions.assertTrue(expectedClasses.contains(ex.getCause().getClass()));
        assertTrue(ex.getCause().getMessage().contains(errorEmea));

        //US mid AMER -> EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("US", record));
        assertEquals(errorMessage + "https://emea.localhost, audience=https://us-localhost.localhost:8765]", ex.getMessage());
        Assertions.assertTrue(expectedClasses.contains(ex.getCause().getClass()));
        assertTrue(ex.getCause().getMessage().contains(errorEmea));

        //Minipop - > EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("SOME_MINIPOP_COUNTRY", record));
        assertEquals(errorMessage + "https://emea.localhost, audience=https://us-localhost.localhost:8765 https://some_minipop_country-localhost.localhost:8765]", ex.getMessage());
        Assertions.assertTrue(expectedClasses.contains(ex.getCause().getClass()));
        assertTrue(ex.getCause().getMessage().contains(errorEmea));
    }

    @Test
    void tokenAccessorTest() throws StorageClientException, StorageServerException, StorageCryptoException {
        TokenClient tokenClient = new OAuthTokenClient(DEFAULT_AUTH_ENDPOINT, null, ENV_ID, CLIENT_ID, SECRET, HttpClients.createDefault());
        String audience = "https://" + COUNTRY.toLowerCase() + ENDPOINT_MASK;
        String oauthToken = tokenClient.refreshToken(false, audience, "emea");

        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENV_ID)
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT)
                .setOauthToken(oauthToken);
        Storage storage = StorageImpl.newStorage(config);
        Record record = new Record(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        storage.write(COUNTRY, record);
        Record readRecord = storage.read(COUNTRY, record.getRecordKey());
        assertEquals(record.getRecordKey(), readRecord.getRecordKey());
        assertEquals(record.getBody(), readRecord.getBody());
        storage.delete(COUNTRY, record.getRecordKey());
        readRecord = storage.read(COUNTRY, record.getRecordKey());
        assertNull(readRecord);
    }
}
