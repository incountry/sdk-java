package com.incountry.residence.sdk.tools.keyaccessor.utils;

import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class SecretKeyUtils {

    private static final Logger LOG = LogManager.getLogger(SecretKeyUtils.class);

    public static final int DEFAULT_VERSION = 0;

    private SecretKeyUtils() {
    }

    /**
     * Convert string to SecretKeyData object
     *
     * @param secretKeyString simple string or json
     * @return SecretKeyData object which contain secret keys and there versions
     */
    public static SecretKeysData getSecretKeyDataFromString(String secretKeyString) {
        SecretKeysData data = JsonUtils.getSecretKeysDataFromJson(secretKeyString);
        if (data != null) {
            validateSecretKeysData(data);
            return data;
        }
        SecretKey secretKey = new SecretKey(secretKeyString, DEFAULT_VERSION, false);
        List<SecretKey> secretKeys = new ArrayList<>();
        secretKeys.add(secretKey);
        SecretKeysData secretKeysData = new SecretKeysData();
        secretKeysData.setSecrets(secretKeys);
        secretKeysData.setCurrentVersion(DEFAULT_VERSION);
        return secretKeysData;
    }

    public static void validateSecretKeysData(SecretKeysData data) {
        data.validateVersion(data.getCurrentVersion());
        if (data.getSecrets() == null || data.getSecrets().isEmpty()) {
            String message = "Secrets in SecretKeysData are null";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        data.getSecrets().forEach(one ->
                SecretKey.validateSecretKey(one.getSecret(), one.getVersion(), one.getIsKey())
        );
    }
}
