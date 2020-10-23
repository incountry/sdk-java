package com.incountry.residence.sdk.dto;

import java.util.Date;
import java.util.Objects;

public class AttachmentMeta {

    private Date createdAt;
    private Date updatedAt;
    private String downloadLink;
    private String fileId;
    private String filename;
    private String hash;
    private String mimeType;
    private int size;

    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFilename() {
        return filename;
    }

    public String getHash() {
        return hash;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, updatedAt, downloadLink, fileId, filename, hash, mimeType, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AttachmentMeta attachmentMeta = (AttachmentMeta) obj;
        return Objects.equals(createdAt, attachmentMeta.createdAt) &&
                Objects.equals(updatedAt, attachmentMeta.updatedAt) &&
                Objects.equals(downloadLink, attachmentMeta.downloadLink) &&
                Objects.equals(fileId, attachmentMeta.fileId) &&
                Objects.equals(filename, attachmentMeta.filename) &&
                Objects.equals(hash, attachmentMeta.hash) &&
                Objects.equals(mimeType, attachmentMeta.mimeType) &&
                Objects.equals(size, attachmentMeta.size);
    }

    @Override
    public String toString() {
        return "AttachmentMeta{" +
                "createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", downloadLink=" + downloadLink.hashCode() +
                ", fileId=" + fileId.hashCode() +
                ", filename=" + filename.hashCode() +
                ", hash=" + hash.hashCode() +
                ", mimeType=" + mimeType.hashCode() +
                ", size=" + size +
                '}';
    }
}
