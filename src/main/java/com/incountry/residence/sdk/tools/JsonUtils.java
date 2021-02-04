package com.incountry.residence.sdk.tools;

import com.google.gson.JsonObject;

public class JsonUtils {

    private static final String P_FILE_NAME = "filename";
    private static final String P_MIME_TYPE = "mime_type";

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

}
