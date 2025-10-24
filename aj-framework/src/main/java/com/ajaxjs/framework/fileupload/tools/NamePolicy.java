package com.ajaxjs.framework.fileupload.tools;

/**
 * How to name the uploaded file.
 */
public class NamePolicy {
    public enum Policy {
        /**
         * Use the original file name.
         */
        ORIGINAL,

        /**
         * Use a random name that appends after the original.
         */
        ORIGINAL_RANDOM,

        /**
         * Use a random name with the original file extension.
         */
        RANDOM
    }
}
