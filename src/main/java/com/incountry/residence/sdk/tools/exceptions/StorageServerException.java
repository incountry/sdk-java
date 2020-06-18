package com.incountry.residence.sdk.tools.exceptions;

public class StorageServerException extends StorageException {

    public StorageServerException(String message) {
        super(message);
    }

    public StorageServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
