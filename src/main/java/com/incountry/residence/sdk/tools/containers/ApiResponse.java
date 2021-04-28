package com.incountry.residence.sdk.tools.containers;

import java.io.InputStream;

public class ApiResponse {

    private final String content;
    private final int responseCode;
    private final String fileName;
    private final InputStream inputStream;

    public ApiResponse(String content, int responseCode, String fileName, InputStream inputStream) {
        this.content = content;
        this.responseCode = responseCode;
        this.fileName = fileName;
        this.inputStream = inputStream;
    }

    public String getContent() {
        return content;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getFileName() {
        return fileName;
    }
}
