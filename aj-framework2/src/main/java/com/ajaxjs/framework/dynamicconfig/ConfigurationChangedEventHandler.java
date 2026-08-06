package com.ajaxjs.framework.dynamicconfig;

import com.ajaxjs.framework.dynamicconfig.model.ConfigurationChangedEvent;

import com.ajaxjs.framework.dynamicconfig.model.ValueBeanFieldBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ajaxjs.framework.dynamicconfig.ConfigurationUtils.SP_EL_PREFIX;
import static com.ajaxjs.framework.dynamicconfig.DynamicConfigBeanPostProcessor.DYNAMIC_FIELD_BINDER_MAP;


/**
 * @author Code2Life
 **/
@Slf4j
@ConditionalOnBean(DynamicConfigPropertiesWatcher.class)
public class ConfigurationChangedEventHandler {
    /**
     * Dot separator used in property paths.
     */
    private static final String DOT_SYMBOL = ".";
    /**
     * Pattern for indexed properties such as {@code a.b[0].c}.
     */
    private static final String INDEXED_PROP_PATTERN = "\\[\\d{1,3}]";

    /**
     * Resolver for evaluating SpEL expressions.
     */
    private final BeanExpressionResolver exprResolver;
    /**
     * Expression evaluation context.
     */
    private final BeanExpressionContext exprContext;
    /**
     * Re-binding processor for {@code @ConfigurationProperties} beans.
     */
    private final ConfigurationPropertiesBindingPostProcessor processor;
    /**
     * Bean factory used for placeholder resolution and type conversion.
     */
    private final ConfigurableListableBeanFactory beanFactory;

    /**
     * Create an event handler that can refresh {@code @Value} fields and {@code @ConfigurationProperties} beans.
     *
     * @param applicationContext Spring application context
     * @param beanFactory        bean factory (must be {@link ConfigurableListableBeanFactory})
     * @param eventPublisher     application event publisher
     */
    ConfigurationChangedEventHandler(ApplicationContext applicationContext, BeanFactory beanFactory, ApplicationEventPublisher eventPublisher) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory))
            throw new IllegalArgumentException("DynamicConfig requires a ConfigurableListableBeanFactory");

        ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) beanFactory;
        this.beanFactory = factory;
        this.processor = applicationContext.getBean(ConfigurationPropertiesBindingPostProcessor.class);
        this.exprResolver = (factory).getBeanExpressionResolver();
        this.exprContext = new BeanExpressionContext(factory, null);
    }

    /**
     * Listen config changed event, to process related beans and set latest values for their fields
     *
     * @param event ConfigurationChangedEvent indicates a configuration file changed event
     */
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ConfigurationChangedEvent.class)
    public synchronized void handleEvent(ConfigurationChangedEvent event) {
        try {
            Map<String, Object> diff = event.getDiff();
            Map<String, ValueBeanFieldBinder> toRefreshProps = new HashMap<>(4);

            for (Map.Entry<String, Object> entry : diff.entrySet()) {
                String key = entry.getKey();
                processConfigPropsClass(toRefreshProps, key);
                processValueField(key, entry.getValue());
            }

            rebindRelatedConfigurationPropsBeans(diff, toRefreshProps);
            log.info("config changes of {} have been processed", event.getSource());
        } catch (Exception ex) {
            log.warn("config changes of {} can not be processed, error:", event.getSource(), ex);
        }
    }

    /**
     * Identify {@code @ConfigurationProperties} beans that should be re-bound for a changed key.
     *
     * @param result output map that collects bean binders to refresh
     * @param key    changed property key
     */
    private void processConfigPropsClass(Map<String, ValueBeanFieldBinder> result, String key) {
        DynamicConfigBeanPostProcessor.DYNAMIC_CONFIG_PROPS_BINDER_MAP.forEach((prefix, binder) -> {
            if (StringUtils.startsWithIgnoreCase(ConfigurationUtils.normalizePropKey(key), prefix)) {
                log.debug("prefix matched for ConfigurationProperties bean: {}, prefix: {}", binder.getBeanName(), prefix);
                result.put(binder.getBeanName(), binder);
            }
        });
    }

    /**
     * Refresh all {@code @Value} bound fields that depend on the given key.
     *
     * @param keyRaw changed property key (raw form)
     * @param val    newest property value
     * @throws IllegalAccessException when reflective field access fails
     */
    private void processValueField(String keyRaw, Object val) throws IllegalAccessException {
        String key = ConfigurationUtils.normalizePropKey(keyRaw);

        if (!DYNAMIC_FIELD_BINDER_MAP.containsKey(key)) {
            log.debug("no bound field of changed property found, skip dynamic config processing of key: {}", keyRaw);
            return;
        }

        List<ValueBeanFieldBinder> valueFieldBinders = DYNAMIC_FIELD_BINDER_MAP.get(key);

        for (ValueBeanFieldBinder binder : valueFieldBinders) {
            Object bean = binder.getBeanRef().get();

            if (bean == null)
                continue;

            convertAndBindFieldValue(val, binder, bean);
        }
    }

    /**
     * Convert the new value and set it into the target field.
     *
     * @param val    raw property value
     * @param binder binder metadata
     * @param bean   target bean instance
     * @throws IllegalAccessException when reflective field access fails
     */
    private void convertAndBindFieldValue(Object val, ValueBeanFieldBinder binder, Object bean) throws IllegalAccessException {
        Field field = binder.getDynamicField();
        field.setAccessible(true);
        String expr = binder.getExpr();
        String newExpr = beanFactory.resolveEmbeddedValue(expr);

        if (expr.startsWith(SP_EL_PREFIX)) {
            Object evaluatedVal = exprResolver.evaluate(newExpr, exprContext);
            field.set(bean, convertIfNecessary(field, evaluatedVal));
        } else
            field.set(bean, convertIfNecessary(field, val));

        if (log.isDebugEnabled())
            log.debug("dynamic config found, set field: '{}' of class: '{}' with new value", field.getName(), bean.getClass().getSimpleName());
    }

    /**
     * Rebind {@code @ConfigurationProperties} beans that are affected by the changed keys.
     *
     * @param diff           diff map of changed properties
     * @param toRefreshProps target beans to refresh
     * @throws IllegalAccessException when reflective field access fails
     */
    private void rebindRelatedConfigurationPropsBeans(Map<String, Object> diff, Map<String, ValueBeanFieldBinder> toRefreshProps) throws IllegalAccessException {
        for (Map.Entry<String, ValueBeanFieldBinder> entry : toRefreshProps.entrySet()) {
            String beanName = entry.getKey();
            ValueBeanFieldBinder binder = entry.getValue();
            Object bean = binder.getBeanRef().get();

            if (bean != null) {
                processor.postProcessBeforeInitialization(bean, beanName);
                // AggregateBinder - MapBinder will merge properties while binding
                // need to check deleted keys and remove from map fields
                removeMissingPropsMapFields(diff, bean, binder.getExpr());
                log.debug("changes detected, re-bind ConfigurationProperties bean: {}", beanName);
            }
        }
    }

    /**
     * Remove map entries from {@code @ConfigurationProperties} beans when keys are deleted.
     *
     * @param diff     diff map
     * @param rootBean root bean instance
     * @param prefix   configuration prefix of the bean
     * @throws IllegalAccessException when reflective field access fails
     */
    private void removeMissingPropsMapFields(Map<String, Object> diff, Object rootBean, String prefix) throws IllegalAccessException {
        for (Map.Entry<String, Object> entry : diff.entrySet()) {
            Object value = entry.getValue();

            if (value != null)
                // only null value prop need to be removed from field value
                continue;

            String rawKey = entry.getKey();
            // 'a.b[1].c.d' liked changes would be refreshed wholly, no need to handle
            if (rawKey.matches(INDEXED_PROP_PATTERN))
                continue;

            // if key 'a.b.c.d' is removed, need to check if 'a.b.c' is a map, if so, remove map key 'd'
            String normalizedFieldPath = findParentPath(prefix, rawKey);
            String leafKey = rawKey.substring(rawKey.lastIndexOf(DOT_SYMBOL) + 1);
            removeMissingMapKeyIfMatch(ConfigurationUtils.getTargetClassOfBean(rootBean), rootBean, normalizedFieldPath, leafKey);
        }
    }

    /**
     * Walk the object graph to find map fields and remove the missing key if path matches.
     *
     * @param clazz  current class being inspected
     * @param obj    current object instance
     * @param path   normalized field path
     * @param mapKey map key to remove
     * @throws IllegalAccessException when reflective field access fails
     */
    private void removeMissingMapKeyIfMatch(Class<?> clazz, Object obj, String path, String mapKey) throws IllegalAccessException {
        int pos = path.indexOf(DOT_SYMBOL);
        boolean onLeaf = pos == -1;
        Field[] fields = clazz.getDeclaredFields();

        for (Field f : fields) {
            if (isIgnorableField(f))
                continue;

            String fieldName = f.getName();
            boolean matchObjPath = StringUtils.startsWithIgnoreCase(path, ConfigurationUtils.normalizePropKey(fieldName));
            if (matchObjPath && onLeaf && Map.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                ((Map<?, ?>) f.get(obj)).remove(mapKey);
                log.info("key {} has been removed from {} because of configuration change.", mapKey, path);
                break;
            }

            // dive to next level for case: path: a.b.c, field: b
            if (matchObjPath && !onLeaf) {
                f.setAccessible(true);
                Object subObj = f.get(obj);
                removeMissingMapKeyIfMatch(subObj.getClass(), subObj, path.substring(pos + 1), mapKey);
            }
        }
    }

    /**
     * Determine whether a field should be ignored when traversing nested properties.
     *
     * @param f target field
     * @return {@code true} if the field is not eligible for deep traversal
     */
    private boolean isIgnorableField(Field f) {
        int modifiers = f.getModifiers();
        Class<?> type = f.getType();

        return Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || BeanUtils.isSimpleValueType(type);
    }

    /**
     * Find the parent path of a removed key relative to a prefix.
     *
     * @param prefix configuration prefix
     * @param rawKey raw removed key
     * @return normalized parent path, or empty string if directly under prefix
     */
    private String findParentPath(String prefix, String rawKey) {
        String normalizedFieldPath = ConfigurationUtils.normalizePropKey(rawKey).substring(prefix.length() + 1);
        int pathPos = normalizedFieldPath.lastIndexOf(DOT_SYMBOL);

        if (pathPos != -1)
            normalizedFieldPath = normalizedFieldPath.substring(0, pathPos);
         else
            normalizedFieldPath = "";

        return normalizedFieldPath;
    }

    /**
     * Convert a raw property value to the target field type using Spring's {@link TypeConverter}.
     *
     * @param field target field
     * @param value raw value
     * @return converted value
     */
    private Object convertIfNecessary(Field field, Object value) {
        TypeConverter converter = beanFactory.getTypeConverter();

        return converter.convertIfNecessary(value, field.getType(), field);
    }
}
