package com.incountry.residence.sdk.tools;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.Logger;

public class NullChecker {

    private NullChecker() {
    }

    public static void checkNull(Logger logger, Object objectToCHeck, StorageClientException exception, String errorMessage) throws StorageClientException {
        if (objectToCHeck == null) {
            logger.error(errorMessage);
            throw exception;
        }
    }

}
