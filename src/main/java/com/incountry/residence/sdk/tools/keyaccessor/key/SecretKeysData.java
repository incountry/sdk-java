package com.incountry.residence.sdk.tools.keyaccessor.key;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class SecretKeysData {
    private static final Logger LOG = LogManager.getLogger(SecretKeysData.class);

    private List<SecretKey> secrets;
    private int currentVersion;

    public SecretKeysData() {
    }

    public SecretKeysData(List<SecretKey> secrets, int currentVersion) {
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
            String message = "Current version must be >= 0";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public String toString() {
        return "SecretKeysData{" +
                "secrets=" + secrets +
                ", currentVersion=" + currentVersion +
                '}';
    }
}
