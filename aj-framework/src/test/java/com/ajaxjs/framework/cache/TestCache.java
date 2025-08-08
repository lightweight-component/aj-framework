package com.ajaxjs.framework.cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = Config.class)
public class TestCache {
    @Autowired
    EmpService service;

    @Test
    void testGet() {
        AbstractCacheManager l;
        Emp emp1 = service.get("1");
        log.info("[第一次查询],emp1:{}", emp1);

        Emp emp2 = service.get("1");
        log.info("[第二次查询],emp2:{}", emp2);
    }
}
