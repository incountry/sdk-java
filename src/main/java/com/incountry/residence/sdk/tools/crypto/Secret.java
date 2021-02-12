package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public abstract class Secret {

    private static final Logger LOG = LogManager.getLogger(Secret.class);

    private static final String MSG_ERR_VERSION = "Version must be >= 0";
    private static final String MSG_ERR_NULL_SECRET = "Secret can't be null";

    private int version;
    private byte[] secretBytes;

    public Secret(int version, byte[] secretBytes) throws StorageClientException {
        validateAbstractSecret(version, secretBytes);
        this.version = version;
        this.secretBytes = secretBytes;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getSecretBytes() {
        return secretBytes;
    }

    private void validateAbstractSecret(int version, byte[] secret) throws StorageClientException {
        if (version < 0) {
            LOG.error(MSG_ERR_VERSION);
            throw new StorageClientException(MSG_ERR_VERSION);
        }
        if (secret == null || secret.length == 0) {
            LOG.error(MSG_ERR_NULL_SECRET);
            throw new StorageClientException(MSG_ERR_NULL_SECRET);
        }
    }

    public String toString(String typeName) {
        return typeName + "{" +
                "secret=HASH[" + Arrays.hashCode(secretBytes) + ']' +
                ", version=" + version +
                '}';
    }
}
