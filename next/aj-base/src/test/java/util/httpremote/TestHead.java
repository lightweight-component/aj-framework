package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestHead {
    @Test
    void testRedirect() {
        Head redirect = new Head("https://httpbin.org/redirect-to?url=https://example.com");
        redirect.init();
        redirect.connect();

        assertEquals("https://example.com", redirect.get302redirect());
    }


    @Test
    void testIs404() {
        Head notFound = new Head("https://httpbin.org/status/404");
        notFound.init();
        notFound.connect();

        assertTrue(notFound.is404());
    }

    @Test
    void testGetFileSize() {
        Head getSize = new Head("https://cdn2.jianshu.io/assets/web/nav-logo-4c7bbafe27adc892f3046e6978459bac.png");
        getSize.init();
        getSize.connect();
        long size = getSize.getFileSize();

        assertEquals(1500L, size);

        Head getSize2 = new Head("https://httpbin.org/bytes/128");
        getSize2.init();
        getSize2.connect();

        assertEquals(128L, getSize2.getFileSize());
    }
}
