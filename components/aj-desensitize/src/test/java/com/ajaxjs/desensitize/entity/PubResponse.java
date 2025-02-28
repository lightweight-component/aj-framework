package com.ajaxjs.desensitize.entity;


import com.ajaxjs.desensitize.DesensitizeType;
import com.ajaxjs.desensitize.annotation.DesensitizeModel;
import com.ajaxjs.desensitize.annotation.DesensitizeProperty;

import java.util.List;
import java.util.Map;

@DesensitizeModel
public class PubResponse {
    @DesensitizeProperty(DesensitizeType.USERNAME)
    public String username;
    @DesensitizeProperty
    public String password;
    @DesensitizeProperty(DesensitizeType.EMAIL)
    public String email;
    @DesensitizeProperty(DesensitizeType.ID_CARD)
    public String idCard;
    @DesensitizeProperty(DesensitizeType.BANK_CARD)
    public String bankCard;
    @DesensitizeProperty(DesensitizeType.PHONE)
    public String phone;
    @DesensitizeProperty(DesensitizeType.PHONE)
    public String mobile;
    public Job job;
    public Map<String, Object> work;
    public List<Job> jobList;
    public Job[] jobs;

    @DesensitizeModel
    public static class Job {
        @DesensitizeProperty(DesensitizeType.DEFAULT)
        public String work;
        @DesensitizeProperty(DesensitizeType.EMAIL)
        public String email;

    }
}
