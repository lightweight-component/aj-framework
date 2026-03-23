package com.ajaxjs.util.log;


import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.log.TextBox;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestTextBox {
    @Test
    void test() {
        TextBox textBox1 = new TextBox();
        textBox1.setWrapLongLines(false);
        String s = textBox1.boxStart("Hello World").line("foo", "bar").line("bar", "foo").boxEnd();
        log.info(s);

        boolean isOk = false;

        String title = isOk ? " HTTP ServerRequest " : " HTTP ServerRequest ErrResponse ";
        TextBox textBox = new TextBox();
        textBox.setBoxColor(isOk ? TextBox.ANSI_YELLOW : TextBox.ANSI_RED);

        textBox.boxStart(title)
                .line("Time:       ", DateTools.now())
                .line("TraceId:    ", "MDC.get(BoxLogger")
                .line("Request:    ", "http://qq.com")
                .line("Parameters: ", "{foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar," +
                        "foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar," +
                        "foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar" +
                        "foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar,foo:bar}")
                .line("ReturnCode: ", "HTTP status 200")
                .line("ReturnText: ", "{}")
                .line("Execution:  ", "30ms");

        log.info(textBox.boxEnd());
        System.out.println("ok");
    }
}
