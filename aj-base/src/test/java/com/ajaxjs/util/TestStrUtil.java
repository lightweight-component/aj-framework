package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.ajaxjs.util.StrUtil.join;
import static com.ajaxjs.util.StrUtil.leftPad;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestStrUtil {
    final static String str = "中国";

    public static void main(String[] args) {
        System.out.println("你好 %s".replace("%s", "张三"));
    }

    @Test
    void testLeftPad() {
        assertEquals("@@@@@12345", leftPad("12345", 10, "@"));
    }

    @Test
    void messageFormat() {
        String result = MessageFormat.format("您好{0}，晚上好！您目前余额：{1,number,#.##}元，积分：{2}", "张三", 10.155, 10);
        System.out.println(result);
    }

    @Test
    void testJoin() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        System.out.println(join(list, "&"));
    }
}
