package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsDataGenerator;

public class FullMigrationExample {

    private static final String ENVIRONMENT_ID = "<environment_id>";
    private static final String CLIENT_ID = "<client_id>";
    private static final String CLIENT_SECRET = "<client_secret>";

    public void startMigration() throws StorageException {
        String secretsDataInJson = "{\n" +
                "    \"currentVersion\": 1,\n" +
                "    \"secrets\": [\n" +
                "        {\"secret\": \"password0\", \"version\": 0},\n" +
                "        {\"secret\": \"password1\", \"version\": 1},\n" +
                "    ],\n" +
                "}";
        SecretsData secretsData = SecretsDataGenerator.fromJson(secretsDataInJson);
        StorageConfig config = new StorageConfig()
                .setEnvId(ENVIRONMENT_ID)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setSecretKeyAccessor(() -> secretsData);
        Storage storage = StorageImpl.getInstance(config);
        String country = "US";
        boolean migrationComplete = false;
        while (!migrationComplete) {
            MigrateResult migrationResult = storage.migrate(country, 50);
            if (migrationResult.getTotalLeft() == 0) {
                migrationComplete = true;
            }
        }
    }
}
