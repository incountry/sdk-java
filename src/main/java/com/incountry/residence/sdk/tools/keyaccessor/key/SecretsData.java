package com.incountry.residence.sdk.tools.keyaccessor.key;

import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecretsData {

    private static final Logger LOG = LogManager.getLogger(SecretsData.class);

//    private static final String MSG_ERR_VERSION = "Current version must be >= 0";
    private static final String MSG_ERR_EMPTY_SECRETS = "Secrets in SecretData are null";
    private static final String MSG_ERR_UNIQUE_VERSIONS = "SecretKey versions must be unique. Got duplicates for: %s";
    private static final String MSG_ERR_CURRENT_SECRET = "There is no SecretKey version that matches current version %s";
    private static final String MSG_ERR_VERSION = "Secret not found for 'version'=%s";


    private List<Secret> secrets;
    private Secret currentSecret;


    public SecretsData(List<Secret> secrets, Secret currentSecret) throws StorageClientException {
        validateSecretsData(secrets, currentSecret);
        this.currentSecret = currentSecret;
        this.secrets = secrets;
    }


    private static void validateSecretsData(List<Secret> secrets, Secret currentSecret) throws StorageClientException {
        if (secrets == null || secrets.isEmpty()) {
            LOG.error(MSG_ERR_EMPTY_SECRETS);
            throw new StorageClientException(MSG_ERR_EMPTY_SECRETS);
        }

        Set versionSet = new HashSet<Integer>();
        for (Secret secret : secrets) {
            if (versionSet.contains(secret.getVersion())) {
                LOG.error(String.format(MSG_ERR_UNIQUE_VERSIONS, secret.getVersion()));
                throw new StorageClientException(String.format(MSG_ERR_UNIQUE_VERSIONS, secret.getVersion()));
            }
            versionSet.add(secret.getVersion());
        }
        if (!secrets.contains(currentSecret)) {
            LOG.error(String.format(MSG_ERR_CURRENT_SECRET, currentSecret.getVersion()));
            throw new StorageClientException(String.format(MSG_ERR_CURRENT_SECRET, currentSecret.getVersion()));
        }
    }

    public Secret getSecret(Integer version) throws StorageClientException {
        if (version == null) {
            return currentSecret;
        }
        Secret secret = secrets.stream()
                .filter(secretKey -> secretKey.getVersion() == version)
                .findAny()
                .orElse(null);
        if (secret == null) {
            LOG.error(String.format(MSG_ERR_VERSION, version));
            throw new StorageClientException(String.format(MSG_ERR_VERSION, version));
        }
        return secret;
    }

    public List<Secret> getSecrets() {
        return secrets;
    }

    public Secret getCurrentSecret() {
        return currentSecret;
    }

    // TODO check how will it look like in tests
    @Override
    public String toString() {
        return "SecretsData{" +
                "secrets=" + secrets +
                ", currentSecret=" + currentSecret +
                '}';
    }
}
