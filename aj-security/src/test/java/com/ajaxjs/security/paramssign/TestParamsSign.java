package com.ajaxjs.security.paramssign;

import com.ajaxjs.util.ObjectHelper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Represents the test params sign component.
 */
class TestParamsSign {
    @Test
    void test() {
        Map<String, Object> params = ObjectHelper.mapOf("foo", 2, "bar", "bar2", "ccc", true);
        String secret = "A123456";

        ParamsSign paramsSign = new ParamsSign();
        paramsSign.setSecretKey(secret);

        String sign = paramsSign.sign(params);

        System.out.println(sign);
    }

    @Test
    void test2() {
        Map<String, Object> params = ObjectHelper.mapOf("name", "jack");
        String secret = "der3@x7Az42";

        ParamsSign paramsSign = new ParamsSign();
        paramsSign.setSecretKey(secret);

        String sign = paramsSign.sign(params);

        System.out.println(sign);
    }

    @Test
    void testSortUsesEncodedKeyOrder() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("z key", "last value");
        values.put("a", "first");

        assertEquals("a=first&z+key=last+value", ParamsSign.sort(values));
    }

}
