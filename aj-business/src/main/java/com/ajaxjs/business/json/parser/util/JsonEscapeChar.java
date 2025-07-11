package com.ajaxjs.business.json.parser.util;

import java.util.HashMap;
import java.util.Map;

public class JsonEscapeChar {
    private static final Map<Character, Character> ESCAPE_CHARS =  new HashMap<>();

    static {
        ESCAPE_CHARS.put('b', '\b');
        ESCAPE_CHARS.put('f', '\f');
        ESCAPE_CHARS.put('n', '\n');
        ESCAPE_CHARS.put('r', '\r');
        ESCAPE_CHARS.put('t', '\t');
        ESCAPE_CHARS.put('"', '\"');
    }

    public static char fromLiteral(char c) {
        return ESCAPE_CHARS.getOrDefault(c, c);
    }
}
