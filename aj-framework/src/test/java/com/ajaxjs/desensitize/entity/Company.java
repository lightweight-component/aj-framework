package com.ajaxjs.desensitize.entity;

import com.ajaxjs.desensitize.annotation.DesensitizeModel;
import com.ajaxjs.desensitize.annotation.DesensitizeProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Company {
    private Long id;
    private String name;
    private String address;
    private Worker worker;
    private List<Worker> list;
    private Map<String, Worker> dataMap;

    @DesensitizeModel
    @Data
    public static class Worker {
        private Long id;

        @DesensitizeProperty
        private String name;
    }
}
