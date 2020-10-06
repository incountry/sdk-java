package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.models.RequestParameters;
import com.incountry.residence.sdk.tools.models.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

public interface HttpAgent {

    ApiResponse request(String url, String body, String audience, String region, int retryCount,
                        RequestParameters requestParameters) throws StorageServerException, StorageClientException;

}
