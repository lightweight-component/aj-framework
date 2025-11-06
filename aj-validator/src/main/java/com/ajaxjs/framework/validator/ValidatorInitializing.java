package com.ajaxjs.framework.validator;

import com.ajaxjs.spring.DiContextUtil;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 在 Spring 应用程序上下文初始化完成后设置验证器和参数解析器
 * 这个类的作用是在 Spring MVC 启动时，拦截并修改 RequestMappingHandlerAdapter 的行为。通过设置自定义的验证器和参数解析器，可以对路径变量进行验证
 */
@Component
public class ValidatorInitializing {
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        RequestMappingHandlerAdapter adapter = DiContextUtil.getBean(RequestMappingHandlerAdapter.class);
        assert adapter != null;
        ConfigurableWebBindingInitializer init = (ConfigurableWebBindingInitializer) adapter.getWebBindingInitializer();
        assert init != null;
        init.setValidator(new ValidatorImpl());

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        List<HandlerMethodArgumentResolver> originalResolvers = Objects.requireNonNull(adapter.getArgumentResolvers());

        for (HandlerMethodArgumentResolver r : originalResolvers) {
            if (!r.getClass().equals(PathVariableMethodArgumentResolver.class))
                resolvers.add(r);
        }

        // 路径变量时进行参数验证
        resolvers.add(0, new PathVariableMethodArgumentResolver() {
            @Override
            protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
                Object value = super.resolveName(name, parameter, request);
                // validateIfApplicable
                new ValidatorImpl().resolveAnnotations(parameter.getParameterAnnotations(), value, name);

                return value;
            }
        });

        adapter.setArgumentResolvers(resolvers);
    }
}
