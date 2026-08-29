package com.ajaxjs.security.desensitize.entity;

import com.ajaxjs.security.desensitize.DesensitizeType;
import com.ajaxjs.security.desensitize.annotation.DesensitizeMapProperty;
import com.ajaxjs.security.desensitize.annotation.DesensitizeModel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 集合类型
 */
@DesensitizeModel
@Data
public class PeopleMap {
    private String username;
    private String password;
    private LocalDateTime localDateTime;

    /**
     * Stores the sub map map value.
     */
    private Map<String, SubMap> subMapMap = new HashMap<>();

    /**
     * Stores the params value.
     */
    @DesensitizeMapProperty(keys = {"password", "username"}, types = {DesensitizeType.DEFAULT, DesensitizeType.USERNAME})
    private Map<String, String> params = new HashMap<>();

    /**
     * Stores the ages value.
     */
    @DesensitizeMapProperty(keys = {"age", "username"}, types = {DesensitizeType.DEFAULT, DesensitizeType.USERNAME})
    private Map<Integer, String> ages = new HashMap<>();

    /**
     * Represents the sub map component.
     */
    @Data
    public static class SubMap {
        private String sub;
    }
}
