package com.ajaxjs.wechat;

import com.ajaxjs.sqlman.JdbcConnection;
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
        JdbcConnection.closeConnection();
    }
}