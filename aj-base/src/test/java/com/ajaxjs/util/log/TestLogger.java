package com.ajaxjs.util.log;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestLogger {
    @Test
    void testLogger() {
        log.info("Just a test.");
        log.warn("Just a test.");
        log.error("Just a test.");
        log.debug("Just a test.");
        log.trace("Just a test.");
    }
}
