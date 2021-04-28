package com.incountry.residence.sdk.tools.containers;

import java.io.InputStream;

public class RequestParameters {

    private static final String APPLICATION_JSON = "application/json";

    private final String method;
    private final String contentType;
    private final InputStream dataStream;
    private final String fileName;

    public RequestParameters(String method) {
        this(method, APPLICATION_JSON, null, null);
    }

    public RequestParameters(String method, String contentType, InputStream dataStream, String fileName) {
        this.method = method;
        this.contentType = contentType;
        this.dataStream = dataStream;
        this.fileName = fileName;
    }

    public String getMethod() {
        return method;
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
