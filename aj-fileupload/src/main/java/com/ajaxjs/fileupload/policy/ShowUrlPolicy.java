package com.ajaxjs.fileupload.policy;

/**
 * URL display policy and URL concatenation utility for upload results.
 */
public class ShowUrlPolicy {
    /**
     * How the upload result URL is provided.
     */
    public enum Policy {
        /**
         * File access URL is provided by the current service.
         */
        SERVICE_SELF,

        /**
         * URL is provided by other services such as Nginx, object storage, etc.
         */
        OTHER_WEB_SERVER,

        /**
         * Do not return the file URL.
         */
        NOT_SHOW
    }

    /**
     * Concatenate URL prefix and relative path with a single slash.
     *
     * @param baseUrl URL prefix, must not be {@code null}
     * @param subPath Relative path, must not be {@code null}
     * @return The concatenated URL
     */
    public static String concatTwoUrl(String baseUrl, String subPath) {
        if (!baseUrl.endsWith("/"))// Ensure base ends with `/`
            baseUrl += "/";


        if (subPath.startsWith("/"))// Ensure subPath doesn't start with `/`
            subPath = subPath.substring(1);

        return baseUrl + subPath;
    }
}
