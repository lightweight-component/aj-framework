package com.ajaxjs.security.antireptile.model;

import lombok.Data;

/**
 * User-Agent 规则的配置模型，对应 {@code anti.reptile.manager.ua-rule}。
 * <p>处理器根据请求头解析出的系统和设备类别判断是否允许访问。
 * 未知设备或未知操作系统始终命中规则，不受下列允许开关控制。</p>
 */
@Data
public class UaRule {
    /**
     * 是否注册内置 User-Agent 规则 Bean，默认 true。
     */
    private boolean enabled = true;

    /**
     * 是否允许被识别为 Linux 系统或 Linux 系统组的请求，默认 false。
     */
    private boolean allowedLinux = false;

    /**
     * 是否允许手机和平板设备，默认 true；仍需通过系统类别等其他检查。
     */
    private boolean allowedMobile = true;

    /**
     * 是否允许 COMPUTER 类别的 PC 设备，默认 true。
     */
    private boolean allowedPc = true;

    /**
     * 是否允许 DMR、游戏机和可穿戴设备，默认 false。
     */
    private boolean allowedIot = false;

    /**
     * 是否允许操作系统类型被识别为 PROXY 的请求，默认 false；不是对网络代理连接的直接检测。
     */
    private boolean allowedProxy = false;
}
