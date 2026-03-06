package com.ajaxjs.spring.traceid;

import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.RandomTools;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class TracedThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
    @Override
    public void execute(Runnable task) {
        super.execute(wrap(task, MDC.getCopyOfContextMap()));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(wrap(task, MDC.getCopyOfContextMap()));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(wrap(task, MDC.getCopyOfContextMap()));
    }

    private static void setTraceIdIfAbsent() {
        if (MDC.get(BoxLogger.TRACE_KEY) == null)
            MDC.put(BoxLogger.TRACE_KEY, RandomTools.uuid().toString());
    }

    /**
     * 封装 Callable，复制 MDC 上下文
     * 用于父线程向线程池中提交任务时，将自身 MDC 中的数据复制给子线程
     */
    private static <T> Callable<T> wrap(Callable<T> callable, Map<String, String> context) {
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();

            if (context == null)
                MDC.clear();
            else
                MDC.setContextMap(context);

            setTraceIdIfAbsent();

            try {
                return callable.call();
            } finally {
                if (previous == null) // 恢复线程池线程原来的 MDC，避免影响下一次任务
                    MDC.clear();
                else
                    MDC.setContextMap(previous);
            }
        };
    }

    /**
     * 封装 Runnable，复制 MDC 上下文
     * 用于父线程向线程池中提交任务时，将自身 MDC 中的数据复制给子线程
     */
    private static Runnable wrap(Runnable runnable, Map<String, String> context) {
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();

            if (context == null)
                MDC.clear();
            else
                MDC.setContextMap(context);

            setTraceIdIfAbsent();

            try {
                runnable.run();
            } finally {
                if (previous == null) // 恢复线程池线程原来的 MDC，避免影响下一次任务
                    MDC.clear();
                else
                    MDC.setContextMap(previous);
            }
        };
    }
}
