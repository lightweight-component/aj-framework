package com.ajaxjs.base;

import com.ajaxjs.framework.spring.PrintBanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BaseApp {
    public static void main(String[] args) {
        SpringApplication.run(BaseApp.class, args);
        PrintBanner.showOk("AJ-Base");
    }
}
