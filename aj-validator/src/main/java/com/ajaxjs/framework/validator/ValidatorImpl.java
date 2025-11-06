package com.ajaxjs.framework.validator;

//import com.ajaxjs.framework.CustomPropertySources;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ValidatorImpl implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Field[] declaredFields = target.getClass().getDeclaredFields();

        try {
            for (Field field : declaredFields) {
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {// isPrivate
                    field.setAccessible(true);
                    resolveAnnotations(field.getDeclaredAnnotations(), field.get(target), field.getName());
                }
            }
        } catch (Exception e) {
            if (e instanceof ValidatorException)
                throw (ValidatorException) e;

            throw new ValidatorException(e);
        }
    }

    private static final String DEFAULT_PACKAGE = "javax.validation.constraints";
    private static final String AJ_PACKAGE = "com.ajaxjs.framework.spring.validator";

    public void resolveAnnotations(Annotation[] annotations, Object value, String fieldName) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            String name = annotationType.getName();

            if (name.contains(DEFAULT_PACKAGE) || name.contains(AJ_PACKAGE)) {
                ValidatorEnum validConstant = ValidatorEnum.getInstance(annotationType.getSimpleName());

                if (validConstant != null) {
                    String message = getValue(annotation);

                    if (!StringUtils.hasText(message))
                        throw new ValidatorException("Correctly configure annotation message property");

                    validConstant.validated(value, fieldName + " " + message);
                } else
                    throw new ValidatorException("Correctly configure easy validator annotation");
            }
        }
    }

    /**
     * 从注解上获取错误信息，如果没有则从默认的 YAML 配置获取
     */
    private String getValue(Annotation annotation) {
        String message = (String) AnnotationUtils.getValue(annotation, "message");
        assert message != null;

        if (message.indexOf('{') > -1) { // 注解上没设置 message，要读取配置
//            CustomPropertySources bean = DiContextUtil.getBean(CustomPropertySources.class);
//            assert bean != null;
//            String key = "javax-validation." + message.replaceAll("^\\{|}$", "");
//            Object o = bean.getLocalProperties().get(key);
//
//            if (o != null)
//                message = o.toString();

            if (yamlConfig == null) {
                // init config
                Yaml yaml = new Yaml();
                // 假设你的配置文件为 application.yaml
                try (InputStream in = ValidatorImpl.class.getClassLoader().getResourceAsStream("application.yml")) {
                    yamlConfig = flattenMap(yaml.load(in));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String key = "javax-validation." + message.replaceAll("^\\{|}$", "");
            Object o = yamlConfig.get(key);

            if (o != null)
                message = o.toString();
        }

        return message;
    }

    private static  Map<String, Object> yamlConfig;

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
    private static void flattenMapHelper(Map<String, Object> currentMap, String prefix, Map<String, Object> flatMap) {
        for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                // 如果值是嵌套的 Map，则递归处理
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) value;
                flattenMapHelper(nested, key, flatMap);
            } else {
                // 如果值是普通对象，直接放入平铺的 Map 中
                flatMap.put(key, value);
            }
        }
    }
}
