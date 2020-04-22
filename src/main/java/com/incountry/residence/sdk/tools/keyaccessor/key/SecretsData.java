package com.incountry.residence.sdk.tools.keyaccessor.key;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class SecretsData {
    private static final Logger LOG = LogManager.getLogger(SecretsData.class);

    private static final String MSG_ERR_VERSION = "Current version must be >= 0";
    private static final String MSG_ERR_EMPTY_SECRETS = "Secrets in SecretData are null";

    private List<SecretKey> secrets;
    private int currentVersion;

    public SecretsData(List<SecretKey> secrets, int currentVersion) {
        validate(secrets, currentVersion);
        this.currentVersion = currentVersion;
        this.secrets = secrets;
    }

    public List<SecretKey> getSecrets() {
        return secrets;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public static void validate(List<SecretKey> secrets, int currentVersion) {
        if (secrets == null || secrets.isEmpty()) {
            LOG.error(MSG_ERR_EMPTY_SECRETS);
            throw new IllegalArgumentException(MSG_ERR_EMPTY_SECRETS);
        }
        if (currentVersion < 0) {
            LOG.error(MSG_ERR_VERSION);
            throw new IllegalArgumentException(MSG_ERR_VERSION);
        }
        secrets.forEach(one ->
                SecretKey.validateSecretKey(one.getSecret(), one.getVersion(), one.getIsKey())
        );
    }

    @Override
    public String toString() {
        return "SecretsData{" +
                "secrets=" + secrets +
                ", currentVersion=" + currentVersion +
                '}';
    }
}
