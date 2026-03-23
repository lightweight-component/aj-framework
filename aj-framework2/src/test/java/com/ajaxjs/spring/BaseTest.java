package com.ajaxjs.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
public abstract class BaseTest {
    @BeforeEach
    void initAll() {
//        DataBaseConnection.initDb();
    }

    @AfterEach
    void closeDb() {
//        JdbcConnection.closeDb();
    }
}