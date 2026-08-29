package com.ajaxjs.framework.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

/**
 * Spring {@link Validator} 实现，负责执行本组件内置及扩展的校验规则。
 */
@Slf4j
public class ValidatorImpl implements Validator {
    /**
     * Spring 已注册的校验器，存在时先执行。
     */
    private final Validator delegate;
    /**
     * 应用注册的扩展校验规则。
     */
    private final List<ValidatorRule> rules;

    /**
     * 校验本组件的自定义注解。
     */
    public ValidatorImpl() {
        this(null, new ValidatorProperties(), Collections.<ValidatorRule>emptyList());
    }

    /**
     * 使用 Spring 已注册的校验器创建组合校验器。
     *
     * @param delegate Spring 原有的 Bean Validation 适配器，如果存在则先执行。
     */
    public ValidatorImpl(Validator delegate) {
        this(delegate, new ValidatorProperties(), Collections.<ValidatorRule>emptyList());
    }

    /**
     * 使用 Spring 校验器和本组件配置创建校验器。
     *
     * @param delegate   Spring 原有的 Bean Validation 适配器
     * @param properties 校验组件配置属性
     */
    public ValidatorImpl(Validator delegate, ValidatorProperties properties) {
        this(delegate, properties, Collections.<ValidatorRule>emptyList());
    }

    /**
     * 使用完整依赖创建校验器。
     *
     * @param delegate   Spring 原有的 Bean Validation 适配器，可为 {@code null}
     * @param properties 校验组件配置属性
     * @param rules      应用注册的扩展校验规则
     */
    public ValidatorImpl(Validator delegate, ValidatorProperties properties, List<ValidatorRule> rules) {
        this.delegate = delegate;
        this.properties = properties;
        this.rules = rules;
    }

    /**
     * 使用指定配置创建内置校验器。
     *
     * @param properties 校验组件配置属性
     */
    public ValidatorImpl(ValidatorProperties properties) {
        this(null, properties, Collections.<ValidatorRule>emptyList());
    }

    /**
     * 使用指定配置和扩展规则创建内置校验器。
     *
     * @param properties 校验组件配置属性
     * @param rules      应用注册的扩展校验规则
     */
    public ValidatorImpl(ValidatorProperties properties, List<ValidatorRule> rules) {
        this(null, properties, rules);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    /**
     * 先委托 Spring 已注册校验器，再将本组件校验失败写入字段错误集合。
     *
     * @param target 待校验的对象
     * @param errors 用于收集字段校验错误的容器
     * @throws ValidatorException 校验器无法访问字段或注解配置不正确时抛出
     */
    @Override
    public void validate(Object target, Errors errors) {
        if (delegate != null)
            delegate.validate(target, errors);

        Field[] declaredFields = target.getClass().getDeclaredFields();

        try {
            for (Field field : declaredFields) {
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {// isPrivate
                    field.setAccessible(true);
                    try {
                        resolveAnnotations(field.getDeclaredAnnotations(), field.get(target), field.getName());
                    } catch (ValidatorConfigurationException e) {
                        throw e;
                    } catch (ValidatorException e) {
                        errors.rejectValue(field.getName(), "validation", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Validator error", e);

            if (e instanceof ValidatorException)
                throw (ValidatorException) e;

            throw new ValidatorException(e);
        }
    }

    /**
     * 本组件内置注解所在的包名前缀。
     */
    private static final String AJ_PACKAGE = "com.ajaxjs.framework.validator.custom";
    /**
     * 注解消息配置。
     */
    private final ValidatorProperties properties;

    /**
     * 解析并执行字段或参数上的校验注解。
     *
     * @param annotations 待处理的注解
     * @param value       待校验的值
     * @param fieldName   字段或路径变量名称
     * @throws ValidatorException              校验失败时抛出
     * @throws ValidatorConfigurationException 注解配置不正确时抛出
     */
    public void resolveAnnotations(Annotation[] annotations, Object value, String fieldName) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            String name = annotationType.getName();

            if (name.startsWith(AJ_PACKAGE + ".")) {
                Boolean required = (Boolean) AnnotationUtils.getValue(annotation, "required");
                if (Boolean.FALSE.equals(required) && (value == null || !StringUtils.hasText(value.toString())))
                    continue;

                if (isCommonConstraint(annotationType.getSimpleName())) {
                    String message = getValue(annotation);
                    if (!StringUtils.hasText(message))
                        throw new ValidatorConfigurationException("Correctly configure annotation message property");

                    validateCommonConstraint(annotation, value, fieldName + " " + message);
                } else {
                    ValidatorEnum validConstant = ValidatorEnum.getInstance(annotationType.getSimpleName());
                    if (validConstant != null) {
                        String message = getValue(annotation);
                        if (!StringUtils.hasText(message))
                            throw new ValidatorConfigurationException("Correctly configure annotation message property");

                        validConstant.validated(value, fieldName + " " + message);
                    } else
                        throw new ValidatorConfigurationException("Correctly configure easy validator annotation");
                }
            } else {
                ValidatorRule rule = getRule(annotation);
                if (rule != null)
                    rule.validate(annotation, value, fieldName);
            }
        }
    }

    /**
     * 查找支持指定注解的扩展规则。
     *
     * @param annotation 待匹配的注解
     * @return 匹配的规则；没有匹配项时为 {@code null}
     */
    ValidatorRule getRule(Annotation annotation) {
        for (ValidatorRule rule : rules) {
            if (rule.supports(annotation))
                return rule;
        }

        return null;
    }

    /**
     * 判断注解名称是否为内置通用约束。
     *
     * @param annotationName 注解简单名称
     * @return 内置通用约束时为 {@code true}
     */
    boolean isCommonConstraint(String annotationName) {
        return "NotNull".equals(annotationName) || "NotBlank".equals(annotationName)
                || "Size".equals(annotationName) || "Min".equals(annotationName)
                || "Max".equals(annotationName) || "Pattern".equals(annotationName)
                || "Email".equals(annotationName);
    }

    /**
     * 执行内置通用约束的校验。
     *
     * @param annotation   约束注解
     * @param value        待校验的值
     * @param errorMessage 校验失败提示语
     * @throws ValidatorException              校验失败时抛出
     * @throws ValidatorConfigurationException 约束用于不支持的类型或配置不正确时抛出
     */
    void validateCommonConstraint(Annotation annotation, Object value, String errorMessage) {
        String annotationName = annotation.annotationType().getSimpleName();

        if ("NotNull".equals(annotationName)) {
            if (value == null)
                throw new ValidatorException(errorMessage);
        } else if ("NotBlank".equals(annotationName)) {
            if (!(value instanceof CharSequence) || !StringUtils.hasText(value.toString()))
                throw new ValidatorException(errorMessage);
        } else if ("Size".equals(annotationName)) {
            if (value != null) {
                int length = getSize(value);
                int min = ((Integer) AnnotationUtils.getValue(annotation, "min")).intValue();
                int max = ((Integer) AnnotationUtils.getValue(annotation, "max")).intValue();
                if (length < min || length > max)
                    throw new ValidatorException(errorMessage);
            }
        } else if ("Min".equals(annotationName)) {
            long min = ((Long) AnnotationUtils.getValue(annotation, "value")).longValue();
            if (value != null && toBigDecimal(value).compareTo(BigDecimal.valueOf(min)) < 0)
                throw new ValidatorException(errorMessage);
        } else if ("Max".equals(annotationName)) {
            long max = ((Long) AnnotationUtils.getValue(annotation, "value")).longValue();
            if (value != null && toBigDecimal(value).compareTo(BigDecimal.valueOf(max)) > 0)
                throw new ValidatorException(errorMessage);
        } else if ("Pattern".equals(annotationName)) {
            if (value != null) {
                if (!(value instanceof CharSequence))
                    throw new ValidatorConfigurationException("@Pattern only supports CharSequence values");

                int flags = getPatternFlags(annotation);
                String regexp = (String) AnnotationUtils.getValue(annotation, "regexp");

                if (!java.util.regex.Pattern.compile(regexp, flags).matcher(value.toString()).matches())
                    throw new ValidatorException(errorMessage);
            }
        } else if ("Email".equals(annotationName)) {
            if (value != null && !value.toString().isEmpty()) {
                if (!(value instanceof CharSequence) || !ValidatorHelper.isEmail(value.toString()))
                    throw new ValidatorException(errorMessage);

                int flags = getPatternFlags(annotation);
                String regexp = (String) AnnotationUtils.getValue(annotation, "regexp");

                if (!java.util.regex.Pattern.compile(regexp, flags).matcher(value.toString()).matches())
                    throw new ValidatorException(errorMessage);
            }
        } else
            throw new ValidatorConfigurationException("Unsupported validation annotation: "
                    + annotation.annotationType().getName());
    }

    /**
     * 获取 {@link com.ajaxjs.framework.validator.custom.Pattern} 的正则标志位。
     *
     * @param annotation Pattern 注解
     * @return 合并后的 {@link java.util.regex.Pattern} 标志位
     */
    int getPatternFlags(Annotation annotation) {
        Object flags = AnnotationUtils.getValue(annotation, "flags");
        if (!(flags instanceof Object[]))
            return 0;

        int value = 0;
        for (Object flag : (Object[]) flags) {
            value |= ((com.ajaxjs.framework.validator.custom.Pattern.Flag) flag).getValue();
        }

        return value;
    }

    /**
     * 获取支持 {@code @Size} 约束的对象长度。
     *
     * @param value 字符序列、集合、映射或数组
     * @return 对象长度或元素数量
     * @throws ValidatorConfigurationException 值的类型不受支持时抛出
     */
    int getSize(Object value) {
        if (value instanceof CharSequence)
            return ((CharSequence) value).length();
        if (value instanceof Collection<?>)
            return ((Collection<?>) value).size();
        if (value instanceof Map<?, ?>)
            return ((Map<?, ?>) value).size();
        if (value.getClass().isArray())
            return Array.getLength(value);

        throw new ValidatorConfigurationException("@Size only supports CharSequence, Collection, Map, or arrays");
    }

    /**
     * 将数值转换为 {@link BigDecimal} 以便比较边界。
     *
     * @param value 待转换的数值
     * @return 转换后的数值
     * @throws ValidatorConfigurationException 值不是 {@link Number} 时抛出
     */
    BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number)
            return new BigDecimal(value.toString());

        throw new ValidatorConfigurationException("@Min and @Max only support Number values");
    }

    /**
     * 从注解上获取错误信息，占位符从 Spring Boot 配置属性解析。
     *
     * @param annotation 校验注解
     * @return 解析后的错误提示语
     * @throws ValidatorConfigurationException 注解未声明 {@code message} 属性时抛出
     */
    String getValue(Annotation annotation) {
        String message = (String) AnnotationUtils.getValue(annotation, "message");
        if (message == null)
            throw new ValidatorConfigurationException("Annotation must declare a message property");

        if (message.startsWith("{") && message.endsWith("}")) {
            String key = message.substring(1, message.length() - 1);
            String configuredMessage = properties.getMessages().get(key);
            if (configuredMessage != null)
                message = configuredMessage;
        }

        return message;
    }

    /**
     * 将嵌套的 Map 转换为平铺的 Map
     *
     * @param nestedMap 嵌套的 Map
     * @return 平铺的 Map
     */
    public static Map<String, Object> flattenMap(Map<String, Object> nestedMap) {
        Map<String, Object> flatMap = new HashMap<>();
        flattenMapHelper(nestedMap, "", flatMap);

        return flatMap;
    }

    /**
     * 递归方法，用于将嵌套 Map 的键值对平铺到目标 Map 中
     *
     * @param currentMap 当前处理的 Map
     * @param prefix     当前键的前缀
     * @param flatMap    平铺的目标 Map
     */
    static void flattenMapHelper(Map<String, Object> currentMap, String prefix, Map<String, Object> flatMap) {
        for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                // 如果值是嵌套的 Map，则递归处理
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) value;
                flattenMapHelper(nested, key, flatMap);
            } else
                flatMap.put(key, value);  // 如果值是普通对象，直接放入平铺的 Map 中
        }
    }
}
