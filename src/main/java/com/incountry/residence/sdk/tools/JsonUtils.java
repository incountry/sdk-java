package com.incountry.residence.sdk.tools;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FilterNumberParam;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.dao.PoP;
import com.incountry.residence.sdk.tools.exceptions.RecordException;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonUtils {

    private static final Logger LOG = LogManager.getLogger(JsonUtils.class);

    private static final String P_BODY = "body";
    private static final String P_KEY = "key";
    private static final String P_KEY_2 = "key2";
    private static final String P_KEY_3 = "key3";
    private static final String P_PROFILE_KEY = "profile_key";
    private static final String P_RANGE_KEY = "range_key";
    private static final String P_PAYLOAD = "payload";
    private static final String P_META = "meta";
    private static final String P_VERSION = "version";
    private static final String P_LIMIT = "limit";
    private static final String P_OFFSET = "offset";
    private static final String P_OPTIONS = "options";
    private static final String P_FILTER = "filter";
    /*error messages */
    private static final String MSG_RECORD_PARSE_EXCEPTION = "Record Parse Exception";
    private static final String MSG_ERR_NULL_META = "Response error: Meta is null";
    private static final String MSG_ERR_NEGATIVE_META = "Response error: negative values in batch metadata";
    private static final String MSG_ERR_INCORRECT_COUNT = "Response error: count in batch metadata differs from data size";
    private static final String MSG_ERR_INCORRECT_TOTAL = "Response error: incorrect total in batch metadata, less then recieved";
    private static final String MSG_ERR_NULL_POPLIST = "Response error: country list is empty";
    private static final String MSG_ERR_NULL_POPNAME = "Response error: country name is empty";
    private static final String MSG_ERR_NULL_POPID = "Response error: country id is empty";
    private static final String MSG_ERR_RESPONSE = "Response error";
    private static final String MSG_ERR_INCORRECT_SECRETS = "Incorrect JSON with SecretsData";

    private JsonUtils() {
    }

    /**
     * Converts a Record object to JsonObject
     *
     * @param record        data record
     * @param cryptoManager object which is using to encrypt data
     * @return JsonObject with Record data
     * @throws StorageClientException if validation of parameters failed
     * @throws StorageCryptoException if encryption failed
     */
    public static JsonObject toJson(Record record, CryptoManager cryptoManager) throws StorageClientException, StorageCryptoException {
        Gson gson = getGson4Records();
        JsonObject jsonObject = (JsonObject) gson.toJsonTree(record);
        if (cryptoManager == null) {
            return jsonObject;
        }
        //store keys in new composite body with encription
        jsonObject.remove(P_BODY);
        Map<String, String> mapBodyMeta = new HashMap<>();
        mapBodyMeta.put(P_PAYLOAD, record.getBody());
        mapBodyMeta.put(P_META, jsonObject.toString());
        String packedBody = gson.toJson(mapBodyMeta);
        TransferRecord encRec = new TransferRecord(record, cryptoManager, packedBody);
        return (JsonObject) gson.toJsonTree(encRec);
    }

    /**
     * Creates JsonObject with FindFilter object properties
     *
     * @param filter        FindFilter
     * @param cryptoManager crypto object
     * @return JsonObject with properties corresponding to FindFilter object properties
     */
    public static JsonObject toJson(FindFilter filter, CryptoManager cryptoManager) {
        JsonObject json = new JsonObject();
        if (filter != null) {
            addToJson(json, P_KEY, filter.getKeyFilter(), cryptoManager);
            addToJson(json, P_KEY_2, filter.getKey2Filter(), cryptoManager);
            addToJson(json, P_KEY_3, filter.getKey3Filter(), cryptoManager);
            addToJson(json, P_PROFILE_KEY, filter.getProfileKeyFilter(), cryptoManager);
            addToJson(json, P_VERSION, filter.getVersionFilter(), cryptoManager);
            FilterNumberParam range = filter.getRangeKeyFilter();
            if (range != null) {
                json.add(P_RANGE_KEY, range.isConditional() ? conditionJSON(range) : valueJSON(range));
            }
        }
        return json;
    }

    /**
     * Create record object from json string
     *
     * @param jsonString    json string
     * @param cryptoManager crypto object
     * @return record objects with data from json
     * @throws StorageClientException if validation of parameters failed
     * @throws StorageCryptoException if decryption failed
     * @throws StorageServerException if server connection failed or server response error
     */
    public static Record recordFromString(String jsonString, CryptoManager cryptoManager) throws StorageClientException, StorageCryptoException, StorageServerException {
        Gson gson = getGson4Records();
        TransferRecord tempRecord;
        try {
            tempRecord = gson.fromJson(jsonString, TransferRecord.class);
        } catch (JsonSyntaxException ex) {
            throw new StorageServerException(MSG_ERR_RESPONSE, ex);
        }
        tempRecord.validate();
        if (tempRecord.version == null) {
            tempRecord.version = 0;
        }
        return tempRecord.decrypt(cryptoManager);
    }

    private static void addToJson(JsonObject json, String paramName, FilterStringParam param, CryptoManager cryptoManager) {
        if (param != null) {
            if (paramName.equals(P_VERSION)) {
                json.add(paramName, param.isNotCondition() ? addNotCondition(param, null, false) : toJsonInt(param));
            } else {
                json.add(paramName, param.isNotCondition() ? addNotCondition(param, cryptoManager, true) : toJsonArray(param, cryptoManager));
            }
        }
    }

    /**
     * Adds 'not' condition to parameter of FindFilter
     *
     * @param param         parameter to which the not condition should be added
     * @param cryptoManager crypto object
     * @param isForString   the condition must be added for string params
     * @return JsonObject with added 'not' condition
     */
    private static JsonObject addNotCondition(FilterStringParam param, CryptoManager cryptoManager, boolean isForString) {
        JsonArray arr = isForString ? toJsonArray(param, cryptoManager) : toJsonInt(param);
        JsonObject object = new JsonObject();
        object.add(FindFilterBuilder.OPER_NOT, arr);
        return object;
    }

    public static BatchRecord batchRecordFromString(String responseString, CryptoManager cryptoManager) throws StorageServerException {
        List<RecordException> errors = new ArrayList<>();
        Gson gson = getGson4Records();
        TransferBatch transferBatch;
        try {
            transferBatch = gson.fromJson(responseString, TransferBatch.class);
        } catch (JsonSyntaxException ex) {
            throw new StorageServerException(MSG_ERR_RESPONSE, ex);
        }
        transferBatch.validate();
        List<Record> records = new ArrayList<>();
        if (transferBatch.meta.getCount() != 0) {
            for (TransferRecord tempRecord : transferBatch.data) {
                try {
                    tempRecord.validate();
                    if (tempRecord.version == null) {
                        tempRecord.version = 0;
                    }
                    records.add(tempRecord.decrypt(cryptoManager));
                } catch (Exception e) {
                    errors.add(new RecordException(MSG_RECORD_PARSE_EXCEPTION, gson.toJson(tempRecord), e));
                }
            }
        }
        return new BatchRecord(records, transferBatch.meta.getCount(), transferBatch.meta.getLimit(),
                transferBatch.meta.getOffset(), transferBatch.meta.getTotal(), errors);
    }

    private static JsonArray valueJSON(FilterNumberParam range) {
        if (range.getValues() == null || range.getValues().length == 0) {
            return null;
        }
        JsonArray array = new JsonArray();
        for (int i : range.getValues()) {
            array.add(i);
        }
        return array;
    }

    private static JsonObject conditionJSON(FilterNumberParam range) {
        JsonObject object = new JsonObject();
        object.addProperty(range.getOperator1(), range.getValues()[0]);
        if (range.isRange()) {
            object.addProperty(range.getOperator2(), range.getValues()[1]);
        }
        return object;
    }

    private static JsonObject findOptionstoJson(int limit, int offset) {
        JsonObject object = new JsonObject();
        object.addProperty(P_LIMIT, limit);
        object.addProperty(P_OFFSET, offset);
        return object;
    }

    private static List<String> hashValue(FilterStringParam param, CryptoManager cryptoManager) {
        return param.getValues().stream().map(cryptoManager::createKeyHash).collect(Collectors.toList());
    }

    public static JsonArray toJsonInt(FilterStringParam param) {
        if (param == null || param.getValues() == null) {
            return null;
        }
        JsonArray array = new JsonArray();
        param.getValues().stream().map(Integer::parseInt).forEach(array::add);
        return array;
    }

    private static Gson getGson4Records() {
        return new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    public static String toJsonString(List<Record> records, CryptoManager cryptoManager)
            throws StorageClientException, StorageCryptoException {
        JsonArray array = new JsonArray();
        for (Record record : records) {
            array.add(toJson(record, cryptoManager));
        }
        JsonObject obj = new JsonObject();
        obj.add("records", array);
        return obj.toString();
    }

    /**
     * Put record into JSON format
     *
     * @param record        data for JSON
     * @param cryptoManager object which is using to encrypt data
     * @return String with JSON
     * @throws StorageClientException if validation of parameters failed
     * @throws StorageCryptoException when there are problems with encryption
     */
    public static String toJsonString(Record record, CryptoManager cryptoManager) throws StorageClientException, StorageCryptoException {
        return toJson(record, cryptoManager).toString();
    }

    public static String toJsonString(FindFilter filter, CryptoManager cryptoManager) {
        JsonObject object = new JsonObject();
        object.add(P_FILTER, JsonUtils.toJson(filter, cryptoManager));
        object.add(P_OPTIONS, JsonUtils.findOptionstoJson(filter.getLimit(), filter.getOffset()));
        return object.toString();
    }

    public static JsonArray toJsonArray(FilterStringParam param, CryptoManager cryptoManager) {
        if (param == null || param.getValues() == null) {
            return null;
        }
        JsonArray array = new JsonArray();
        List<String> values = (cryptoManager != null ? hashValue(param, cryptoManager) : param.getValues());
        values.forEach(array::add);
        return array;
    }

    public static SecretsData getSecretsDataFromJson(String string) throws StorageClientException {
        SecretsData result;
        try {
            result = new Gson().fromJson(string, SecretsData.class);
        } catch (JsonSyntaxException e) {
            throw new StorageClientException(MSG_ERR_INCORRECT_SECRETS, e);
        }
        return result;
    }

    public static Map<String, PoP> getCountries(String response, String uriStart, String uriEnd) throws StorageServerException {
        TransferPopList popList;
        try {
            popList = new Gson().fromJson(response, TransferPopList.class);
        } catch (JsonSyntaxException ex) {
            throw new StorageServerException(MSG_ERR_RESPONSE, ex);
        }
        Map<String, PoP> result = new HashMap<>();
        TransferPopList.validatePopList(popList);
        for (TransferPop one : popList.countries) {
            if (one.direct) {
                result.put(one.getId(), new PoP(uriStart + one.getId() + uriEnd, one.name));
            }
        }
        return result;
    }

    /**
     * inner class for cosy encryption and serialization of {@link Record} instances
     */
    private static class TransferRecord extends Record {
        Integer version;

        TransferRecord(Record record, CryptoManager cryptoManager, String bodyJsonString) throws StorageClientException, StorageCryptoException {
            setKey(cryptoManager.createKeyHash(record.getKey()));
            setKey2(cryptoManager.createKeyHash(record.getKey2()));
            setKey3(cryptoManager.createKeyHash(record.getKey3()));
            setProfileKey(cryptoManager.createKeyHash(record.getProfileKey()));
            setRangeKey(record.getRangeKey());

            Map.Entry<String, Integer> encBodyAndVersion = cryptoManager.encrypt(bodyJsonString);
            setBody(encBodyAndVersion.getKey());
            version = (encBodyAndVersion.getValue() != null ? encBodyAndVersion.getValue() : 0);
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

        public void justDecryptKeys(CryptoManager cryptoManager) throws StorageClientException, StorageCryptoException {
            setKey(cryptoManager.decrypt(getKey(), version));
            setKey2(cryptoManager.decrypt(getKey2(), version));
            setKey3(cryptoManager.decrypt(getKey3(), version));
            setProfileKey(cryptoManager.decrypt(getProfileKey(), version));
        }

        public void decryptAllFromBody() {
            Gson gson = getGson4Records();
            JsonObject bodyObj = gson.fromJson(getBody(), JsonObject.class);
            setBody(getPropertyFromJson(bodyObj, P_PAYLOAD));
            String meta = getPropertyFromJson(bodyObj, P_META);
            Record recordFromMeta = gson.fromJson(meta, Record.class);
            setKey(recordFromMeta.getKey());
            setKey2(recordFromMeta.getKey2());
            setKey3(recordFromMeta.getKey3());
            setProfileKey(recordFromMeta.getProfileKey());
        }

        /**
         * Get property value from json
         *
         * @param jsonObject json object
         * @param property   property name
         * @return property value
         */
        private String getPropertyFromJson(JsonObject jsonObject, String property) {
            if (!jsonObject.has(property)) {
                return null;
            }
            return jsonObject.get(property).isJsonNull() ? null : jsonObject.get(property).getAsString();
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

        public Record decrypt(CryptoManager cryptoManager) throws StorageClientException, StorageCryptoException, StorageServerException {
            try {
                if (cryptoManager != null && getBody() != null) {
                    String[] parts = getBody().split(":", 2);
                    setBody(cryptoManager.decrypt(getBody(), version));
                    if (parts.length != 2) {
                        justDecryptKeys(cryptoManager);
                    } else {
                        decryptAllFromBody();
                    }
                }
            } catch (JsonSyntaxException ex) {
                throw new StorageServerException(MSG_ERR_RESPONSE, ex);
            }
            return toRecord();
        }
    }

    /**
     * inner class for cosy serialization of {@link BatchRecord} instances
     */
    private static class TransferBatch {
        BatchRecord meta;
        List<TransferRecord> data;

        public void validate() throws StorageServerException {
            if (meta == null) {
                throw new StorageServerException(MSG_ERR_NULL_META);
            } else if (meta.getCount() < 0 || meta.getLimit() < 0 || meta.getOffset() < 0 || meta.getTotal() < 0) {
                throw new StorageServerException(MSG_ERR_NEGATIVE_META);
            } else if (meta.getCount() > 0 && (data == null || data.isEmpty() || data.size() != meta.getCount())
                    || meta.getCount() == 0 && !data.isEmpty()) {
                throw new StorageServerException(MSG_ERR_INCORRECT_COUNT);
            } else if (meta.getCount() > meta.getTotal()) {
                throw new StorageServerException(MSG_ERR_INCORRECT_TOTAL);
            }
        }
    }

    /**
     * inner class for cosy serialization loading country List
     */
    private static class TransferPop {
        String name;
        String id;
        String status;
        boolean direct;

        @Override
        public String toString() {
            return "TransferPop{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", status='" + status + '\'' +
                    ", direct=" + direct +
                    '}';
        }

        public String getId() {
            return id.toLowerCase();
        }
    }

    private static class TransferPopList {
        List<TransferPop> countries;

        static void validatePopList(TransferPopList one) throws StorageServerException {
            if (one == null || one.countries == null || one.countries.isEmpty()) {
                LOG.error(MSG_ERR_NULL_POPLIST);
                throw new StorageServerException(MSG_ERR_NULL_POPLIST);
            }
            for (TransferPop pop : one.countries) {
                if (pop.name == null || pop.name.isEmpty()) {
                    String message = MSG_ERR_NULL_POPNAME + pop.toString();
                    LOG.error(message);
                    throw new StorageServerException(message);
                }
                if (pop.id == null || pop.id.isEmpty()) {
                    String message = MSG_ERR_NULL_POPID + pop.toString();
                    LOG.error(message);
                    throw new StorageServerException(message);
                }
            }
        }
    }
}
