package com.ajaxjs.tracing;

import com.ajaxjs.tracing.model.HeaderInfo;
import com.ajaxjs.tracing.model.TracingHolder;
import com.ajaxjs.tracing.model.TracingStage;
import com.alibaba.ttl.TransmittableThreadLocal;

import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * 全链路追踪上下文
 */
public class LocalContextHolder {
    private static final ThreadLocal<TracingHolder> CONTEXT = new TransmittableThreadLocal<>() {
        @Override
        protected TracingHolder initialValue() {
            return new TracingHolder()
                    .systemNumber(getSystemNumber())
                    .traceId(StringUtils.hasText(RequestUtils.getHeader(HeaderInfo.TRACE_ID)) ? RequestUtils.getHeader(HeaderInfo.TRACE_ID) : uuid())
                    .servlet(RequestUtils.isServlet())
                    .appType(RequestUtils.getHeader(HeaderInfo.APP_TYPE))
                    .appVersion(RequestUtils.getHeader(HeaderInfo.APP_VERSION))
                    .clientIp(RequestUtils.getClientIp())
                    .serverIp(RequestUtils.getServerIp())
                    .tracingStage(TracingStage.OTHER)
                    .startTime(Instant.now());
        }

        /**
         * 将子线程的初始上下文值设置为 null
         * ----------------------------------------------------------------------
         * 关闭父子线程之间的继承关系，为什么要关闭继承关系？
         * 1. 在线程池的场景下会触发父线程已经remove掉上下文值，子线程还持有从父线程继承的上下文值，子线程结束后会将线程归还给线程池，归还后线程有可能会被复用，
         * 这样就可能会导致一部分值一直无法被GC收回，如果复用的数量过多可能导致 OOM，而且还有可能导致其它线程拿到了本不属于当前线程的数据；
         * ----------------------------------------------------------------------
         *
         * @param parentValue 父线程的值对象
         * @return 子线程的初始值对象
         * @see <a href="https://github.com/alibaba/transmittable-thread-local/issues/521">...</a>
         */
        @Override
        protected TracingHolder childValue(TracingHolder parentValue) {
            return super.initialValue();// 调用父类的初始化方法可以确保子类初始化为 null
        }
    };

    /**
     * 设置当前线程持有的数据源
     *
     * @param holder 上下文对象
     */
    public static void bind(TracingHolder holder) {
        CONTEXT.set(holder);
    }

    /**
     * 获取当前线程持有的数据源
     *
     * @return 上下文对象
     */
    public static TracingHolder current() {
        return CONTEXT.get();
    }

    /**
     * 是否移除上下文中文存储的值
     *
     * @param servlet 是否 Servlet 上下文
     */
    public static void unbind(boolean servlet) {
        if (servlet)
            CONTEXT.remove();
    }

    /**
     * 如果当前上下文是非 Servlet 上下文场景才会移除上下文中存储的数据
     */
    public static void unbind() {
        if (!current().isServlet())
            CONTEXT.remove();
    }

    /**
     * 系统唯一标识
     */
    private static String systemNumber = null;

    /**
     * 获取系统标识
     *
     * @return 系统标识
     */
    public static String getSystemNumber() {
        return systemNumber;
    }

    public static void setSystemNumber(String systemNumber) {
        LocalContextHolder.systemNumber = systemNumber;
    }

    public static String uuid() {
        return new AlternativeJdkIdGenerator().generateId().toString();
    }

}
