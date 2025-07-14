package com.ajaxjs.business.web.rate_limiter.ratelimiter2.config;

/**
 * 配置资源, 配置来源可以是本地文件、数据库、分布式配置
 */
public interface RuleConfigSource {
    RuleConfig load();
}