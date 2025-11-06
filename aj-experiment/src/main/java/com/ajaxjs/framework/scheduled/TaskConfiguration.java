package com.ajaxjs.framework.scheduled;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 程序配置
 */
public class TaskConfiguration {
    // 初始化 Spring 任务调度器
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5); // 指定线程数
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);

        return pool;
    }

    // 初始化任务调度管理
    @Bean(initMethod = "init")
    public ScheduleHandler scheduleHandler() {
        return new ScheduleHandler();
    }

    // 注入任务调度的控制器
    @Bean
    public ScheduledController scheduledController() {
        return new ScheduledController();
    }

    private static final AtomicLong ATOMIC_LONG = new AtomicLong(0L);

    // 添加定时任务
    @Scheduled(cron = "0/2 * * * * *") // cron 表达式，每5秒执行
    public void doTask() {
        System.out.println("我是定时任务~" + ATOMIC_LONG.getAndIncrement());
    }

}
