package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.containers.RequestParameters;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

public interface HttpExecutor {

    ApiResponse request(String url, String body, String audience, String region, int retryCount,
                        RequestParameters requestParameters) throws StorageServerException, StorageClientException;

}
