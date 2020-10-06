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

    public void setMetaInfo(Map<MetaInfoTypes, String> metaInfo) {
        this.metaInfo = metaInfo;
    }

    @Override
    public String toString() {
        if (content.length() < 1000) {
            return "ApiResponse{" +
                    "content=" + content +
                    ", metaInfo=" + metaInfo +
                    '}';
        }
        return "ApiResponse{" +
                "metaInfo=" + metaInfo +
                '}';

    }
}
