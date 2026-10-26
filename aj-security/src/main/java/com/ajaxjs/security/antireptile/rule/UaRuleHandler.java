package com.ajaxjs.security.antireptile.rule;

import com.ajaxjs.security.antireptile.config.AntiReptileProperties;
import com.ajaxjs.security.antireptile.model.UaRule;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 根据 User-Agent 中的操作系统与设备类型执行访问限制。
 * <p>先拒绝无法识别的设备或系统，再按配置检查 Linux、移动设备、PC、物联网设备
 * 和代理类型。该规则不维护请求状态，验证后的重置为空操作，后续请求仍会重新检查请求头。</p>
 */
@Slf4j
public class UaRuleHandler extends AbstractRule {
    /**
     * 组件配置，提供当前规则的参数。
     */
    @Autowired
    private AntiReptileProperties properties;

    /**
     * 解析 User-Agent 并按配置检查设备和操作系统类别。
     * <p>移动设备包含手机和平板；物联网类别包含 DMR、游戏机和可穿戴设备。
     * 任一检查不通过即返回命中，且不会继续后续类别检查。</p>
     *
     * @param request  携带 User-Agent 请求头的请求
     * @param response 当前响应，此实现不直接写入响应
     * @return 未知设备、未知系统或配置禁止的类别为 {@code true}，其余为 {@code false}
     */
    @Override
    protected boolean doExecute(HttpServletRequest request, HttpServletResponse response) {
        UaRule uaRule = properties.getUaRule();
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        OperatingSystem os = userAgent.getOperatingSystem();
        OperatingSystem osGroup = userAgent.getOperatingSystem().getGroup();
        DeviceType deviceType = userAgent.getOperatingSystem().getDeviceType();

        if (DeviceType.UNKNOWN.equals(deviceType)) {
            log.info("Intercepted request, uri: {} Unknown device, User-Agent: {}", request.getRequestURI(), userAgent);

            return true;
        } else if (OperatingSystem.UNKNOWN.equals(os) || OperatingSystem.UNKNOWN_MOBILE.equals(os) || OperatingSystem.UNKNOWN_TABLET.equals(os)) {
            log.info("Intercepted request, uri: {} Unknown OperatingSystem, User-Agent: {}", request.getRequestURI(), userAgent);
            return true;
        }

        if (!uaRule.isAllowedLinux() && (OperatingSystem.LINUX.equals(osGroup) || OperatingSystem.LINUX.equals(os))) {
            log.info("Intercepted request, uri: {} Not Allowed Linux request, User-Agent: {}", request.getRequestURI(), userAgent);

            return true;
        }

        if (!uaRule.isAllowedMobile() && (DeviceType.MOBILE.equals(deviceType) || DeviceType.TABLET.equals(deviceType))) {
            log.info("Intercepted request, uri: {} Not Allowed Mobile Device request, User-Agent: {}", request.getRequestURI(), userAgent);

            return true;
        }

        if (!uaRule.isAllowedPc() && DeviceType.COMPUTER.equals(deviceType)) {
            log.info("Intercepted request, uri: {} Not Allowed PC request, User-Agent: {}", request.getRequestURI(), userAgent);
            return true;
        }

        if (!uaRule.isAllowedIot() && (DeviceType.DMR.equals(deviceType) || DeviceType.GAME_CONSOLE.equals(deviceType) || DeviceType.WEARABLE.equals(deviceType))) {
            log.info("Intercepted request, uri: {} Not Allowed Iot Device request, User-Agent: {}", request.getRequestURI(), userAgent);
            return true;
        }

        if (!uaRule.isAllowedProxy() && OperatingSystem.PROXY.equals(os)) {
            log.info("Intercepted request, uri: {} Not Allowed Proxy request, User-Agent: {}", request.getRequestURI(), userAgent);
            return true;
        }

        return false;
    }

    /**
     * 处理验证成功后的重置通知。此规则不保存状态，因此不执行任何操作。
     *
     * @param request        验证码提交请求
     * @param realRequestUri 原始业务 URI，此实现不使用
     */
    @Override
    public void reset(HttpServletRequest request, String realRequestUri) {
    }

    /**
     * 指定内置 User-Agent 规则的执行优先级。
     *
     * @return {@code 1}，默认在 IP 规则之后执行
     */
    @Override
    public int getOrder() {
        return 1;
    }
}
