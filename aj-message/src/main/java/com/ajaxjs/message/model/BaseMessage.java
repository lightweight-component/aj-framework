package com.ajaxjs.message.model;

import lombok.Data;

@Data
public abstract class BaseMessage {
    /**
     * 请求编号（幂等）
     */
    private String requestNo;


    /**
     * 指定 clientId 发送
     */
    private String clientId;
}
