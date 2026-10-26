package com.ajaxjs.security.antireptile.rule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 反爬规则的模板基类。
 * <p>将统一入口 {@link #execute(HttpServletRequest, HttpServletResponse)}
 * 委托给子类的 {@link #doExecute(HttpServletRequest, HttpServletResponse)}，
 * 当前不额外处理异常或维护状态，重置行为和优先级由子类实现。</p>
 */
public abstract class AbstractRule implements AntiReptileRule {
    /**
     * 将规则检查委托给子类实现，原样返回结果。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @return 子类返回的命中标记，{@code true} 表示应拦截
     */
    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) {
        return doExecute(request, response);
    }

    protected abstract boolean doExecute(HttpServletRequest request, HttpServletResponse response);
}
