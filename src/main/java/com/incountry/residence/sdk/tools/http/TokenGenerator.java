package com.incountry.residence.sdk.tools.http;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

public interface TokenGenerator {

    String getToken() throws StorageServerException;

    void refreshToken(boolean force) throws StorageServerException;
}
