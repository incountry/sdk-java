package com.incountry.residence.sdk.tools;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;

import java.util.List;

public class JsonUtils {

    private static final String P_FILE_NAME = "filename";
    private static final String P_MIME_TYPE = "mime_type";

    private static final String MSG_ERR_RESPONSE = "Response parse error";

    private JsonUtils() {
    }

    public static String createUpdatedMetaJson(String fileName, String mimeType) {
        JsonObject json = new JsonObject();
        if (fileName != null && !fileName.isEmpty()) {
            json.addProperty(P_FILE_NAME, fileName);
        }
        if (mimeType != null && !mimeType.isEmpty()) {
            json.addProperty(P_MIME_TYPE, mimeType);
        }
        return json.toString();
    }

    public static AttachmentMeta getDataFromAttachmentMetaJson(String json) {
        return getGson4Records().fromJson(json, AttachmentMeta.class);
    }

    @SuppressWarnings("java:S3740")
    public static Object jsonStringToObject(String json, Class objectClass) throws StorageServerException {
        try {
            return getGson4Records().fromJson(json, objectClass);
        } catch (JsonSyntaxException ex) {
            throw new StorageServerException(MSG_ERR_RESPONSE, ex);
        }
    }

    public static Gson getGson4Records() {
        return new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public static String recordsToJson(List<TransferRecord> records) {
        JsonArray array = new JsonArray();
        for (Record record : records) {
            array.add(getGson4Records().toJsonTree(record));
        }
        JsonObject obj = new JsonObject();
        obj.add("records", array);
        return obj.toString();
    }

}
