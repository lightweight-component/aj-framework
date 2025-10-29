package com.ajaxjs.framework.fileupload.policy;

/**
 * After uploading the file, the system should show the URL of the uploaded file so that it can be accessed.
 * This is the policy for how to show the URL.
 * It'll affect the value of `FileUploadConfig.urlPrefix` finally.
 */
public class ShowUrlPolicy {
    public enum Policy {
        /**
         * The URL of the uploaded file is provided by the service itself.
         */
        SERVICE_SELF,

        /**
         * Another web server provides the URL of the uploaded file, like Nginx or S3 file service.
         */
        OTHER_WEB_SERVER,

        /**
         * Do not show the URL of the uploaded file.
         */
        NOT_SHOW
    }

    public static String concatTwoUrl(String baseUrl, String subPath) {
        if (!baseUrl.endsWith("/"))// Ensure base ends with `/`
            baseUrl += "/";


        if (subPath.startsWith("/"))// Ensure subPath doesn't start with `/`
            subPath = subPath.substring(1);

        return baseUrl + subPath;
    }
}
