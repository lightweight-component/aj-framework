package com.ajaxjs.business.json.parser.util;

import java.util.NoSuchElementException;

public class TextCharIterator implements CharPeekable {
    private final char[] chars;
    private int readIndex = 0;

    public TextCharIterator(String text) {
        chars = text.toCharArray();
    }

    @Override
    public boolean hasNext() {
        return readIndex < chars.length;
    }

    public char next() {
        if (!hasNext())
            throw new NoSuchElementException();

        return chars[readIndex++];
    }

    @Override
    public char peek() {
        return chars[readIndex];
    }
}
