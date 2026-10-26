package com.ajaxjs.security.antireptile.config;

import com.ajaxjs.security.antireptile.AntiReptile;
import com.ajaxjs.security.antireptile.model.IpRule;
import com.ajaxjs.security.antireptile.model.UaRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 绑定 {@code anti.reptile.manager} 前缀的反爬配置。
 * <p>配置包含组件开关、受保护接口范围及两种内置规则。未启用全局过滤时，
 * URI 列表和方法上的 {@link AntiReptile} 注解共同决定检查范围。</p>
 */
@Data
@ConfigurationProperties(prefix = "anti.reptile.manager")
public class AntiReptileProperties {
    /**
     * 组件启用开关，默认 false；只有显式配置为 true 时才加载主自动配置。
     */
    private boolean enabled;

    /** 本地缓存最大条目数，默认 10000；容量不足时 LRU 淘汰，可能提前清除计数、锁定或验证码。 */
    private int localCacheCapacity = 10000;

    /**
     * 全局过滤开关，默认 false；为 true 时所有控制器方法均参与规则检查。
     */
    private boolean globalFilterMode = false;

    /**
     * 非全局模式下需要保护的 URI 列表。支持精确值与 Java 正则表达式；正则对完整请求 URI 匹配。
     */
    private List<String> includeUrls;

    /**
     * IP 访问频率配置，默认创建配置对象并启用规则。
     */
    private IpRule ipRule = new IpRule();

    /**
     * User-Agent 设备与系统检查配置，默认创建配置对象并启用规则。
     */
    private UaRule uaRule = new UaRule();
}
