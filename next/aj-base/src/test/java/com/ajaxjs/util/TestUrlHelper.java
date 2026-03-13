package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static com.ajaxjs.util.UrlHelper.concatUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestUrlHelper {
    @Test
    @SuppressWarnings("SpellCheckingInspection")
    void testConcatUrl() {
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa/", "/bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa", "bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa/", "bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa", "/bbb/sds"));
    }
}
