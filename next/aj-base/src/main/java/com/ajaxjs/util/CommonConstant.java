package com.ajaxjs.util;

/**
 * Common Constants Interface
 * <p>
 * This interface defines commonly used constants throughout the application.
 * These constants provide reusable values that help maintain consistency
 * and avoid magic numbers/strings in the codebase.
 */
public interface CommonConstant {
    /**
     * Represents an empty string constant
     */
    String EMPTY_STRING = "";

    /**
     * Represents a hyphen character '-' (ASCII 45)
     */
    char HYPHEN = '-';

    /**
     * Represents a hyphen string "-"
     */
    String HYPHEN_STR = "-";

    /**
     * Represents a new line character '\n' (line feed)
     */
    char NEW_LINE = '\n';

    /**
     * Represents the UTF-8 character encoding string
     */
    String UTF8 = "UTF-8";

    /**
     * Represents the "class" string used in reflection operations
     */
    String CLASS = "class";
}