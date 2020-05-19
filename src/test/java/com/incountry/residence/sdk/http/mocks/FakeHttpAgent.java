package com.incountry.residence.sdk.http.mocks;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenGenerator;

import java.util.List;
import java.util.Map;

public class FakeHttpAgent implements HttpAgent {

    private String callEndpoint;
    private String callMethod;
    private String callBody;
    private String response;
    private List<String> responseList;
    private Map<Integer, ApiResponse> codeMap;
    private TokenGenerator tokenGenerator;
    private int retryCount;
    private String audienceUrl;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    public FakeHttpAgent(List<String> responseList) {
        this.responseList = responseList;
    }

    @Override
    public String request(String endpoint, String method, String body, Map<Integer, ApiResponse> codeMap, TokenGenerator tokenGenerator, String audienceUrl, int retryCount) {
        this.callEndpoint = endpoint;
        this.callMethod = method;
        this.callBody = body;
        this.codeMap = codeMap;
        this.tokenGenerator = tokenGenerator;
        this.retryCount = retryCount;
        this.audienceUrl = audienceUrl;
        return getResponse();
    }

    public String getCallEndpoint() {
        return callEndpoint;
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

    public TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
