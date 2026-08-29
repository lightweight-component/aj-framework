package com.ajaxjs.framework.validator;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

/**
 * Bean Validator 的 Spring Boot MVC 自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(RequestMappingHandlerAdapter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "ajaxjs.beanvalidator", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ValidatorProperties.class)
public class ValidatorAutoConfiguration {
    /**
     * 创建并执行 MVC 校验组件初始化。
     *
     * @param adapter    Spring MVC 请求映射适配器
     * @param properties 校验组件配置属性
     * @param rules      应用提供的扩展校验规则
     * @return 已完成初始化的组件实例
     */
    @Bean
    ValidatorInitializing validatorInitializing(RequestMappingHandlerAdapter adapter, ValidatorProperties properties,
                                                List<ValidatorRule> rules) {
        ValidatorInitializing initializing = new ValidatorInitializing();
        initializing.initialize(adapter, properties, rules);

        return initializing;
    }
}
