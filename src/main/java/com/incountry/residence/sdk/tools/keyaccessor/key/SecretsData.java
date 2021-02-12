package com.incountry.residence.sdk.tools.keyaccessor.key;

import com.incountry.residence.sdk.tools.crypto.Secret;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecretsData {

    private static final Logger LOG = LogManager.getLogger(SecretsData.class);

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

        Set<Integer> versionSet = new HashSet<>();
        for (Secret secret : secrets) {
            if (versionSet.contains(secret.getVersion())) {
                String message = String.format(MSG_ERR_UNIQUE_VERSIONS, secret.getVersion());
                LOG.error(message);
                throw new StorageClientException(message);
            }
            versionSet.add(secret.getVersion());
        }
        if (!secrets.contains(currentSecret)) {
            String version = currentSecret != null ? String.valueOf(currentSecret.getVersion()) : "";
            String message = String.format(MSG_ERR_CURRENT_SECRET, version);
            LOG.error(message);
            throw new StorageClientException(message);
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
            String message = String.format(MSG_ERR_VERSION, version);
            LOG.error(message);
            throw new StorageClientException(message);
        }
        return secret;
    }

    public List<Secret> getSecrets() {
        return secrets;
    }

    public Secret getCurrentSecret() {
        return currentSecret;
    }

    @Override
    public String toString() {
        return "SecretsData{" +
                "secrets=" + secrets +
                ", currentSecret=" + currentSecret +
                '}';
    }
}
