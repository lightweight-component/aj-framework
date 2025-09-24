package com.ajaxjs.memviz;

import com.ajaxjs.memviz.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class MemvizApplication {
    public static List<String> largeStrings = new ArrayList<>();

    public static void main(String[] args) {
        // Create 100 1MB strings and store them properly
        for (int i = 0; i < 100; i++) {
            String largeString = User.randomString(1024 * 1024); // 1MB each
            largeStrings.add(largeString); // Store so they won't be GC'd
        }

        User.createTestUsers(); // 创建测试User对象，验证深度大小计算

        SpringApplication.run(MemvizApplication.class, args);
    }
}