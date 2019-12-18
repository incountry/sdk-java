package com.incountry.http;

import com.incountry.exceptions.StorageServerException;

import java.io.IOException;

public interface IHttpAgent {
    String request(String endpoint, String method, String body, boolean allowNone) throws IOException, StorageServerException;
}
