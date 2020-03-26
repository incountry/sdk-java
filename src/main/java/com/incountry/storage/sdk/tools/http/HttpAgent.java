package com.incountry.storage.sdk.tools.http;

import com.incountry.storage.sdk.tools.exceptions.StorageServerException;

import java.io.IOException;

public interface HttpAgent {
    String request(String endpoint, String method, String body, boolean allowNone) throws IOException, StorageServerException;
}
