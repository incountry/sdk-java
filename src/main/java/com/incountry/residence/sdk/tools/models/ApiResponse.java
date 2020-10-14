package com.incountry.residence.sdk.tools.models;

import java.util.Map;

public class ApiResponse {

    private String content;
    private Map<MetaInfoTypes, String> metaInfo;

    public ApiResponse(String content) {
        this.content = content;
    }

    public ApiResponse(String content, Map<MetaInfoTypes, String> metaInfo) {
        this.content = content;
        this.metaInfo = metaInfo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<MetaInfoTypes, String> getMetaInfo() {
        return metaInfo;
    }

    @Override
    public String toString() {
            return "ApiResponse{" +
                    ", metaInfo=" + metaInfo +
                    '}';
    }
}
