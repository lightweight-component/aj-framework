package com.ajaxjs.framework;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogger {
    @Test
    void test() {
        Logger logger = LoggerFactory.getLogger(TestLogger.class);

        if (logger instanceof ch.qos.logback.classic.Logger)
            System.out.println("Using Logback");
        else
            System.out.println("Unknown logging framework");

//        Object loggerContext = ((ch.qos.logback.classic.Logger) logger).getLoggerContext();
//        if (loggerContext instanceof LoggerContext) {
//            System.out.println("Using Logback");
//        } else {
//            System.out.println("Unknown logging framework");
//        }
    }

}
