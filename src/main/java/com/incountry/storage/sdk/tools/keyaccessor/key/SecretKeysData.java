package com.incountry.storage.sdk.tools.keyaccessor.key;

import java.util.List;

public class SecretKeysData {
    private List<SecretKey> secrets;
    private int currentVersion;

    public SecretKeysData() {
    }

    public SecretKeysData(List<SecretKey> secrets, int currentVersion) {
        this.secrets = secrets;
        this.currentVersion = currentVersion;
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
