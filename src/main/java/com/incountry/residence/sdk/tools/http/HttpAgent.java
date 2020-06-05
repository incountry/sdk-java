package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.util.Map;

public interface HttpAgent {

    String request(String url, String method, String body, Map<Integer, ApiResponse> codeMap,
                   String audience, int retryCount) throws StorageServerException;
}
