package com.incountry.residence.sdk.dto;

import java.util.Date;
import java.util.Objects;

public class AttachmentMeta {

    private Date createdAt;
    private Date updatedAt;
    private String downloadLink;
    private String fileId;
    private String fileName;
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

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, updatedAt, downloadLink, fileId, fileName, hash, mimeType, size);
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
                Objects.equals(fileName, attachmentMeta.fileName) &&
                Objects.equals(hash, attachmentMeta.hash) &&
                Objects.equals(mimeType, attachmentMeta.mimeType) &&
                Objects.equals(size, attachmentMeta.size);
    }

    @Override
    public String toString() {
        return "AttachmentMeta{" +
                "createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", downloadLink=" + downloadLink +
                ", fileId=" + fileId +
                ", fileName=" + fileName +
                ", hash=" + hash +
                ", mimeType=" + mimeType +
                ", size=" + size +
                '}';
    }
}
