package com.ajaxjs.framework.watchlog;

import com.ajaxjs.framework.watchlog.impl.ReadFile;
import org.junit.jupiter.api.Test;

public class TestLogTailer {
    @Test
    void test() {
        ReadFile tailer = new ReadFile("C:\\code\\ajaxjs\\aj-framework\\aj-base\\src\\test\\java\\com\\ajaxjs\\base\\watchlog\\bar.txt", 1000, true);
        tailer.setTailing(true);
        tailer.start();
        tailer.setCallback(System.out::println);

        while (true) {

        }
    }
}
