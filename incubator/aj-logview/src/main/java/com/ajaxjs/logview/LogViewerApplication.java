package com.ajaxjs.logview;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;

/**
 * Spring Boot日志查看系统主启动类
 *
 * @author example
 * @version 1.0.0
 */
@SpringBootApplication
@Slf4j
public class LogViewerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogViewerApplication.class, args);
        System.out.println("\n==================================");
        System.out.println("日志查看系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("==================================");
        logTest();
    }

    /**
     * 模拟日志输出
     */
    public static void logTest() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000); // 使用原生 Thread.sleep 替代 ThreadUtil.sleep
                    int random = randomInt(1, 4);
                    if (random == 1)
                        log.info("这是一条info日志");
                    else if (random == 2)
                        log.warn("这是一条warn日志");
                    else
                        log.error("这是一条error日志");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    log.error("线程被中断", e);
                    break;
                }
            }
        }).start();
    }

    /**
     * Generates a random integer between the specified minimum and maximum values (inclusive).
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer between min and max (inclusive)
     */
    public static int randomInt(int min, int max) {
        if (min > max)
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");

        return new Random().nextInt(max - min + 1) + min;
    }
}