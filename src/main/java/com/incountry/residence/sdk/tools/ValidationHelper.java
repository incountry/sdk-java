package com.incountry.residence.sdk.tools;

import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.apache.logging.log4j.Logger;

public class ValidationHelper {
    private final Logger log;

    public ValidationHelper(Logger log) {
        this.log = log;
    }

    public <T extends StorageException> void check(
            Class<T> exceptionClass, boolean errorCondition, String exceptionMessage, Object... params)
            throws T {
        if (!errorCondition) {
            return;
        }
        String message = (params != null && params.length > 0) ? String.format(exceptionMessage, params) : exceptionMessage;
        log.error(message);
        try {
            throw exceptionClass.getConstructor(String.class).newInstance(message);
        } catch (ReflectiveOperationException ex) {
            log.error(ex);
        }
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
