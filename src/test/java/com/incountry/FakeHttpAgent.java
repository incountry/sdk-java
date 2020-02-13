package com.incountry;

import com.incountry.exceptions.StorageServerException;
import com.incountry.http.HttpAgent;

import java.io.IOException;

public class FakeHttpAgent implements HttpAgent {
    private String response;
    private String callEndpoint;
    private String callBody;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    public String getCallBody() {
        return callBody;
    }
    public String getCallEndpoint() { return callEndpoint; }

    @Override
    public String request(String endpoint, String method, String body, boolean allowNone) throws IOException, StorageServerException {
        callBody = body;
        callEndpoint = endpoint;
        return response;
    }
}
