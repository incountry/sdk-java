package com.incountry.residence.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.tools.JsonUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AttachmentMetaTest {

    private static String fileId = "123456";
    private static String downloadLink = "some_link";
    private static String fileName = "test_file";
    private static String hash = "1234567890";
    private static String mimeType = "text/plain";
    private static int size = 1000;

    @SuppressWarnings("java:S5785")
    @Test
    void testEquals() {

        AttachmentMeta attachmentMeta1 = new AttachmentMeta();
        AttachmentMeta attachmentMeta2 = new AttachmentMeta();
        assertEquals(attachmentMeta2, attachmentMeta1);
        assertEquals(attachmentMeta1, attachmentMeta2);

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("downloadLink", downloadLink);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);

        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("fileId", fileId);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("fileName", fileName);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("hash", hash);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("mimeType", mimeType);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("size", size);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        assertEquals(attachmentMeta2, attachmentMeta1);
        assertEquals(attachmentMeta1, attachmentMeta1);
        assertNotEquals(new AttachmentMeta(), attachmentMeta1);
        assertNotEquals(attachmentMeta1, new Object());
        assertFalse(attachmentMeta1.equals(null));

        String createUpdateAt = "2020-10-09T10:52:54+00:00";

        String attachmentMetaString1 = String.format("{\"created_at\": \"%s\"}", createUpdateAt);
        AttachmentMeta attachmentMeta3 = JsonUtils.getDataFromAttachmentMetaJson(attachmentMetaString1);
        assertNotEquals(attachmentMeta3, new AttachmentMeta());
        String attachmentMetaString2 = String.format("{\"updated_at\": \"%s\"}", createUpdateAt);
        AttachmentMeta attachmentMeta4 = JsonUtils.getDataFromAttachmentMetaJson(attachmentMetaString2);
        assertNotEquals(attachmentMeta4, new AttachmentMeta());

        assertNotEquals(attachmentMeta3.getCreatedAt(), attachmentMeta4.getCreatedAt());
        assertNotEquals(attachmentMeta3.getUpdatedAt(), attachmentMeta4.getUpdatedAt());
    }

    @Test
    void testHashCode() {
        Gson gson = new Gson();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("downloadLink", downloadLink);
        jsonObject.addProperty("fileId", fileId);
        jsonObject.addProperty("fileName", fileName);
        jsonObject.addProperty("hash", hash);
        jsonObject.addProperty("mimeType", mimeType);
        jsonObject.addProperty("size", size);
        AttachmentMeta attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        AttachmentMeta attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);

        assertEquals(attachmentMeta1, attachmentMeta2);
        assertEquals(attachmentMeta1.hashCode(), attachmentMeta2.hashCode());
    }
}
