package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.http.HttpAgent;

import java.util.List;
import java.util.Map;

public class FakeHttpAgent implements HttpAgent {


    private String callEndpoint;
    private String callMethod;
    private String callBody;
    private String response;
    private List<String> responseList;
    private Map<Integer, ApiResponse> codeMap;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    public FakeHttpAgent(List<String> responseList) {
        this.responseList = responseList;
    }

    @Override
    public String request(String endpoint, String method, String body, Map<Integer, ApiResponse> codeMap) {
        this.callEndpoint = endpoint;
        this.callMethod = method;
        this.callBody = body;
        this.codeMap = codeMap;

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
}
