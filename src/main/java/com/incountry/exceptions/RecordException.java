package com.incountry.exceptions;

public class RecordException extends Exception {
    String rawData;
    Exception originalException;

    public RecordException(String exceptionText, String rawData){
        super(exceptionText);
        this.rawData = rawData;
    }

    public RecordException(String exceptionText, String rawData, Exception originalException){
        super(exceptionText);
        this.rawData = rawData;
        this.originalException = originalException;
    }

    public String getRawData() {
        return this.rawData;
    }

    public Exception getOriginalException() { return this.originalException; }
}