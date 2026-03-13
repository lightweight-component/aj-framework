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

    public static final String ENABLE_OPERATION_LOG = "ENABLE_OPERATION_LOG";

    public static void saveLogToMDC(String log) {
        if (MDC.get(ENABLE_OPERATION_LOG) != null) {
            String _log = MDC.get(ENABLE_OPERATION_LOG);
            _log += log;

            MDC.put(ENABLE_OPERATION_LOG, _log);
        }
    }
}
