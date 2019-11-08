package com.incountry;

import com.incountry.exceptions.StorageServerException;
import com.incountry.http.IHttpAgent;

import java.io.IOException;

public class FakeHttpAgent implements IHttpAgent {
    private String response;
    private String callBody;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    public String getCallBody() {
        return callBody;
    }

    @Override
    public String request(String endpoint, String method, String body, boolean allowNone) throws IOException, StorageServerException {
        callBody = body;
        return response;
    }
}
