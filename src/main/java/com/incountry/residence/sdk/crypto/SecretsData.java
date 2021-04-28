package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SecretsData {
    private static final ValidationHelper HELPER = new ValidationHelper(LogManager.getLogger(SecretsData.class));

    private static final String MSG_ERR_EMPTY_SECRETS = "Secrets in SecretData are null";
    private static final String MSG_ERR_UNIQUE_VERSIONS = "Secret versions must be unique. Got duplicates for: %s";
    private static final String MSG_ERR_CURRENT_VERSION = "There is no current secret at the secrets list";
    private static final String MSG_ERR_NO_SECRET = "Secret not found for 'version'=%d";

    private final List<Secret> secrets;
    private final Secret currentSecret;

    /**
     * creates a container with secrets
     *
     * @param secrets       non-empty list of secrets. One of the secrets should be the second parameter {@link #currentSecret}
     * @param currentSecret Should be a non-negative integer
     * @throws StorageClientException when parameter validation fails
     */
    public SecretsData(List<Secret> secrets, Secret currentSecret) throws StorageClientException {
        validateSecretsData(secrets, currentSecret);
        this.currentSecret = currentSecret;
        this.secrets = secrets;
    }

    public List<Secret> getSecrets() {
        return secrets;
    }

    public Secret getCurrentSecret() {
        return currentSecret;
    }

    @SuppressWarnings("java:S2259")
    private static void validateSecretsData(List<Secret> secrets, Secret currentSecret) throws StorageClientException {
        boolean emptySecrets = secrets == null || secrets.isEmpty();
        HELPER.check(StorageClientException.class, emptySecrets, MSG_ERR_EMPTY_SECRETS);
        boolean isInvalidCurrentSecret = currentSecret == null || !secrets.contains(currentSecret);
        HELPER.check(StorageClientException.class, isInvalidCurrentSecret, MSG_ERR_CURRENT_VERSION);

        List<Integer> errorList = new ArrayList<>();
        Set<Integer> versionSet = new HashSet<>();
        for (Secret secret : secrets) {
            if (versionSet.contains(secret.getVersion())) {
                errorList.add(secret.getVersion());
            } else {
                versionSet.add(secret.getVersion());
            }
        }
        boolean duplicateVersions = !errorList.isEmpty();
        HELPER.check(StorageClientException.class, duplicateVersions, MSG_ERR_UNIQUE_VERSIONS, errorList);
    }


    @SuppressWarnings("java:S3655")
    public Secret getSecret(Integer version) throws StorageCryptoException {
        if (version == null) {
            return currentSecret;
        }
        Optional<Secret> secretOpt = secrets.stream().filter(one -> one.getVersion() == version).findFirst();
        HELPER.check(StorageCryptoException.class, !secretOpt.isPresent(), MSG_ERR_NO_SECRET, version);
        return secretOpt.get();
    }

    @Override
    public String toString() {
        return "SecretsData{" +
                "secrets=" + secrets +
                ", currentSecret=" + currentSecret +
                '}';
    }
}
