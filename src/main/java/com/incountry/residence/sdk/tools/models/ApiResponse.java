package com.incountry.residence.sdk.tools.models;

import java.util.Map;

public class ApiResponse {

    private String content;
    private Map<CustomEnum, String> metaInfo;

    public ApiResponse(String content) {
        this.content = content;
    }

    public ApiResponse(String content, Map<CustomEnum, String> metaInfo) {
        this.content = content;
        this.metaInfo = metaInfo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<CustomEnum, String> getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(Map<CustomEnum, String> metaInfo) {
        this.metaInfo = metaInfo;
    }
}
