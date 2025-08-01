package com.ajaxjs.framework.mvc.filter;

import org.junit.jupiter.api.Test;

import static com.ajaxjs.framework.mvc.filter.RequestLogger.printLog;

public class TestRequestLogger {
    @Test
    void print() {
        printLog(
                "SELECT",
                "SELECT COUNT(*) FROM user WHERE 1 = 1 AND tenant_id = 3",
                "33ms",
                "{COUNT(*)=1}"
        );
    }
}
