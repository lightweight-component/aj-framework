package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class AjFrameworkSampleApp {
    public static void main(String[] args) {
        SpringApplication.run(AjFrameworkSampleApp.class, args);
        log.warn("okoko----------------cbvcbv---");
    }
}
