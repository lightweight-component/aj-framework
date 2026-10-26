package com.ajaxjs.security.antireptile.rule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 可扩展的反爬规则契约。
 * <p>实现类注册为 Spring Bean 后，由自动配置按 {@link #getOrder()} 升序装配。
 * 执行返回 {@code true} 表示命中规则，执行器立即拒绝请求；验证码校验成功后，
 * 执行器调用全部规则的 {@link #reset(HttpServletRequest, String)}。</p>
 */
public interface AntiReptileRule {
    /**
     * 检查当前请求是否命中本规则，可同时更新该规则维护的状态。
     *
     * @param request  当前业务请求
     * @param response 当前响应，供规则实现按需使用
     * @return 命中并应阻止请求时为 {@code true}，未命中时为 {@code false}
     */
    boolean execute(HttpServletRequest request, HttpServletResponse response);

    /**
     * 在验证码校验通过后重置相关规则状态。
     *
     * @param request        提交验证码的请求，用于提取客户端身份
     * @param realRequestUri 被拦截的原始业务 URI，并非验证码接口 URI
     */
    void reset(HttpServletRequest request, String realRequestUri);

    /**
     * 返回规则在自动配置装配时使用的执行优先级。
     *
     * @return 优先级数值，数值越小越先执行；相同数值不提供额外的先后约定
     */
    int getOrder();
}
