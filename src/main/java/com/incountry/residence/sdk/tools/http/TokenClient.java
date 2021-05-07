package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

@FunctionalInterface
public interface TokenClient {

    String refreshToken(boolean force, String audience, String region) throws StorageServerException;
}
