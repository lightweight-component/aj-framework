package com.ajaxjs.framework.validator;

import com.ajaxjs.framework.validator.custom.IdCard;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.annotation.*;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

class TestValidatorInitializing {
    @Test
    void canDisableAutoConfiguration() {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("test", Collections.singletonMap("ajaxjs.beanvalidator.enabled", "false")));
            context.register(WebConfig.class, ValidatorAutoConfiguration.class);
            context.refresh();

            assertFalse(context.containsBean("validatorInitializing"));
        }
    }

    @Test
    void configuresMvcValidatorAndValidatesPathVariables() throws Exception {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("test", Collections.singletonMap(
                            "ajaxjs.beanvalidator.messages.id-card", "自定义身份证错误")));
            context.register(WebConfig.class, ValidatorAutoConfiguration.class);
            context.refresh();

            RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);
            ConfigurableWebBindingInitializer initializer = (ConfigurableWebBindingInitializer) adapter.getWebBindingInitializer();
            assertInstanceOf(ValidatorImpl.class, initializer.getValidator());

            long resolverCount = adapter.getArgumentResolvers().stream()
                    .filter(ValidatorPathVariableMethodArgumentResolver.class::isInstance)
                    .count();
            assertEquals(1, resolverCount);
            assertEquals("自定义身份证错误", context.getBean(ValidatorProperties.class).getMessages().get("id-card"));

            MockMvc mvc = webAppContextSetup(context).build();
            mvc.perform(get("/cards/11010519491231002X"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("11010519491231002X"));
            assertThrows(ServletException.class, () -> mvc.perform(get("/cards/invalid")));
            mvc.perform(get("/tenants/TENANT-100"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("TENANT-100"));
            assertThrows(ServletException.class, () -> mvc.perform(get("/tenants/invalid")));
            mvc.perform(post("/cards")
                            .contentType(APPLICATION_JSON)
                            .content("{\"idCard\":\"invalid\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class WebConfig {
        @Bean
        PathController pathController() {
            return new PathController();
        }

        @Bean
        ValidatorRule tenantCodeRule() {
            return new TenantCodeRule();
        }
    }

    @RestController
    static class PathController {
        @GetMapping("/cards/{idCard}")
        String getCard(@PathVariable("idCard") @IdCard String idCard) {
            return idCard;
        }

        @GetMapping("/tenants/{tenantCode}")
        String getTenant(@PathVariable("tenantCode") @TenantCode String tenantCode) {
            return tenantCode;
        }

        @PostMapping("/cards")
        String createCard(@Validated @RequestBody CardRequest request) {
            return request.getIdCard();
        }
    }

    static class CardRequest {
        @IdCard
        private String idCard;

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface TenantCode {
    }

    static class TenantCodeRule implements ValidatorRule {
        @Override
        public boolean supports(Annotation annotation) {
            return annotation.annotationType() == TenantCode.class;
        }

        @Override
        public void validate(Annotation annotation, Object value, String fieldName) {
            if (!(value instanceof String) || !((String) value).matches("TENANT-\\d+"))
                throw new ValidatorException(fieldName + " 租户编码不正确");
        }
    }
}
