package com.ajaxjs.spring;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A Ioc Helper
 */
@Slf4j
@Component
public class DiContextUtil implements ApplicationContextAware {
    /**
     * Spring 上下文
     */
    public static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        DiContextUtil.context = context;
    }

    /**
     * 获取上下文
     *
     * @return 上下文对象
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * 获取已注入的对象
     *
     * @param <T> 对象类型
     * @param clz 对象类型引用
     * @return 组件对象
     */
    public static <T> T getBean(Class<T> clz) {
        if (context == null) {
            log.warn("Spring Bean 未准备好，不能返回 {} 类", clz);
            return null;
        }

        try {
            return context.getBean(clz);
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("No such bean of class {}.", clz);

            return null;
        }
    }

    /**
     * 获取已注入的对象
     *
     * @param beanName 组件对象 id
     * @return 组件对象
     */
    public static Object getBean(String beanName) {
        if (context == null) {
            log.warn("Spring Bean 未准备好，不能返回 {} Bean.", beanName);
            return null;
        }

        try {
            return context.getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("No such bean {}.", beanName);
            return null;
        }
    }

    public static <T> T getBean(HttpServletRequest request, Class<T> clazz) {
        ApplicationContext cxt = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());

        try {
            return cxt == null ? null : cxt.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("No such bean of class {}.", clazz);

            return null;
        }
    }

    /**
     * 获取指定key对应的消息。
     *
     * @param key 指定消息的 key
     * @return 返回指定 key 对应的消息
     */
    public static String getMessage(String key) {
        return context.getMessage(key, null, Locale.getDefault());
    }

    /**
     * 根据接口类型查找并返回所有实现该接口的bean实例
     * 此方法用于从应用上下文中检索所有匹配给定接口类型的bean，
     * 并将这些bean按照它们在上下文中的id以键值对的形式返回
     *
     * @param <T> 泛型参数，表示接口类型
     * @param clz 接口的Class对象，用于查找实现了该接口的所有bean
     * @return 返回一个Map，其中key为bean的id，value为bean实例
     */
    public static <T> Map<String, T> findByInterface(Class<T> clz) {
        return context.getBeansOfType(clz);
    }

    /**
     * 获取注册者 context->bean factory->registry
     * 此方法用于获取 BeanDefinitionRegistry 实例，该实例允许对 Bean 定义进行更深层次的操作和访问
     * 它通过将当前上下文转换为配置 ApplicationContext，然后从其中获取 BeanFactory 来实现
     * 这个过程涉及到类型转换和对 Spring 内部 API 的直接访问，因此需要对 Spring框架有深入的了解才能正确使用
     *
     * @return BeanDefinitionRegistry 对象，通过它可以操作 Bean 定义
     */
    public static BeanDefinitionRegistry getRegistry() {
        ConfigurableApplicationContext c = (ConfigurableApplicationContext) context;

        return (DefaultListableBeanFactory) c.getBeanFactory();
    }

    /**
     * 手动注入 Bean
     * <p>
     * 该方法允许在运行时动态地向 Spring 容器中注册一个新的 Bean 定义
     * 它通过指定的 Bean 类型和 Bean ID 创建一个泛型 Bean 定义，并将其注册到容器中
     *
     * @param <T>    泛型参数，表示要注册的 Bean 类型
     * @param clz    要注册的 Bean 类的 Class 对象，用于创建 Bean 定义
     * @param beanId 在 Spring 容器中注册的 Bean 的唯一标识符
     */
    public static <T> void registryBean(Class<T> clz, String beanId) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clz);
        getRegistry().registerBeanDefinition(beanId, builder.getBeanDefinition());
    }

    /**
     * 手动注入 Bean 并立即返回
     * 此方法用于在运行时动态注册一个 Bean 到 Spring 容器，并立即获取该 Bean 的实例
     * 主要用于在应用启动后，动态添加并使用新的 Bean 实例，而不必重新启动应用
     *
     * @param <T>    Bean 的类型，用于指定返回值的类型
     * @param clz    Bean 的类对象，用于确定 Bean 的类型
     * @param beanId Bean 的唯一标识符，用于在 Spring 容器中区分不同的 Bean
     * @return 返回指定类型的 Bean 实例，如果 Bean 注册失败或找不到对应的 Bean，可能返回 null
     */
    public static <T> T registryBeanInstance(Class<T> clz, String beanId) {
        registryBean(clz, beanId);

        return getBean(clz);
    }

    /**
     * 获取当前请求的 HttpServletRequest 对象
     * 如果当前没有请求上下文，则根据是否正在运行测试来返回对应的请求对象
     *
     * @return 当前请求的 HttpServletRequest 对象，如果不存在请求上下文则返回 null
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes == null)
            return null;

        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * For static-way to get request in UNIT TEST
     */
    public static HttpServletRequest request;

    /**
     * 获取当前请求的响应对象
     *
     * @return 响应对象
     */
    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }

    /**
     * 获取当前请求的 HttpSession 对象
     *
     * @return 当前请求的 HttpSession 对象
     */
    public static HttpSession getSession() {
        return Objects.requireNonNull(getRequest()).getSession();
    }

    /**
     * 非 spring 管理的容器获取 application.yml 中的配置
     *
     * @param configName YAML 中的配置名称
     * @return 配置
     */
    public static String getConfigFromYml(String configName) {
        Environment env = getBean(Environment.class);
        if (env == null)
            throw new NullPointerException("Environment Not ready.");

        String property = env.getProperty(configName);

        if (!StringUtils.hasText(property))
            throw new NullPointerException("The config name [" + configName + "] is null.");

        return property;
    }

    /**
     * Environment.resolveRequiredPlaceholders 方法是 Spring Framework 中的一个工具方法，用于解析配置文件中的占位符（placeholder）。
     * 该方法会根据配置文件中的 ${...} 占位符，替换成对应的属性值，如果无法解析，则抛出 IllegalArgumentException 异常
     *
     * @param str 待解析的字符串，包含一个或多个 ${...} 占位符
     * @return 解析后的字符串，其中 ${...} 占位符被替换为对应的环境变量或配置属性值
     * @throws IllegalArgumentException 如果占位符无法被解析且没有提供默认值，则抛出此异常
     */
    public static String resolveRequiredPlaceholders(String str) {
        return context.getEnvironment().resolveRequiredPlaceholders(str);
    }

    public static <T extends Annotation> T getAnnotationFromMethod(Object handler, Class<T> annotationClass) {
        return getAnnotationFromMethod((HandlerMethod) handler, annotationClass);
    }


    /**
     * Get the annotation from the method or its interface
     *
     * @param handlerMethod   The method on the Interface
     * @param annotationClass The class of annotation
     * @param <T>             The type of annotation
     * @return The annotation
     */
    public static <T extends Annotation> T getAnnotationFromMethod(HandlerMethod handlerMethod, Class<T> annotationClass) {
        T annotation = handlerMethod.getMethodAnnotation(annotationClass);

        if (annotation != null)
            return annotation;

        Method method = handlerMethod.getMethod(); // The real controller method

        return method.getAnnotation(annotationClass);
    }
}