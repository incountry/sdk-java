package com.incountry.exceptions;

public class StorageCryptoException extends StorageException {

    public StorageCryptoException(String s) {
        super(s);
    }

    public StorageCryptoException(String s, Throwable cause) {
        super(s, cause);
    }
}
