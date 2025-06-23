package com.ajaxjs.base.watchlog;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseTail extends Thread {
    /**
     * 读取时间间隔
     */
    private long interval = Duration.ofSeconds(1).toMillis();

    /**
     * 监视开关，true = 打开监视
     */
    private boolean tailing;

    /**
     * 日志内容
     */
    private String messageLine;

    private Consumer<String> callback;
}
