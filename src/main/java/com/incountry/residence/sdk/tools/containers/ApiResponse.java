package com.incountry.residence.sdk.tools.containers;

import java.io.InputStream;
import java.util.Map;

public class ApiResponse {

    private final String content;
    private final Map<MetaInfoTypes, String> metaInfo;
    private final InputStream inputStream;

    public ApiResponse() {
        this(null, null, null);
    }

    public ApiResponse(String content, Map<MetaInfoTypes, String> metaInfo) {
        this(content, metaInfo, null);
    }

    public ApiResponse(String content, Map<MetaInfoTypes, String> metaInfo, InputStream inputStream) {
        this.content = content;
        this.metaInfo = metaInfo;
        this.inputStream = inputStream;
    }

    public String getContent() {
        return content;
    }

    public Map<MetaInfoTypes, String> getMetaInfo() {
        return metaInfo;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
