package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

public interface TokenClient {

    String getToken(String audience, String region) throws StorageServerException;

    String refreshToken(boolean force, String audience, String region) throws StorageServerException;

}
