package com.incountry.residence.sdk.dto;

import java.io.InputStream;

public class AttachedFile {

    private InputStream fileContent;
    private String fileExtension;

    public AttachedFile(InputStream fileContent, String fileExtension) {
        this.fileContent = fileContent;
        this.fileExtension = fileExtension;
    }

    public InputStream getFileContent() {
        return fileContent;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
