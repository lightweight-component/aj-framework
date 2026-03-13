package util;


import com.ajaxjs.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestJsonUtil {
    private String validJson;
    private String invalidJson;
    private String mismatchedJson;

    @Data
    static
    class User {
        private String name;
        private int age;

    }

    @BeforeEach
    void setUp() {
        validJson = "{\"name\":\"John\", \"age\":30}";
        invalidJson = "{\"name\":\"John\", \"age\":30"; // 缺少右大括号
        mismatchedJson = "{\"name\":123, \"age\":\"thirty\"}"; // 键值类型不匹配
    }

    @Test
    void fromJson_ValidJson_ReturnsObject() {
        User user = JsonUtil.fromJson(validJson, User.class);
        assertNotNull(user, "The returned object should not be null.");
        assertEquals("John", user.getName(), "The name should match.");
        assertEquals(30, user.getAge(), "The age should match.");
    }

    @Test
    void fromJson_InvalidJson_ThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> JsonUtil.fromJson(invalidJson, User.class));

        assertTrue(exception.getMessage().contains("Failed to convert JSON to object"),
                "Exception message should indicate JSON conversion failure.");
    }

    @Test
    void fromJson_MismatchedJson_ThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> JsonUtil.fromJson(mismatchedJson, User.class));

        assertTrue(exception.getMessage().contains("Failed to convert JSON to object"),
                "Exception message should indicate JSON conversion failure.");
    }

    @Data
    static class MyPojoWithDate {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private Date updateTime;
    }

    @Test // failed
    void testDate() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("name", "Test Device");
        sourceMap.put("createTime", LocalDateTime.of(2023, 10, 27, 10, 30, 0)); // Example LocalDateTime
        sourceMap.put("updateTime", LocalDateTime.now());

        MyPojoWithDate pojo = JsonUtil.map2pojo(sourceMap, MyPojoWithDate.class);
        System.out.println("Converted POJO: " + pojo);
    }
}
