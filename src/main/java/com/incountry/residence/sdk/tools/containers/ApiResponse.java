package com.incountry.residence.sdk.tools.containers;

import java.util.Map;

public class ApiResponse {

    private final String content;
    private final Map<MetaInfoTypes, String> metaInfo;

    public ApiResponse() {
        this(null, null);
    }

    public ApiResponse(String content, Map<MetaInfoTypes, String> metaInfo) {
        this.content = content;
        this.metaInfo = metaInfo;
    }

    public String getContent() {
        return content;
    }

    public Map<MetaInfoTypes, String> getMetaInfo() {
        return metaInfo;
    }
}
