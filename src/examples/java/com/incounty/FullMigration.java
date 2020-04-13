package com.incounty;

import com.incountry.MigrateResult;
import com.incountry.Storage;
import com.incountry.exceptions.FindOptionsException;
import com.incountry.exceptions.StorageException;
import com.incountry.keyaccessor.SecretKeyAccessor;
import com.incountry.keyaccessor.generator.SecretKeyGenerator;

import java.security.GeneralSecurityException;

public class FullMigration {

    public void startMigration() throws StorageException, GeneralSecurityException, FindOptionsException {
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretKeyGenerator<String>() {
            @Override
            public String generate() {
                return  "{\n" +
                        "    \"currentVersion\": 1,\n" +
                        "    \"secrets\": [\n" +
                        "        {\"secret\": \"password0\", \"version\": 0},\n" +
                        "        {\"secret\": \"password1\", \"version\": 1},\n" +
                        "    ],\n" +
                        "}";
            }
        });

        Storage storage = new Storage(
                "envId",
                "apiKey",
                secretKeyAccessor

        );

        String country="US";
        boolean migrationComplete = false;
        while (!migrationComplete) {
            MigrateResult migrationResult = storage.migrate(country, 50);
            if (migrationResult.getTotalLeft() == 0) {
                migrationComplete = true;
            }
        }

    }
}
