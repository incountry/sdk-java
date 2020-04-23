package com.incountry.residence.sdk.tools.exceptions;

public class StorageClientException extends StorageException {

    public StorageClientException(String message) {
        super(message);
    }

    public StorageClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
