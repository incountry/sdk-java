package com.incountry.residence.sdk.tools.keyaccessor.key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SecretKeysData {
    private static final Logger LOG = LoggerFactory.getLogger(SecretKeysData.class);

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
        if (currentVersion < 0) {
            String message = "Current version must be >= 0";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        this.currentVersion = currentVersion;
    }

    @Override
    public String toString() {
        return "SecretKeysData{" +
                "secrets=" + secrets +
                ", currentVersion=" + currentVersion +
                '}';
    }
}
