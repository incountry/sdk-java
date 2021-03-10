package com.incountry.residence.sdk.tools.containers;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponseCodes;

import java.io.InputStream;
import java.util.Map;

public class RequestParameters {

    private static final String APPLICATION_JSON = "application/json";

    private final String method;
    private final Map<Integer, ApiResponseCodes> codeMap;
    private final String contentType;
    private final InputStream dataStream;
    private final String fileName;

    public RequestParameters(String method, Map<Integer, ApiResponseCodes> codeMap) {
        this(method, codeMap, APPLICATION_JSON, null, null);
    }

    public RequestParameters(String method, Map<Integer, ApiResponseCodes> codeMap, String contentType, InputStream dataStream, String fileName) {
        this.method = method;
        this.codeMap = codeMap;
        this.contentType = contentType;
        this.dataStream = dataStream;
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

    public InputStream getDataStream() {
        return dataStream;
    }

    public String getFileName() {
        return fileName;
    }
}
