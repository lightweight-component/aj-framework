package com.ajaxjs.wechat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class BaseTest {
    @BeforeEach
    void initAll() {

    }

    @AfterEach
    void closeDb() {

    }
}