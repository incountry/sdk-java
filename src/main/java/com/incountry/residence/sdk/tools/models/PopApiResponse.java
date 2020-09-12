package com.incountry.residence.sdk.tools.models;

public class PopApiResponse {

    private String content;
    private String fileExtension;

    public PopApiResponse(String content) {
        this.content = content;
    }

    public PopApiResponse(String content, String fileExtension) {
        this.content = content;
        this.fileExtension = fileExtension;
    }

    public String getContent() {
        return content;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
