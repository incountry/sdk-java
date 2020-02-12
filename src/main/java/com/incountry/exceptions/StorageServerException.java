package com.incountry.exceptions;

public class StorageServerException extends StorageException {

    public StorageServerException(String s) {
        super(s);
    }

    public StorageServerException(String s, Throwable cause) {
            super(s, cause);
    }

}