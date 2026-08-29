package com.ajaxjs.framework.validator;

import com.ajaxjs.framework.validator.custom.IdCard;
import com.ajaxjs.framework.validator.custom.Chinese;
import com.ajaxjs.framework.validator.custom.HttpUrl;
import com.ajaxjs.framework.validator.custom.Ipv4;
import com.ajaxjs.framework.validator.custom.MobileNo;
import com.ajaxjs.framework.validator.custom.Password;
import com.ajaxjs.framework.validator.custom.Username;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DirectFieldBindingResult;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestValidatorImpl {
    @Test
    void supportsAnyTargetType() {
        assertTrue(new ValidatorImpl().supports(Object.class));
    }

    @Test
    void validatesCustomAnnotatedFields() {
        ValidatorImpl validator = new ValidatorImpl();
        DirectFieldBindingResult errors = new DirectFieldBindingResult(new ValidIdCardBean(), "bean");
        assertDoesNotThrow(() -> validator.validate(errors.getTarget(), errors));

        DirectFieldBindingResult invalidErrors = new DirectFieldBindingResult(new InvalidIdCardBean(), "bean");
        assertDoesNotThrow(() -> validator.validate(invalidErrors.getTarget(), invalidErrors));
        assertTrue(invalidErrors.hasFieldErrors("idCard"));
    }

    @Test
    void validatesAdditionalCustomAnnotationsAndSkipsOptionalValues() {
        ValidatorImpl validator = new ValidatorImpl();
        DirectFieldBindingResult validErrors = new DirectFieldBindingResult(new ValidCustomBean(), "bean");
        validator.validate(validErrors.getTarget(), validErrors);
        assertFalse(validErrors.hasErrors());

        DirectFieldBindingResult invalidErrors = new DirectFieldBindingResult(new InvalidCustomBean(), "bean");
        validator.validate(invalidErrors.getTarget(), invalidErrors);
        assertTrue(invalidErrors.hasFieldErrors("mobile"));
        assertTrue(invalidErrors.hasFieldErrors("username"));
        assertTrue(invalidErrors.hasFieldErrors("password"));
        assertTrue(invalidErrors.hasFieldErrors("chinese"));
        assertTrue(invalidErrors.hasFieldErrors("ip"));
        assertTrue(invalidErrors.hasFieldErrors("url"));
    }

    @Test
    void resolvesCustomAnnotationAndMessage() throws NoSuchFieldException {
        ValidatorImpl validator = new ValidatorImpl();
        Field field = InvalidIdCardBean.class.getDeclaredField("idCard");
        Annotation annotation = field.getAnnotation(IdCard.class);

        assertEquals("身份证号格式不正确", validator.getValue(annotation));
        assertThrows(ValidatorException.class,
                () -> validator.resolveAnnotations(field.getAnnotations(), "invalid", "idCard"));
        assertDoesNotThrow(() -> validator.resolveAnnotations(new Annotation[0], "value", "field"));
    }

    @Test
    void failsFastForInvalidAnnotationConfiguration() {
        DirectFieldBindingResult errors = new DirectFieldBindingResult(new MisconfiguredIdCardBean(), "bean");

        assertThrows(ValidatorConfigurationException.class,
                () -> new ValidatorImpl().validate(errors.getTarget(), errors));
        assertFalse(errors.hasErrors());
    }

    @Test
    void flattensNestedMaps() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("message", "invalid");
        Map<String, Object> source = new HashMap<>();
        source.put("validation", nested);

        assertEquals("invalid", ValidatorImpl.flattenMap(source).get("validation.message"));

        Map<String, Object> result = new HashMap<>();
        ValidatorImpl.flattenMapHelper(source, "root", result);
        assertEquals("invalid", result.get("root.validation.message"));
        assertFalse(result.isEmpty());
    }

    @Test
    void validatesApplicationDefinedRule() {
        ValidatorImpl validator = new ValidatorImpl(new ValidatorProperties(),
                Collections.<ValidatorRule>singletonList(new TenantCodeRule()));
        DirectFieldBindingResult validErrors = new DirectFieldBindingResult(new ValidTenantBean(), "bean");
        validator.validate(validErrors.getTarget(), validErrors);
        assertFalse(validErrors.hasErrors());

        DirectFieldBindingResult invalidErrors = new DirectFieldBindingResult(new InvalidTenantBean(), "bean");
        validator.validate(invalidErrors.getTarget(), invalidErrors);
        assertTrue(invalidErrors.hasFieldErrors("tenantCode"));
    }

    private static class ValidIdCardBean {
        @IdCard
        private String idCard = "11010519491231002X";
    }

    private static class InvalidIdCardBean {
        @IdCard
        private String idCard = "invalid";
    }

    private static class ValidCustomBean {
        @MobileNo
        private String mobile = "13812345678";

        @Username
        private String username = "tester_1";

        @Password
        private String password = "Aa123456#";

        @Chinese
        private String chinese = "中文";

        @Ipv4
        private String ip = "192.168.1.1";

        @HttpUrl
        private String url = "https://example.com";

        @MobileNo(required = false)
        private String optionalMobile;

        @IdCard(required = false)
        private String optionalIdCard;
    }

    private static class InvalidCustomBean {
        @MobileNo
        private String mobile = "123";

        @Username
        private String username = "name";

        @Password
        private String password = "12345678";

        @Chinese
        private String chinese = "Chinese";

        @Ipv4
        private String ip = "192.168.1.256";

        @HttpUrl
        private String url = "example.com";
    }

    private static class MisconfiguredIdCardBean {
        @IdCard(message = " ")
        private String idCard = "invalid";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TenantCode {
        String message() default "租户编码不正确";
    }

    static class TenantCodeRule implements ValidatorRule {
        @Override
        public boolean supports(Annotation annotation) {
            return annotation.annotationType() == TenantCode.class;
        }

        @Override
        public void validate(Annotation annotation, Object value, String fieldName) {
            TenantCode tenantCode = (TenantCode) annotation;
            if (!(value instanceof String) || !((String) value).matches("TENANT-\\d+"))
                throw new ValidatorException(fieldName + " " + tenantCode.message());
        }
    }

    private static class ValidTenantBean {
        @TenantCode
        private String tenantCode = "TENANT-100";
    }

    private static class InvalidTenantBean {
        @TenantCode
        private String tenantCode = "invalid";
    }
}
