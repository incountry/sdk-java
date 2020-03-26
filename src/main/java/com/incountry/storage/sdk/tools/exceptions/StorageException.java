package com.incountry.storage.sdk.tools.exceptions;

public class StorageException extends Exception {
    public StorageException(String s){
        super(s);
    }

    public StorageException(String s, Throwable cause) {
        super(s, cause);
    }
}