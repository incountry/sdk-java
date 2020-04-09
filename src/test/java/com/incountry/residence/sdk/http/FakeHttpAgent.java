package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.http.HttpAgent;

public class FakeHttpAgent implements HttpAgent {

    private String callEndpoint;
    private String callMethod;
    private String callBody;
    private boolean callAllowNone;
    private String response;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    @Override
    public String request(String endpoint, String method, String body, boolean allowNone) {
        this.callEndpoint = endpoint;
        this.callMethod = method;
        this.callBody = body;
        this.callAllowNone = allowNone;

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

    public boolean isCallAllowNone() {
        return callAllowNone;
    }

    public String getResponse() {
        return response;
    }
}
