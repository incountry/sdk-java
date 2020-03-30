package com.incountry.residence.sdk.tools;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FilterRangeParam;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindOptions;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.RecordException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Converts a Record object to JsonObject
     *
     * @param record  data record
     * @param mCrypto object which is using to encrypt data
     * @return JsonObject with Record data
     * @throws StorageCryptoException if encryption failed
     */
    public static JsonObject toJson(Record record, Crypto mCrypto) throws StorageCryptoException {
        Gson gson = getGson4Records();
        JsonObject jsonObject = (JsonObject) gson.toJsonTree(record);
        jsonObject.remove(P_COUNTRY);
        if (mCrypto == null) {
            return jsonObject;
        }
        //store keys in new composite body with encription
        jsonObject.remove(P_BODY);
        Map<String, String> mapBodyMeta = new HashMap<>();
        mapBodyMeta.put(P_PAYLOAD, record.getBody());
        mapBodyMeta.put(P_META, jsonObject.toString());
        String packedBody = gson.toJson(mapBodyMeta);
        EncryptedRecord encRec = new EncryptedRecord(record, mCrypto, packedBody);
        return (JsonObject) gson.toJsonTree(encRec);
    }

    //todo refactor

    /**
     * Create record object from json string
     *
     * @param jsonString json string
     * @param mCrypto    crypto object
     * @return record objects with data from json
     * @throws StorageCryptoException if decryption failed
     */
    public static Record recordFromString(String jsonString, Crypto mCrypto) throws StorageCryptoException {
        Gson gson = getGson4Records();
        EncryptedRecord verRec = gson.fromJson(jsonString, EncryptedRecord.class);
        if (verRec.getVersion() == null) {
            verRec.setVersion(0);
        }
        if (mCrypto != null && verRec.getBody() != null) {
            String[] parts = verRec.getBody().split(":");
            verRec.setBody(mCrypto.decrypt(verRec.getBody(), verRec.getVersion()));
            if (parts.length != 2) {
                verRec.justDecryptKeys(mCrypto);
            } else {
                verRec.decryptAllFromBody(gson);
            }
        }
        return verRec.toRecord();
    }

    /**
     * @param mCrypto object which is using to encrypt data
     * @return Json string with Record data
     * @throws StorageCryptoException if encryption failed
     */
    public static String toJsonString(Record record, Crypto mCrypto) throws StorageCryptoException {
        return toJson(record, mCrypto).toString();
    }

    /**
     * Get property value from json
     *
     * @param jsonObject json object
     * @param property   property name
     * @return property value
     */
    private static String getPropertyFromJson(JsonObject jsonObject, String property) {
        if (!jsonObject.has(property)) {
            return null;
        }
        return jsonObject.get(property).isJsonNull() ? null : jsonObject.get(property).getAsString();
    }


    //todo refactor

    /**
     * Creates JSONObject with FindFilter object properties
     *
     * @param filter  FindFilter
     * @param mCrypto crypto object
     * @return JSONObject with properties corresponding to FindFilter object properties
     */
    public static JSONObject toJson(FindFilter filter, Crypto mCrypto) {
        JSONObject json = new JSONObject();
        if (filter != null) {
            addToJson(json, P_KEY, filter.getKeyParam(), mCrypto);
            addToJson(json, P_KEY_2, filter.getKey2Param(), mCrypto);
            addToJson(json, P_KEY_3, filter.getKey3Param(), mCrypto);
            addToJson(json, P_PROFILE_KEY, filter.getProfileKeyParam(), mCrypto);
            addToJson(json, P_VERSION, filter.getVersionParam(), mCrypto);
            FilterRangeParam range = filter.getRangeKeyParam();
            if (range != null) {
                json.put(P_RANGE_KEY, range.isConditional() ? conditionJSON(range) : valueJSON(range));
            }
        }
        return json;
    }

    private static void addToJson(JSONObject json, String paramName, FilterStringParam param, Crypto mCrypto) {
        if (param != null) {
            if (paramName.equals(P_VERSION)) {
                json.put(paramName, param.isNotCondition() ? addNotCondition(param, null, false) : toJsonInt(param));
            } else {
                json.put(paramName, param.isNotCondition() ? addNotCondition(param, mCrypto, true) : toJsonString(param, mCrypto));
            }
        }
    }

    /**
     * Adds 'not' condition to parameter of FindFilter
     *
     * @param param       parameter to which the not condition should be added
     * @param mCrypto     crypto object
     * @param isForString the condition must be added for string params
     * @return JSONObject with added 'not' condition
     */
    private static JSONObject addNotCondition(FilterStringParam param, Crypto mCrypto, boolean isForString) {
        JSONArray arr = isForString ? toJsonString(param, mCrypto) : toJsonInt(param);
        return new JSONObject(String.format("{$not: %s}", arr != null ? arr.toString() : null));
    }

    //todo refactor
    public static BatchRecord batchRecordFromString(String responseString, Crypto mCrypto) throws StorageCryptoException {
        List<RecordException> errors = new ArrayList<>();
        Gson gson = getGson4Records();
        JsonObject responseObject = gson.fromJson(responseString, JsonObject.class);

        JsonObject meta = (JsonObject) responseObject.get("meta");
        int count = meta.get("count").getAsInt();
        int limit = meta.get("limit").getAsInt();
        int offset = meta.get("offset").getAsInt();
        int total = meta.get("total").getAsInt();
        List<Record> records = new ArrayList<>();
        if (count != 0) {
            JsonArray data = responseObject.getAsJsonArray("data");
            for (JsonElement item : data) {
                try {
                    records.add(JsonUtils.recordFromString(item.toString(), mCrypto));
                } catch (Exception e) {
                    errors.add(new RecordException("Record Parse Exception", item.toString(), e));
                }
            }
        }
        return new BatchRecord(records, count, limit, offset, total, errors);
    }

    private static JSONArray valueJSON(FilterRangeParam range) {
        if (range.getValues() == null) {
            return null;
        }
        return new JSONArray(range.getValues());
    }

    private static JSONObject conditionJSON(FilterRangeParam range) {
        return new JSONObject().put(range.getOperator(), range.getValue());
    }

    public static JSONObject toJson(FindOptions options) {
        return new JSONObject()
                .put(P_LIMIT, options.getLimit())
                .put(P_OFFSET, options.getOffset());
    }

    private static List<String> hashValue(FilterStringParam param, Crypto mCrypto) {
        return param.getValue().stream().map(mCrypto::createKeyHash).collect(Collectors.toList());
    }

    public static JSONArray toJsonString(FilterStringParam param, Crypto mCrypto) {
        if (param.getValue() == null) return null;
        if (mCrypto == null) return new JSONArray(param.getValue());

        return new JSONArray(hashValue(param, mCrypto));
    }

    public static JSONArray toJsonInt(FilterStringParam param) {
        if (param.getValue() == null) {
            return null;
        }
        return new JSONArray(param.getValue().stream().map(Integer::parseInt).collect(Collectors.toList()));
    }

    private static Gson getGson4Records() {
        return new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    /**
     * inner class for cosy encryption and serialization of {@link Record} instances
     */
    private static class EncryptedRecord extends Record {
        private Integer version;

        public EncryptedRecord() {
        }

        public EncryptedRecord(Record record, Crypto mCrypto, String bodyJsonString) throws StorageCryptoException {
            setKey(mCrypto.createKeyHash(record.getKey()));
            setKey2(mCrypto.createKeyHash(record.getKey2()));
            setKey3(mCrypto.createKeyHash(record.getKey3()));
            setProfileKey(mCrypto.createKeyHash(record.getProfileKey()));
            setRangeKey(record.getRangeKey());

            Pair<String, Integer> encBodyAndVersion = mCrypto.encrypt(bodyJsonString);
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
         * @return
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

        public void justDecryptKeys(Crypto mCrypto) throws StorageCryptoException {
            setKey(mCrypto.decrypt(getKey(), version));
            setKey2(mCrypto.decrypt(getKey2(), version));
            setKey3(mCrypto.decrypt(getKey3(), version));
            setProfileKey(mCrypto.decrypt(getProfileKey(), version));
        }

        public void decryptAllFromBody(Gson gson) {
            JsonObject bodyObj = gson.fromJson(getBody(), JsonObject.class);
            setBody(getPropertyFromJson(bodyObj, P_PAYLOAD));
            String meta = getPropertyFromJson(bodyObj, P_META);
            JsonObject metaObj = gson.fromJson(meta, JsonObject.class);
            setKey(getPropertyFromJson(metaObj, P_KEY));
            setProfileKey(getPropertyFromJson(metaObj, P_PROFILE_KEY));
            setKey2(getPropertyFromJson(metaObj, P_KEY_2));
            setKey3(getPropertyFromJson(metaObj, P_KEY_3));
        }
    }
}
