package com.incountry.residence.sdk.tools.transfer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;

public class TransferRecord extends Record {

    private static final Logger LOG = LogManager.getLogger(TransferRecord.class);

    private static final String P_PAYLOAD = "payload";
    private static final String P_META = "meta";
    private static final String MSG_ERR_RESPONSE = "Response error";

    private Integer version;

    public TransferRecord(Record record, CryptoManager cryptoManager, String bodyJsonString) throws StorageClientException, StorageCryptoException {
        setKey(cryptoManager.createKeyHash(record.getKey()));
        setKey2(cryptoManager.createKeyHash(record.getKey2()));
        setKey3(cryptoManager.createKeyHash(record.getKey3()));
        setProfileKey(cryptoManager.createKeyHash(record.getProfileKey()));
        setRangeKey(record.getRangeKey());

        Map.Entry<String, Integer> encBodyAndVersion = cryptoManager.encrypt(bodyJsonString);
        setBody(encBodyAndVersion.getKey());
        version = (encBodyAndVersion.getValue() != null ? encBodyAndVersion.getValue() : 0);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void validate() throws StorageServerException {
        StringBuilder builder = null;
        if (getKey() == null || getKey().length() == 0) {
            builder = new StringBuilder("Null required record fields: key");
        }
        if (getBody() == null || getBody().length() == 0) {
            builder = (builder == null ? new StringBuilder("Null required record fields: body") : builder.append(", body"));
        }
        if (builder != null) {
            String message = builder.toString();
            LOG.error(message);
            throw new StorageServerException(message);
        }
    }

    /**
     * immutable get Record
     *
     * @return return immutalbe Record
     */
    private Record toRecord() {
        Record rec = new Record();
        rec.setKey(getKey());
        rec.setKey2(getKey2());
        rec.setKey3(getKey3());
        rec.setBody(getBody());
        rec.setRangeKey(getRangeKey());
        rec.setProfileKey(getProfileKey());
        return rec;
    }

    public void decryptAllFromBody(Gson gson) {
        JsonObject bodyObj = gson.fromJson(getBody(), JsonObject.class);
        JsonElement innerBodyJson = bodyObj.get(P_PAYLOAD);
        setBody(innerBodyJson != null ? innerBodyJson.getAsString() : null);
        Record recordFromMeta = gson.fromJson(bodyObj.get(P_META), Record.class);
        setKey(recordFromMeta.getKey());
        setKey2(recordFromMeta.getKey2());
        setKey3(recordFromMeta.getKey3());
        setProfileKey(recordFromMeta.getProfileKey());
    }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            if (!super.equals(object)) {
                return false;
            }
            TransferRecord that = (TransferRecord) object;
            return Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), version);
        }

    public Record decrypt(CryptoManager cryptoManager, Gson gson) throws StorageClientException, StorageCryptoException, StorageServerException {
        try {
            if (cryptoManager != null && getBody() != null) {
                setBody(cryptoManager.decrypt(getBody(), version));
                decryptAllFromBody(gson);
            }
        } catch (JsonSyntaxException ex) {
            throw new StorageServerException(MSG_ERR_RESPONSE, ex);
        }
        return toRecord();
    }
}
