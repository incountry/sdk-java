package com.incountry.residence.sdk.tools.exceptions;

public class RecordException extends Exception {
    private final String rawData;
    private final Exception originalException;

    public RecordException(String exceptionText, String rawData) {
        super(exceptionText);
        this.rawData = rawData;
        this.originalException = null;
    }

    public RecordException(String exceptionText, String rawData, Exception originalException) {
        super(exceptionText);
        this.rawData = rawData;
        this.originalException = originalException;
    }

    public String getRawData() {
        return this.rawData;
    }

    public Exception getOriginalException() {
        return this.originalException;
    }
}