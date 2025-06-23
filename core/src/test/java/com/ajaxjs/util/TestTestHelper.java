package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

public class TestTestHelper {
    @Test
    void testTimer() throws InterruptedException {
        TestHelper.setTimer("testTimer-1");
        Thread.sleep(2000);
        TestHelper.setTimer("testTimer-2");
        TestHelper.timerPrint();
    }
}
