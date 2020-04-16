package com.incountry.residence.sdk.tools.keyaccessor.key;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class SecretsData {
    private static final Logger LOG = LogManager.getLogger(SecretsData.class);

    private static final String MSG_ERR_VERSION = "Current version must be >= 0";

    private List<SecretKey> secrets;
    private int currentVersion;

    public SecretsData() {
    }

    public SecretsData(List<SecretKey> secrets, int currentVersion) {
        setCurrentVersion(currentVersion);
        this.secrets = secrets;
    }

    public List<SecretKey> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<SecretKey> secrets) {
        this.secrets = secrets;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        validateVersion(currentVersion);
        this.currentVersion = currentVersion;
    }

    public void validateVersion(int currentVersion) {
        if (currentVersion < 0) {
            LOG.error(MSG_ERR_VERSION);
            throw new IllegalArgumentException(MSG_ERR_VERSION);
        }
    }

    @Override
    public String toString() {
        return "SecretsData{" +
                "secrets=" + secrets +
                ", currentVersion=" + currentVersion +
                '}';
    }
}
