package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

public interface TokenGenerator {

    String getToken(String audienceUrl) throws StorageServerException;

    String refreshToken(boolean force, String audienceUrl) throws StorageServerException;

    boolean canRefreshToken();
}
