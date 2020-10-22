package com.incountry.residence.sdk.dto;

import java.io.InputStream;

public class AttachedFile {

    private final InputStream fileContent;
    private final String fileName;

    public AttachedFile(InputStream fileContent, String fileName) {
        this.fileContent = fileContent;
        this.fileName = fileName;
    }

    public InputStream getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "AttachedFile{" +
                "fileContent=" + fileContent +
                ", fileName=" + fileName.hashCode() +
                '}';
    }
}
