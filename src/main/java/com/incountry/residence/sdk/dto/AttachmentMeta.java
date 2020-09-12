package com.incountry.residence.sdk.dto;

public class AttachmentMeta {

    private String createdAt;
    private String downloadLink;
    private String fileId;
    private String fileName;
    private String hash;
    private String mimeType;
    private int size;
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AttachmentMeta{" +
                "createdAt=" + createdAt +
                ", downloadLink=" + downloadLink +
                ", fileId=" + fileId +
                ", fileName=" + fileName +
                ", hash=" + hash +
                ", mimeType=" + mimeType +
                ", size=" + size +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
