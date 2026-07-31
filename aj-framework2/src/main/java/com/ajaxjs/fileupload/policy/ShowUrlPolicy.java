package com.ajaxjs.fileupload.policy;

/**
 * 上传结果 URL 的展示策略及 URL 拼接工具。
 */
public class ShowUrlPolicy {
    /** 上传结果 URL 的提供方式。 */
    public enum Policy {
        /**
         * 由当前服务提供文件访问 URL。
         */
        SERVICE_SELF,

        /**
         * 由 Nginx、对象存储等其他服务提供 URL。
         */
        OTHER_WEB_SERVER,

        /**
         * 不返回文件 URL。
         */
        NOT_SHOW
    }

    /**
     * 使用单个斜杠连接 URL 前缀和相对路径。
     *
     * @param baseUrl URL 前缀，不得为 {@code null}
     * @param subPath 相对路径，不得为 {@code null}
     * @return 拼接后的 URL
     */
    public static String concatTwoUrl(String baseUrl, String subPath) {
        if (!baseUrl.endsWith("/"))// Ensure base ends with `/`
            baseUrl += "/";


        if (subPath.startsWith("/"))// Ensure subPath doesn't start with `/`
            subPath = subPath.substring(1);

        return baseUrl + subPath;
    }
}
