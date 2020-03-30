package com.incountry.storage.sdk.tools;

import com.google.gson.*;
import com.incountry.storage.sdk.dto.*;
import com.incountry.storage.sdk.dto.search.FilterRangeParam;
import com.incountry.storage.sdk.dto.search.FilterStringParam;
import com.incountry.storage.sdk.dto.search.FindFilter;
import com.incountry.storage.sdk.dto.search.FindOptions;
import com.incountry.storage.sdk.tools.crypto.Crypto;
import com.incountry.storage.sdk.tools.exceptions.RecordException;
import com.incountry.storage.sdk.tools.exceptions.StorageCryptoException;
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
        Gson gson = getGson();
        JsonObject jsonObject = (JsonObject) gson.toJsonTree(record);
        jsonObject.remove(P_COUNTRY);
        if (mCrypto == null) {
            return jsonObject;
        }
        jsonObject.remove(P_BODY);
        Map<String, String> mapBodyMeta = new HashMap<>();
        mapBodyMeta.put(P_PAYLOAD, record.getBody());
        mapBodyMeta.put(P_META, jsonObject.toString());
        String packedBody = gson.toJson(mapBodyMeta);
        VersionRecord encRec = cryptRecord(record, mCrypto, packedBody);
        return  (JsonObject)gson.toJsonTree(encRec);
    }

    private static VersionRecord cryptRecord(Record record, Crypto mCrypto, String bodyJsonString) throws StorageCryptoException {
        VersionRecord encVersRec = new VersionRecord();
        encVersRec.setKey(mCrypto.createKeyHash(record.getKey()));
        encVersRec.setKey2(mCrypto.createKeyHash(record.getKey2()));
        encVersRec.setKey3(mCrypto.createKeyHash(record.getKey3()));
        encVersRec.setProfileKey(mCrypto.createKeyHash(record.getProfileKey()));
        encVersRec.setRangeKey(record.getRangeKey());

        Pair<String, Integer> encBodyAndVersion = mCrypto.encrypt(bodyJsonString);
        encVersRec.setBody(encBodyAndVersion.getValue0());
        encVersRec.setVersion(encBodyAndVersion.getValue1() != null ? encBodyAndVersion.getValue1() : 0);
        return encVersRec;
    }

    /**
     * Create record object from json string
     *
     * @param jsonString json string
     * @param mCrypto    crypto object
     * @return record objects with data from json
     * @throws StorageCryptoException if decryption failed
     */
    public static Record recordFromString(String jsonString, Crypto mCrypto) throws StorageCryptoException {
        Gson gson = getGson();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);

        String country = getPropertyFromJson(jsonObject, P_COUNTRY);
        String key = getPropertyFromJson(jsonObject, P_KEY);
        String body = getPropertyFromJson(jsonObject, P_BODY);
        String profileKey = getPropertyFromJson(jsonObject, P_PROFILE_KEY);
        Integer rangeKey = getPropertyFromJson(jsonObject, P_RANGE_KEY) != null ? Integer.parseInt(getPropertyFromJson(jsonObject, P_RANGE_KEY)) : null;
        String key2 = getPropertyFromJson(jsonObject, P_KEY_2);
        String key3 = getPropertyFromJson(jsonObject, P_KEY_3);

        Integer version = Integer.parseInt(getPropertyFromJson(jsonObject, P_VERSION) != null ? getPropertyFromJson(jsonObject, P_VERSION) : "0");

        if (body != null && mCrypto != null) {
            String[] parts = body.split(":");

            body = mCrypto.decrypt(body, version);

            if (parts.length != 2) {
                key = mCrypto.decrypt(key, version);
                profileKey = mCrypto.decrypt(profileKey, version);
                key2 = mCrypto.decrypt(key2, version);
                key3 = mCrypto.decrypt(key3, version);
            } else {
                JsonObject bodyObj = gson.fromJson(body, JsonObject.class);
                body = getPropertyFromJson(bodyObj, P_PAYLOAD);
                String meta = getPropertyFromJson(bodyObj, P_META);
                JsonObject metaObj = gson.fromJson(meta, JsonObject.class);
                key = getPropertyFromJson(metaObj, P_KEY);
                profileKey = getPropertyFromJson(metaObj, P_PROFILE_KEY);
                key2 = getPropertyFromJson(metaObj, P_KEY_2);
                key3 = getPropertyFromJson(metaObj, P_KEY_3);
            }
        }
        return new Record(country, key, body, profileKey, rangeKey, key2, key3);
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

    public static BatchRecord batchRecordFromString(String responseString, Crypto mCrypto) throws StorageCryptoException {
        List<RecordException> errors = new ArrayList<>();
        Gson gson = getGson();
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

    private static Gson getGson() {
        return new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    private static class VersionRecord extends Record {
        private Integer version;

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }
    }
}
