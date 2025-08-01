package com.ajaxjs.base;

import com.ajaxjs.framework.spring.PrintBanner;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BaseApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApp.class);
    public static void main(String[] args) {
        LOGGER.info("测试反馈info");
        LOGGER.debug("测试反馈debug");
        LOGGER.warn("测试反馈warn");
        LOGGER.error("测试反馈error");
        SpringApplication.run(BaseApp.class, args);
        PrintBanner.showOk("AJ-Base");
    }
}
