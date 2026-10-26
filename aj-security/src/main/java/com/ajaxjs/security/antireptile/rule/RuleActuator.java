package com.ajaxjs.security.antireptile.rule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 按配置顺序执行反爬规则，并在人工验证通过后统一重置规则状态。
 * <p>执行器本身不排序：自动配置负责按优先级升序传入规则。
 * 检查遇到首个命中即短路返回，因此后续规则不会处理该次请求；重置则遍历全部规则。</p>
 */
@RequiredArgsConstructor
public class RuleActuator {
    /**
     * 按调用方指定顺序执行的规则列表，自动配置按优先级升序传入；列表及元素均不能为 {@code null}，允许空列表。
     */
    private final List<AntiReptileRule> ruleList;

    /**
     * 依次执行规则，遇到首个命中立即停止。
     *
     * @param request  待检查的业务请求
     * @param response 当前响应，传递给各规则
     * @return 全部规则均未命中或规则列表为空时为 {@code true}，任一命中时为 {@code false}
     */
    public boolean isAllowed(HttpServletRequest request, HttpServletResponse response) {
        for (AntiReptileRule rule : ruleList) {
            if (rule.execute(request, response))
                return false;
        }

        return true;
    }

    /**
     * 通知全部规则重置原始业务请求对应的状态。
     * <p>不因某条规则曾未执行而跳过它；规则抛出的异常会中断遍历并向调用方传播。</p>
     *
     * @param request        提交正确验证码的请求
     * @param realRequestUri 原先被拦截的业务 URI
     */
    public void reset(HttpServletRequest request, String realRequestUri) {
        ruleList.forEach(rule -> rule.reset(request, realRequestUri));
    }
}
