package com.ajaxjs.tracing;

import com.alibaba.ttl.TtlRunnable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

/**
 * 新增对非 servlet 上下文请求入口
 * 应用场景：
 * 1.定时调度器方法，需要当前调用的请求方法内调用三方业务的事物流水号串联起来；
 * 2.消息监听器方法，需要当前调用的请求方法内调用三方业务的事物流水号串联起来；
 * 3.也可以应用在正常的控制器请求方法；
 */
public class TracingWrapper {
    /**
     * 通过Ttl修饰运行指定的线程
     * 默认业务逻辑上的servlet上下文
     * 使用案例如下：
     * <pre>{@code
     *     //@Scheduled(fixedRate = 5000)
     *     public void doSchedule() {
     *         ContextWrapper.run(() -> {
     *             System.out.println("start--------上下文-1-" + LocalContextHolder.current().getTraceId());
     *             mysqlMapper.getMysql("田晓霞", "123456");
     *             System.out.println("--------上下文-2-" + LocalContextHolder.current().getTraceId());
     *             mysqlMapper.getMysql("孙少平", "123457");
     *             System.out.println("end--------上下文-3-" + LocalContextHolder.current().getTraceId());
     *         });
     *     }
     * }</pre>
     *
     * @param runnable 线程
     */
    public static void run(ThreadPoolTaskExecutor task, Runnable runnable) {
        run(task, runnable, null);
    }

    /**
     * 1.对线程前初始化上下文，并标记是否是逻辑上的servlet上下文
     * 2.将线程通过TTL修饰后加入线程池执行；
     * 3.最后移除第一步初始化的上下文
     *
     * @param runnable 线程
     * @param traceId  事务流水号
     */
    public static void run(ThreadPoolTaskExecutor task, Runnable runnable, String traceId) {
        try {
            if (StringUtils.hasText(traceId)) // 事务流水号
                LocalContextHolder.current().setTraceId(traceId);

            LocalContextHolder.current().setServlet(true);   // 初始化上下文
            task.execute(TtlRunnable.get(runnable)); // 执行具体代码
        } finally {
            LocalContextHolder.unbind(true);// 移除上下文值设置
        }
    }
}
