package com.ajaxjs.business.json.parser.util;

public interface CharPeekable {
    /**
     * get an element, but not consume it.
     */
    char peek();

    boolean hasNext();
}
