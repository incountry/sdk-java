package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.models.PopApiResponse;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.util.Map;

public interface HttpAgent {

    PopApiResponse request(String url, String method, String body, Map<Integer, ApiResponse> codeMap,
                           String audience, String region, int retryCount, String contentType) throws StorageServerException, StorageClientException;

}
