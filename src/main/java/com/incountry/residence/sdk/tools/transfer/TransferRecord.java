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

    //for backwards compatibility
    private String key;
    private Integer version;
    private boolean isEncrypted;


    public TransferRecord(Record record, CryptoManager cryptoManager, String bodyJsonString) throws StorageClientException, StorageCryptoException {
        super(cryptoManager.createKeyHash(record.getRecordKey()));
        setKey1(cryptoManager.createSearchKeyHash(record.getKey1()));
        setKey2(cryptoManager.createSearchKeyHash(record.getKey2()));
        setKey3(cryptoManager.createSearchKeyHash(record.getKey3()));
        setKey4(cryptoManager.createSearchKeyHash(record.getKey4()));
        setKey5(cryptoManager.createSearchKeyHash(record.getKey5()));
        setKey6(cryptoManager.createSearchKeyHash(record.getKey6()));
        setKey7(cryptoManager.createSearchKeyHash(record.getKey7()));
        setKey8(cryptoManager.createSearchKeyHash(record.getKey8()));
        setKey9(cryptoManager.createSearchKeyHash(record.getKey9()));
        setKey10(cryptoManager.createSearchKeyHash(record.getKey10()));
        setProfileKey(cryptoManager.createKeyHash(record.getProfileKey()));
        setServiceKey1(cryptoManager.createKeyHash(record.getServiceKey1()));
        setServiceKey2(cryptoManager.createKeyHash(record.getServiceKey2()));
        setRangeKey1(record.getRangeKey1());
        setRangeKey2(record.getRangeKey2());
        setRangeKey3(record.getRangeKey3());
        setRangeKey4(record.getRangeKey4());
        setRangeKey5(record.getRangeKey5());
        setRangeKey6(record.getRangeKey6());
        setRangeKey7(record.getRangeKey7());
        setRangeKey8(record.getRangeKey8());
        setRangeKey9(record.getRangeKey9());
        setRangeKey10(record.getRangeKey10());

        Map.Entry<String, Integer> encBodyAndVersion = cryptoManager.encrypt(bodyJsonString);
        setBody(encBodyAndVersion.getKey());
        isEncrypted = !cryptoManager.isUsePTEncryption();
        version = (encBodyAndVersion.getValue() != null ? encBodyAndVersion.getValue() : 0);

        if (record.getPrecommitBody() != null) {
            Map.Entry<String, Integer> encPreCommit = cryptoManager.encrypt(record.getPrecommitBody());
            setPrecommitBody(encPreCommit.getKey());
        }
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public static void validate(TransferRecord record) throws StorageServerException {
        StringBuilder builder = null;
        if (record == null) {
            builder = new StringBuilder("Received record is null");
        } else {
            if (record.getRecordKey() == null || record.getRecordKey().length() == 0) {
                builder = new StringBuilder("Null required record fields: recordKey");
            }
            if (record.getBody() == null || record.getBody().length() == 0) {
                builder = (builder == null ? new StringBuilder("Null required record fields: body") : builder.append(", body"));
            }
        }
        if (builder != null) {
            String message = builder.toString();
            LOG.error(message);
            throw new StorageServerException(message);
        }
    }

    public void decryptAllFromBody(Gson gson) {
        JsonObject bodyObj = gson.fromJson(getBody(), JsonObject.class);
        JsonElement innerBodyJson = bodyObj.get(P_PAYLOAD);
        setBody(innerBodyJson != null ? innerBodyJson.getAsString() : null);
        TransferRecord recordFromMeta = gson.fromJson(bodyObj.get(P_META), TransferRecord.class);
        String recordKey = recordFromMeta.getRecordKey();
        if (recordKey == null && recordFromMeta.key != null) {
            recordKey = recordFromMeta.key;
        }
        setRecordKey(recordKey);
        setKey1(recordFromMeta.getKey1());
        setKey2(recordFromMeta.getKey2());
        setKey3(recordFromMeta.getKey3());
        setKey4(recordFromMeta.getKey4());
        setKey5(recordFromMeta.getKey5());
        setKey6(recordFromMeta.getKey6());
        setKey7(recordFromMeta.getKey7());
        setKey8(recordFromMeta.getKey8());
        setKey9(recordFromMeta.getKey9());
        setKey10(recordFromMeta.getKey10());
        setProfileKey(recordFromMeta.getProfileKey());
        setServiceKey1(recordFromMeta.getServiceKey1());
        setServiceKey2(recordFromMeta.getServiceKey2());
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
            if (cryptoManager != null) {
                if (getBody() != null) {
                    setBody(cryptoManager.decrypt(getBody(), version));
                    decryptAllFromBody(gson);
                }
                if (getPrecommitBody() != null) {
                    setPrecommitBody(cryptoManager.decrypt(getPrecommitBody(), version));
                }
            }
        } catch (JsonSyntaxException ex) {
            throw new StorageServerException(MSG_ERR_RESPONSE, ex);
        }
        return copy();
    }
}
