package com.ajaxjs.message.model.config;

import lombok.Data;

/**
 * 配置基类
 **/
@Data
public abstract class Config {
    private long configId;

    private boolean defaultFlag;

    private String configName;
}
