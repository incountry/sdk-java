package com.incountry.residence.sdk.dto;

import java.io.InputStream;

public class AttachedFile {

    private InputStream fileContent;
    private String fileName;
    private String fileExtension;

    public AttachedFile(InputStream fileContent, String fileName, String fileExtension) {
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
    }

    public InputStream getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String toString() {
        return "AttachedFile{" +
                "fileContent=" + fileContent +
                ", fileName=" + fileName.hashCode() +
                ", fileExtension=" + fileExtension +
                '}';
    }
}
