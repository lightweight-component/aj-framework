package com.ajaxjs.framework.validator;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;

import java.util.List;

/**
 * 为路径变量增加本组件自定义注解校验的解析器。
 */
class ValidatorPathVariableMethodArgumentResolver extends PathVariableMethodArgumentResolver {
    /**
     * 注解消息配置。
     */
    private final ValidatorProperties properties;

    /**
     * 应用注册的扩展校验规则。
     */
    private final List<ValidatorRule> rules;

    /**
     * 创建路径变量校验解析器。
     *
     * @param properties 校验组件配置属性
     * @param rules      应用注册的扩展校验规则
     */
    ValidatorPathVariableMethodArgumentResolver(ValidatorProperties properties, List<ValidatorRule> rules) {
        this.properties = properties;
        this.rules = rules;
    }

    /**
     * 解析路径变量后立即执行其声明的内置或扩展校验注解。
     *
     * @param name      路径变量名称
     * @param parameter 控制器方法参数
     * @param request   当前 Web 请求
     * @return 已解析且校验通过的路径变量值
     * @throws Exception 父解析器无法解析路径变量或校验失败时抛出
     */
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        Object value = super.resolveName(name, parameter, request);
        new ValidatorImpl(properties, rules).resolveAnnotations(parameter.getParameterAnnotations(), value, name);

        return value;
    }
}
