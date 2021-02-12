package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.tools.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;

public class FullMigrationExample {

    public void startMigration() throws StorageException {
        String secretsDataInJson = "{\n" +
                "    \"currentVersion\": 1,\n" +
                "    \"secrets\": [\n" +
                "        {\"secret\": \"password0\", \"version\": 0},\n" +
                "        {\"secret\": \"password1\", \"version\": 1},\n" +
                "    ],\n" +
                "}";
        SecretsData secretsData = SecretsDataGenerator.fromJson(secretsDataInJson);
        String endPoint = "https://us-mt-01.api.incountry.example";
        String country = "US";
        String envId = "someEnvironmentId";
        String apiKey = "someApiKey";
        Storage storage = StorageImpl.getInstance(envId, apiKey, endPoint, () -> secretsData);
        boolean migrationComplete = false;
        while (!migrationComplete) {
            MigrateResult migrationResult = storage.migrate(country, 50);
            if (migrationResult.getTotalLeft() == 0) {
                migrationComplete = true;
            }
        }
    }
}
