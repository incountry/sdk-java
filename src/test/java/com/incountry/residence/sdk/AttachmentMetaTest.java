package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.tools.JsonUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AttachmentMetaTest {

    private static String fileId = "123456";
    private static String downloadLink = "some_link";
    private static String fileName = "test_file";
    private static String hash = "1234567890";
    private static String mimeType = "text/plain";
    private static int size = 1000;

    @Test
    void testEquals() {

        AttachmentMeta attachmentMeta1 = new AttachmentMeta();
        AttachmentMeta attachmentMeta2 = new AttachmentMeta();
        assertEquals(attachmentMeta2, attachmentMeta1);
        assertEquals(attachmentMeta1, attachmentMeta2);

        attachmentMeta1.setDownloadLink(downloadLink);
        attachmentMeta2.setDownloadLink(downloadLink);
        assertEquals(attachmentMeta2, attachmentMeta1);

        attachmentMeta2.setFileId(fileId);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta1.setFileId(fileId);
        assertEquals(attachmentMeta2, attachmentMeta1);

        attachmentMeta1.setFileName(fileName);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2.setFileName(fileName);
        assertEquals(attachmentMeta2, attachmentMeta1);

        attachmentMeta1.setHash(hash);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2.setHash(hash);
        assertEquals(attachmentMeta2, attachmentMeta1);

        attachmentMeta1.setMimeType(mimeType);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2.setMimeType(mimeType);
        assertEquals(attachmentMeta2, attachmentMeta1);

        attachmentMeta1.setSize(size);
        assertNotEquals(attachmentMeta2, attachmentMeta1);
        attachmentMeta2.setSize(size);
        assertEquals(attachmentMeta2, attachmentMeta1);

        assertEquals(attachmentMeta2, attachmentMeta1);
        assertEquals(attachmentMeta1, attachmentMeta1);
        assertNotEquals(new AttachmentMeta(), attachmentMeta1);
        assertNotEquals(attachmentMeta1, new Object());
        assertNotEquals(null, attachmentMeta1);

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
        AttachmentMeta attachmentMeta1 = new AttachmentMeta();
        attachmentMeta1.setDownloadLink(downloadLink);
        attachmentMeta1.setFileId(fileId);
        attachmentMeta1.setFileName(fileName);
        attachmentMeta1.setHash(hash);
        attachmentMeta1.setMimeType(mimeType);
        attachmentMeta1.setSize(size);

        AttachmentMeta attachmentMeta2 = new AttachmentMeta();
        attachmentMeta2.setDownloadLink(downloadLink);
        attachmentMeta2.setFileId(fileId);
        attachmentMeta2.setFileName(fileName);
        attachmentMeta2.setHash(hash);
        attachmentMeta2.setMimeType(mimeType);
        attachmentMeta2.setSize(size);

        assertEquals(attachmentMeta1, attachmentMeta2);
        assertEquals(attachmentMeta1.hashCode(), attachmentMeta2.hashCode());
    }
}
