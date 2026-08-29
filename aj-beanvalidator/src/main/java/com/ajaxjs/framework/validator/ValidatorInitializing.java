package com.ajaxjs.framework.validator;

import org.springframework.validation.Validator;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 由 Starter 自动配置在 MVC 初始化时安装验证器与路径变量解析器。
 */
public class ValidatorInitializing {
    /**
     * 在 MVC 适配器中安装对象校验器及路径变量校验解析器。
     *
     * @param adapter    Spring MVC 请求映射适配器
     * @param properties 校验组件配置属性
     * @param rules      应用注册的扩展校验规则
     * @throws NullPointerException MVC 适配器尚未初始化参数解析器时抛出
     */
    public void initialize(RequestMappingHandlerAdapter adapter, ValidatorProperties properties, List<ValidatorRule> rules) {
        ConfigurableWebBindingInitializer init = (ConfigurableWebBindingInitializer) adapter.getWebBindingInitializer();
        // 若 Spring 已配置 Jakarta Validation Provider，优先交由它执行标准约束；
        // 否则 ValidatorImpl 使用内置的轻量实现处理受支持的标准注解。
        Validator defaultValidator = init.getValidator();
        if (!(defaultValidator instanceof ValidatorImpl))
            init.setValidator(new ValidatorImpl(defaultValidator, properties, rules));

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        List<HandlerMethodArgumentResolver> originalResolvers = Objects.requireNonNull(adapter.getArgumentResolvers());

        for (HandlerMethodArgumentResolver r : originalResolvers) {
            if (!r.getClass().equals(PathVariableMethodArgumentResolver.class)
                    && !(r instanceof ValidatorPathVariableMethodArgumentResolver))
                resolvers.add(r);
        }

        // 路径变量时进行参数验证
        resolvers.add(0, new ValidatorPathVariableMethodArgumentResolver(properties, rules));

        adapter.setArgumentResolvers(resolvers);
    }
}
