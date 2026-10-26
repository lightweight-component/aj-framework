package com.ajaxjs.security.antireptile.model;

import lombok.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * IP 访问频率规则的配置模型，对应 {@code anti.reptile.manager.ip-rule}。
 * <p>窗口和计数以请求 URI 与客户端 IP 组合区分；命中记录按 IP 保存。
 * 具体计数、白名单及锁定行为见 {@link IpRuleHandler}。</p>
 */
@Data
public class IpRule {
    /**
     * 是否注册内置 IP 规则 Bean，默认 true。
     */
    private boolean enabled = true;

    /**
     * 请求统计窗口长度，单位毫秒，默认 5000；窗口状态由本地缓存的有效期维护。
     */
    private Integer expirationTime = 5000;

    /**
     * 窗口内计数阈值，默认 20。当前实现首个请求只初始化窗口，后续递增值严格大于此阈值时命中。
     */
    private Integer requestMaxSize = 20;

    /**
     * IP 锁定时长，单位秒，默认 1 天（86400 秒），必须大于零；锁定从首次命中开始，期间请求不续期。
     */
    private long lockExpire = TimeUnit.DAYS.toSeconds(1);
    /**
     * IP 白名单列表。处理器移除每项末尾的星号后按字符串前缀匹配，无星号时也采用前缀匹配。
     */
    private List<String> ignoreIp;
}
