package com.incountry.residence.sdk.tools.containers;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponseCodes;

import java.util.Map;

public class RequestParameters {

    private String method;
    private Map<Integer, ApiResponseCodes> codeMap;
    private String contentType;
    private boolean fileUpload;
    private String fileName;

    public RequestParameters(String method, Map<Integer, ApiResponseCodes> codeMap, String contentType, boolean fileUpload, String fileName) {
        this.method = method;
        this.codeMap = codeMap;
        this.contentType = contentType;
        this.fileUpload = fileUpload;
        this.fileName = fileName;
    }

    public String getMethod() {
        return method;
    }

    public Map<Integer, ApiResponseCodes> getCodeMap() {
        return codeMap;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isFileUpload() {
        return fileUpload;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "HttpParameters{" +
                "method=" + method +
                ", codeMap=" + codeMap +
                ", contentType=" + contentType +
                ", fileUpload=" + fileUpload +
                ", fileName=" + fileName +
                '}';
    }
}
