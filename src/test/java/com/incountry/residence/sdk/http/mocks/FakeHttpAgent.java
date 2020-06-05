package com.incountry.residence.sdk.http.mocks;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.http.HttpAgent;

import java.util.List;
import java.util.Map;

public class FakeHttpAgent implements HttpAgent {

    private String callUrl;
    private String callMethod;
    private String callBody;
    private String response;
    private List<String> responseList;
    private Map<Integer, ApiResponse> codeMap;
    private int retryCount;
    private String audienceUrl;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    public FakeHttpAgent(List<String> responseList) {
        this.responseList = responseList;
    }

    @Override
    public String request(String url, String method, String body, Map<Integer, ApiResponse> codeMap, String audience, int retryCount) {
        this.callUrl = url;
        this.callMethod = method;
        this.callBody = body;
        this.codeMap = codeMap;
        this.retryCount = retryCount;
        this.audienceUrl = audience;
        return getResponse();
    }

    public String getCallUrl() {
        return callUrl;
    }

    public String getCallMethod() {
        return callMethod;
    }

    public String getCallBody() {
        return callBody;
    }

    public String getAudienceUrl() {
        return audienceUrl;
    }

    public Map<Integer, ApiResponse> getCodeMap() {
        return codeMap;
    }

    public String getResponse() {
        if (responseList != null && !responseList.isEmpty()) {
            response = responseList.get(0);
            if (responseList.size() == 1) {
                responseList = null;
            } else {
                responseList = responseList.subList(1, responseList.size());
            }
        }
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
