package com.ajaxjs.util.log;

import org.slf4j.MDC;

public class Trace {
    /**
     * Key for trace ID in logs
     */
    public final static String TRACE_KEY = "traceId";

    /**
     * Key for business action in logs
     */
    public final static String BIZ_ACTION = "bizAction";

    /**
     * 允许日志节流
     * 对于同一个 bizAction，如果连续出现超过 N 次（例如 3 次），则后续相同 bizAction 的日志不再打印；直到出现其他 bizAction，计数重新开始。
     */
    public final static String ENABLE_LOG_THROTTLING = "ENABLE_LOG_THROTTLING";

    public static final String ENABLE_OPERATION_LOG = "ENABLE_OPERATION_LOG";

    public static void saveLogToMDC(String log) {
        if (MDC.get(ENABLE_OPERATION_LOG) != null) {
            String _log = MDC.get(ENABLE_OPERATION_LOG);
            _log += log;

            MDC.put(ENABLE_OPERATION_LOG, _log);
        }
    }
}
