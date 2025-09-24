package com.ajaxjs.firewall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class FirewallApplication {
    public static void main(String[] args) {
        SpringApplication.run(FirewallApplication.class, args);
        System.out.println("");
        System.out.println("🔥 ========================================== 🔥");
        System.out.println("🔥  Spring Boot API 防火墙启动成功！        🔥");
        System.out.println("🔥  管理控制台: http://localhost:8080       🔥");
        System.out.println("🔥  H2数据库控制台: http://localhost:8080/h2-console 🔥");
        System.out.println("🔥 ========================================== 🔥");
        System.out.println("");
    }
}