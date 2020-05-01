package com.incountry.residence.sdk.tools.exceptions;

public class RecordException extends Exception {

    private final String rawData;

    public RecordException(String message, String rawData, Throwable exception) {
        super(message, exception);
        this.rawData = rawData;
    }

    public String getRawData() {
        return this.rawData;
    }
}