package com.incountry.residence.sdk.tools.keyaccessor.key;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecretsData {
    private static final Logger LOG = LogManager.getLogger(SecretsData.class);

    private static final String MSG_ERR_VERSION = "Current version must be >= 0";
    private static final String MSG_ERR_EMPTY_SECRETS = "Secrets in SecretData are null";
    private static final String MSG_ERR_UNIQUE_VERSIONS = "Not unique versions of SecretKeys: ";
    private static final String MSG_ERR_CURRENT_VERSION = "There is no any SecretKey with current version ";

    private List<SecretKey> secrets;
    private int currentVersion;

    /**
     * creates a container with secrets
     *
     * @param secrets        non-empty list of secrets. One of the secrets must have same version as currentVersion in SecretsData
     * @param currentVersion Should be a non-negative integer
     * @throws StorageClientException when parameter validation fails
     */
    public SecretsData(List<SecretKey> secrets, int currentVersion) throws StorageClientException {
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

    public static void validate(List<SecretKey> secrets, int currentVersion) throws StorageClientException {
        if (secrets == null || secrets.isEmpty()) {
            LOG.error(MSG_ERR_EMPTY_SECRETS);
            throw new StorageClientException(MSG_ERR_EMPTY_SECRETS);
        }
        if (currentVersion < 0) {
            LOG.error(MSG_ERR_VERSION);
            throw new StorageClientException(MSG_ERR_VERSION);
        }
        List<Integer> errorList = new ArrayList<>();
        Set<Integer> versionSet = new HashSet<>();
        for (SecretKey one : secrets) {
            SecretKey.validateSecretKey(one.getSecret(), one.getVersion(), one.isForCustomEncryption());
            if (versionSet.contains(one.getVersion())) {
                errorList.add(one.getVersion());
            } else {
                versionSet.add(one.getVersion());
            }
            if (!errorList.isEmpty()) {
                String message = MSG_ERR_UNIQUE_VERSIONS + errorList.toString();
                LOG.error(message);
                throw new StorageClientException(message);
            }
        }
        if (!versionSet.contains(currentVersion)) {
            String message = MSG_ERR_CURRENT_VERSION + currentVersion;
            LOG.error(message);
            throw new StorageClientException(message);
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
