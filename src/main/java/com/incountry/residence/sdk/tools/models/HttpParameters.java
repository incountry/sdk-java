package com.incountry.residence.sdk.tools.models;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;

import java.util.Map;

public class HttpParameters {

    private String method;
    private Map<Integer, ApiResponse> codeMap;
    private String contentType;

    public HttpParameters(String method, Map<Integer, ApiResponse> codeMap, String contentType) {
        this.method = method;
        this.codeMap = codeMap;
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<Integer, ApiResponse> getCodeMap() {
        return codeMap;
    }

    public void setCodeMap(Map<Integer, ApiResponse> codeMap) {
        this.codeMap = codeMap;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
