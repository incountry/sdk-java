package com.incountry.residence.sdk.tools;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FilterNumberParam;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.RecordException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class JsonUtils {
    private static final String P_COUNTRY = "country";
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
    private static final String P_COUNT = "count";
    private static final String P_TOTAL = "total";
    private static final String P_DATA = "data";
    private static final String P_CODE = "countries";
    private static final String P_DIRECT = "direct";
    private static final String P_ID = "id";
    private static final String P_NAME = "name";
    private static final String P_OPTIONS = "options";
    private static final String P_FILTER = "filter";
    /*error messages */
    private static final String MSG_RECORD_PARSE_EXCEPTION = "Record Parse Exception";

    private JsonUtils() {
    }

    /**
     * Converts a Record object to JsonObject
     *
     * @param record data record
     * @param crypto object which is using to encrypt data
     * @return JsonObject with Record data
     * @throws StorageCryptoException if encryption failed
     */
    public static JsonObject toJson(Record record, Crypto crypto) throws StorageCryptoException {
        Gson gson = getGson4Records();
        JsonObject jsonObject = (JsonObject) gson.toJsonTree(record);
        jsonObject.remove(P_COUNTRY);
        if (crypto == null) {
            return jsonObject;
        }
        //store keys in new composite body with encription
        jsonObject.remove(P_BODY);
        Map<String, String> mapBodyMeta = new HashMap<>();
        mapBodyMeta.put(P_PAYLOAD, record.getBody());
        mapBodyMeta.put(P_META, jsonObject.toString());
        String packedBody = gson.toJson(mapBodyMeta);
        EncryptedRecord encRec = new EncryptedRecord(record, crypto, packedBody);
        return (JsonObject) gson.toJsonTree(encRec);
    }

    /**
     * Creates JsonObject with FindFilter object properties
     *
     * @param filter FindFilter
     * @param crypto crypto object
     * @return JsonObject with properties corresponding to FindFilter object properties
     */
    public static JsonObject toJson(FindFilter filter, Crypto crypto) {
        JsonObject json = new JsonObject();
        if (filter != null) {
            addToJson(json, P_KEY, filter.getKeyFilter(), crypto);
            addToJson(json, P_KEY_2, filter.getKey2Filter(), crypto);
            addToJson(json, P_KEY_3, filter.getKey3Filter(), crypto);
            addToJson(json, P_PROFILE_KEY, filter.getProfileKeyFilter(), crypto);
            addToJson(json, P_VERSION, filter.getVersionFilter(), crypto);
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
     * @param jsonString json string
     * @param crypto     crypto object
     * @return record objects with data from json
     * @throws StorageCryptoException if decryption failed
     */
    public static Record recordFromString(String jsonString, Crypto crypto) throws StorageCryptoException {
        Gson gson = getGson4Records();
        EncryptedRecord verRec = gson.fromJson(jsonString, EncryptedRecord.class);
        if (verRec.getVersion() == null) {
            verRec.setVersion(0);
        }
        if (crypto != null && verRec.getBody() != null) {
            String[] parts = verRec.getBody().split(":");
            verRec.setBody(crypto.decrypt(verRec.getBody(), verRec.getVersion()));
            if (parts.length != 2) {
                verRec.justDecryptKeys(crypto);
            } else {
                verRec.decryptAllFromBody(gson);
            }
        }
        return verRec.toRecord();
    }

    private static void addToJson(JsonObject json, String paramName, FilterStringParam param, Crypto crypto) {
        if (param != null) {
            if (paramName.equals(P_VERSION)) {
                json.add(paramName, param.isNotCondition() ? addNotCondition(param, null, false) : toJsonInt(param));
            } else {
                json.add(paramName, param.isNotCondition() ? addNotCondition(param, crypto, true) : toJsonArray(param, crypto));
            }
        }
    }

    /**
     * Adds 'not' condition to parameter of FindFilter
     *
     * @param param       parameter to which the not condition should be added
     * @param crypto      crypto object
     * @param isForString the condition must be added for string params
     * @return JsonObject with added 'not' condition
     */
    private static JsonObject addNotCondition(FilterStringParam param, Crypto crypto, boolean isForString) {
        JsonArray arr = isForString ? toJsonArray(param, crypto) : toJsonInt(param);
        JsonObject object = new JsonObject();
        object.add(FindFilterBuilder.OPER_NOT, arr);
        return object;
    }

    public static BatchRecord batchRecordFromString(String responseString, Crypto crypto) {
        List<RecordException> errors = new ArrayList<>();
        Gson gson = getGson4Records();
        JsonObject responseObject = gson.fromJson(responseString, JsonObject.class);
        JsonObject meta = (JsonObject) responseObject.get(P_META);
        int count = meta.get(P_COUNT).getAsInt();
        int limit = meta.get(P_LIMIT).getAsInt();
        int offset = meta.get(P_OFFSET).getAsInt();
        int total = meta.get(P_TOTAL).getAsInt();
        List<Record> records = new ArrayList<>();
        if (count != 0) {
            JsonArray data = responseObject.getAsJsonArray(P_DATA);
            for (JsonElement item : data) {
                try {
                    records.add(JsonUtils.recordFromString(item.toString(), crypto));
                } catch (Exception e) {
                    errors.add(new RecordException(MSG_RECORD_PARSE_EXCEPTION, item.toString(), e));
                }
            }
        }
        return new BatchRecord(records, count, limit, offset, total, errors);
    }

    private static JsonArray valueJSON(FilterNumberParam range) {
        if (range.getValues() == null) {
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

    private static List<String> hashValue(FilterStringParam param, Crypto crypto) {
        return param.getValue().stream().map(crypto::createKeyHash).collect(Collectors.toList());
    }

    public static JsonArray toJsonInt(FilterStringParam param) {
        if (param.getValue() == null) {
            return null;
        }
        JsonArray array = new JsonArray();
        param.getValue().stream().map(Integer::parseInt).forEach(array::add);
        return array;
    }

    private static Gson getGson4Records() {
        return new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    public static void getCountryEntryPoint(String content, BiConsumer<String, String> lambda) {
        Gson gson = new GsonBuilder().create();
        JsonObject contentJson = gson.fromJson(content, JsonObject.class);
        contentJson.getAsJsonArray(P_CODE).forEach(item -> {
            if (((JsonObject) item).get(P_DIRECT).getAsBoolean()) {
                String countryCode = ((JsonObject) item).get(P_ID).getAsString().toLowerCase();
                String countryName = ((JsonObject) item).get(P_NAME).getAsString();
                lambda.accept(countryCode, countryName);
            }
        });
    }

    public static String toJsonString(List<Record> records, String country, Crypto crypto, BiConsumer<String, String> lambda)
            throws StorageCryptoException {
        JsonArray array = new JsonArray();
        for (Record record : records) {
            lambda.accept(country, record.getKey());
            array.add(toJson(record, crypto));
        }
        JsonObject obj = new JsonObject();
        obj.add("records", array);
        return obj.toString();
    }

    /**
     * Put record into JSON format
     *
     * @param record data for JSON
     * @param crypto object which is using to encrypt data
     * @return String with JSON
     * @throws StorageCryptoException when there are problems with encryption
     */
    public static String toJsonString(Record record, Crypto crypto) throws StorageCryptoException {
        return toJson(record, crypto).toString();
    }

    public static String toJsonString(FindFilter filter, Crypto crypto) {
        JsonObject object = new JsonObject();
        object.add(P_FILTER, JsonUtils.toJson(filter, crypto));
        object.add(P_OPTIONS, JsonUtils.findOptionstoJson(filter.getLimit(), filter.getOffset()));
        return object.toString();
    }

    public static JsonArray toJsonArray(FilterStringParam param, Crypto crypto) {
        if (param.getValue() == null) {
            return null;
        }
        JsonArray array = new JsonArray();
        List<String> values = (crypto != null ? hashValue(param, crypto) : param.getValue());
        values.forEach(array::add);
        return array;
    }

    /**
     * inner class for cosy encryption and serialization of {@link Record} instances
     */
    private static class EncryptedRecord extends Record {
        private Integer version;

        EncryptedRecord(Record record, Crypto crypto, String bodyJsonString) throws StorageCryptoException {
            setKey(crypto.createKeyHash(record.getKey()));
            setKey2(crypto.createKeyHash(record.getKey2()));
            setKey3(crypto.createKeyHash(record.getKey3()));
            setProfileKey(crypto.createKeyHash(record.getProfileKey()));
            setRangeKey(record.getRangeKey());

            Pair<String, Integer> encBodyAndVersion = crypto.encrypt(bodyJsonString);
            setBody(encBodyAndVersion.getValue0());
            setVersion(encBodyAndVersion.getValue1() != null ? encBodyAndVersion.getValue1() : 0);
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        /**
         * immutable get Record
         *
         * @return return immutalbe Record
         */
        public Record toRecord() {
            Record rec = new Record();
            rec.setKey(getKey());
            rec.setKey2(getKey2());
            rec.setKey3(getKey3());
            rec.setBody(getBody());
            rec.setRangeKey(getRangeKey());
            rec.setProfileKey(getProfileKey());
            rec.setCountry(getCountry());
            return rec;
        }

        public void justDecryptKeys(Crypto crypto) throws StorageCryptoException {
            setKey(crypto.decrypt(getKey(), version));
            setKey2(crypto.decrypt(getKey2(), version));
            setKey3(crypto.decrypt(getKey3(), version));
            setProfileKey(crypto.decrypt(getProfileKey(), version));
        }

        public void decryptAllFromBody(Gson gson) {
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
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            if (!super.equals(obj)) {
                return false;
            }
            EncryptedRecord that = (EncryptedRecord) obj;
            return Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), version);
        }
    }
}
