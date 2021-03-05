package com.incountry.residence.sdk;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AttachmentMetaTest {
    private static final String FILE_ID = "123456";
    private static final String SOME_LINK = "some_link";
    private static final String FILE_NAME = "test_file";
    private static final String HASH = "1234567890";
    private static final String MIME_TYPE = "text/plain";
    private static final int SIZE = 1000;

    @SuppressWarnings("java:S5785")
    @Test
    void testEquals() {
        AttachmentMeta attachmentMeta1 = new AttachmentMeta();
        AttachmentMeta attachmentMeta2 = new AttachmentMeta();
        assertEquals(attachmentMeta2, attachmentMeta1);
        assertEquals(attachmentMeta1, attachmentMeta2);

        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("download_link", SOME_LINK);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);

        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("file_id", FILE_ID);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("filename", FILE_NAME);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("hash", HASH);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("mime_type", MIME_TYPE);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        jsonObject.addProperty("size", SIZE);
        attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);
        assertEquals(attachmentMeta2, attachmentMeta1);

        assertEquals(attachmentMeta2, attachmentMeta1);
        assertEquals(attachmentMeta1, attachmentMeta1);
        assertNotEquals(new AttachmentMeta(), attachmentMeta1);
        assertNotEquals(attachmentMeta1, new Object());
        assertNotEquals(null, attachmentMeta1);
        assertFalse(attachmentMeta1.equals(UUID.randomUUID()));
        assertFalse(attachmentMeta1.equals(null));
        assertNotNull(attachmentMeta1);

        String createUpdateAt = "2020-10-09T10:52:54+00:00";

        String attachmentMetaString1 = String.format("{\"created_at\": \"%s\"}", createUpdateAt);
        AttachmentMeta attachmentMeta3 = gson.fromJson(attachmentMetaString1, AttachmentMeta.class);
        assertNotEquals(attachmentMeta3, new AttachmentMeta());
        String attachmentMetaString2 = String.format("{\"updated_at\": \"%s\"}", createUpdateAt);
        AttachmentMeta attachmentMeta4 = gson.fromJson(attachmentMetaString2, AttachmentMeta.class);
        assertNotEquals(attachmentMeta4, new AttachmentMeta());

        assertNotEquals(attachmentMeta3.getCreatedAt(), attachmentMeta4.getCreatedAt());
        assertNotEquals(attachmentMeta3.getUpdatedAt(), attachmentMeta4.getUpdatedAt());
    }

    @Test
    void testHashCode() {
        Gson gson = new Gson();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("downloadLink", SOME_LINK);
        jsonObject.addProperty("fileId", FILE_ID);
        jsonObject.addProperty("fileName", FILE_NAME);
        jsonObject.addProperty("hash", HASH);
        jsonObject.addProperty("mimeType", MIME_TYPE);
        jsonObject.addProperty("size", SIZE);
        AttachmentMeta attachmentMeta1 = gson.fromJson(jsonObject, AttachmentMeta.class);
        AttachmentMeta attachmentMeta2 = gson.fromJson(jsonObject, AttachmentMeta.class);

        assertEquals(attachmentMeta1, attachmentMeta2);
        assertEquals(attachmentMeta1.hashCode(), attachmentMeta2.hashCode());
    }
}
