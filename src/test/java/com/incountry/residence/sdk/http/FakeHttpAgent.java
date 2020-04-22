package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.http.HttpAgent;

import java.util.Map;

public class FakeHttpAgent implements HttpAgent {

    private String callEndpoint;
    private String callMethod;
    private String callBody;
    private String response;
    private Map<Integer, ApiResponse> codeMap;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    @Override
    public String request(String endpoint, String method, String body, Map<Integer, ApiResponse> codeMap) {
        this.callEndpoint = endpoint;
        this.callMethod = method;
        this.callBody = body;
        this.codeMap = codeMap;

        return response;
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
        return response;
    }
}
