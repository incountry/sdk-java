package com.incountry.residence.sdk.tools.models;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponseCodes;

import java.util.Map;

public class HttpParameters {

    private String method;
    private Map<Integer, ApiResponseCodes> codeMap;
    private String contentType;

    public HttpParameters(String method, Map<Integer, ApiResponseCodes> codeMap, String contentType) {
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

    public Map<Integer, ApiResponseCodes> getCodeMap() {
        return codeMap;
    }

    public void setCodeMap(Map<Integer, ApiResponseCodes> codeMap) {
        this.codeMap = codeMap;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return "HttpParameters{" +
                "method=" + method +
                ", codeMap=" + codeMap +
                ", contentType=" + contentType +
                '}';
    }
}
