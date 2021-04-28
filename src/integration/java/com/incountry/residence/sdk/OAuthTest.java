package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.OAuthTokenClient;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OAuthTest {
    private static final String COUNTRY = CredentialsHelper.getMidPopCountry(true);

    @Test
    public void positiveAuthTest() throws StorageServerException, StorageClientException {
        StorageConfig config = CredentialsHelper.getConfigWithOauth();
        TokenClient client = new OAuthTokenClient(config.getDefaultAuthEndpoint(), null, config.getEnvironmentId(),
                config.getClientId(), config.getClientSecret(), HttpClients.createDefault());
        TokenClient tokenClient = ProxyUtils.createLoggingProxyForPublicMethods(client, true);
        String endPoint = "https://" + CredentialsHelper.getMidPopCountry(true).toLowerCase() + config.getEndpointMask();
        assertNotNull(tokenClient.refreshToken(false, endPoint, null));
        assertNotNull(tokenClient.refreshToken(false, endPoint, null));
        assertNotNull(tokenClient.refreshToken(true, endPoint, null));
        assertNotNull(tokenClient.refreshToken(true, endPoint, null));
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

        Storage prodStorage = StorageImpl.getInstance(config);
        String errorMessage = "Unexpected exception during authorization, params [OAuth URL=";
        Record myRecord = new Record("someKey", "someBody");

        //IN mid APAC -> APAC auth
        StorageServerException ex = assertThrows(StorageServerException.class, () -> prodStorage.write("IN", myRecord));
        assertEquals(errorMessage + "https://apac.localhost, audience=https://in-localhost.localhost:8765]", ex.getMessage());
        List<Class> expectedClasses = Arrays.asList(HttpHostConnectException.class, UnknownHostException.class);
        Assertions.assertTrue(expectedClasses.contains(ex.getCause().getClass()));
        assertTrue(ex.getCause().getMessage().contains("apac.localhost"));

        String errorEmea = "emea.localhost";
        //AE mid EMEA -> EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("AE", myRecord));
        assertEquals(errorMessage + "https://emea.localhost, audience=https://ae-localhost.localhost:8765]", ex.getMessage());
        Assertions.assertTrue(expectedClasses.contains(ex.getCause().getClass()));
        assertTrue(ex.getCause().getMessage().contains(errorEmea));

        //US mid AMER -> EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("US", myRecord));
        assertEquals(errorMessage + "https://emea.localhost, audience=https://us-localhost.localhost:8765]", ex.getMessage());
        Assertions.assertTrue(expectedClasses.contains(ex.getCause().getClass()));
        assertTrue(ex.getCause().getMessage().contains(errorEmea));

        //Minipop - > EMEA auth
        ex = assertThrows(StorageServerException.class, () -> prodStorage.write("SOME_MINIPOP_COUNTRY", myRecord));
        assertEquals(errorMessage + "https://emea.localhost, audience=https://us-localhost.localhost:8765 https://some_minipop_country-localhost.localhost:8765]", ex.getMessage());
        Assertions.assertTrue(expectedClasses.contains(ex.getCause().getClass()));
        assertTrue(ex.getCause().getMessage().contains(errorEmea));
    }

    @Test
    void tokenAccessorTest() throws StorageClientException, StorageServerException, StorageCryptoException {
        StorageConfig config = CredentialsHelper.getConfigWithOauth();
        TokenClient tokenClient = new OAuthTokenClient(config.getDefaultAuthEndpoint(), null, config.getEnvironmentId(),
                config.getClientId(), config.getClientSecret(), HttpClients.createDefault());
        String audience = "https://" + COUNTRY.toLowerCase() + config.getEndpointMask();
        String oauthToken = tokenClient.refreshToken(false, audience, "emea");

        config = CredentialsHelper.getConfigWithOauth()
                .setClientId(null)
                .setClientSecret(null)
                .setOauthToken(oauthToken);
        Storage storage = StorageImpl.getInstance(config);
        Record myRecord = new Record(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        storage.write(COUNTRY, myRecord);
        Record readRecord = storage.read(COUNTRY, myRecord.getRecordKey());
        assertEquals(myRecord.getRecordKey(), readRecord.getRecordKey());
        assertEquals(myRecord.getBody(), readRecord.getBody());
        storage.delete(COUNTRY, myRecord.getRecordKey());
        readRecord = storage.read(COUNTRY, myRecord.getRecordKey());
        assertNull(readRecord);
    }
}
