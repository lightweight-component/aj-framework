package com.ajaxjs.spring;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.AbstractEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class TestResourceScanner {
    @Test
    void test3SpringGetClasses() {
        try {
            Set<Class<?>> urls = ResourceScanner.getClasses("org.springframework.core", AbstractEncoder.class::isAssignableFrom);
            for (Class<?> url : urls) {
                log.info("class:{}", url);
            }

            log.info("class count:{}", urls.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testAjGetClasses() {
        try {
            Set<Class<?>> urls = ResourceScanner.getClasses("com.ajaxjs.sqlman");
            for (Class<?> url : urls) {
                log.info("class:{}", url);
            }

            log.info("class count:{}", urls.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
