package com.incountry.residence.sdk;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.RepetitionInfo;

public class LogLevelUtils {

    private LogLevelUtils() {
    }

    public static void iterateLogLevel(RepetitionInfo repetitionInfo, Class clazz) {
        switch (repetitionInfo.getCurrentRepetition()) {
            case 1:
                Configurator.setLevel(clazz.getName(), Level.INFO);
                break;
            case 2:
                Configurator.setLevel(clazz.getName(), Level.DEBUG);
                break;
            default:
                Configurator.setLevel(clazz.getName(), Level.TRACE);
        }
    }
}
