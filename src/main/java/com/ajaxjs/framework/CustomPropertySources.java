package com.ajaxjs.framework;

import com.ajaxjs.util.convert.ConvertBasicValue;
import com.ajaxjs.util.io.Resources;
import com.ajaxjs.util.map_traveler.MapUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * PropertySourcesPlaceholderConfigurer 是一个由 Spring 提供的用于解析属性占位符的配置类，
 * 它没有提供直接获取私有属性 localProperties 的公开方法。但是，可以通过以下步骤获取 localProperties 的值
 */
public class CustomPropertySources extends PropertySourcesPlaceholderConfigurer {
    private Properties localProperties;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);

        try {
            localProperties = mergeProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Properties getLocalProperties() {
        return localProperties;
    }

    /**
     * 获取配置值
     *
     * @param key 配置 key
     * @return 配置值
     */
    public static String getConfig(String key) {
        CustomPropertySources bean = DiContextUtil.getBean(CustomPropertySources.class);
        assert bean != null;
        Object o = bean.getLocalProperties().get(key);

        if (o != null)
            return o.toString();
        else {
            System.err.println("找不到 " + key + "配置");

            return null;
        }
    }

    /**
     * 从配置中获取指 定key 对应的值，并将其转换为指定类型后返回
     *
     * @param key 配置的 key
     * @param clz 需要转换的目标类型
     * @param <T> 类型参数
     * @return 转换后的值
     */
    public static <T> T getConfig(String key, Class<T> clz) {
        String value = getConfig(key);

        return ConvertBasicValue.basicCast(value, clz);
    }

    /**
     * 从指定的 YAML 配置文件中加载配置信息。
     *
     * @param configFile 配置文件的名称，该文件应在项目的资源目录中。
     * @return 一个包含配置信息的 Map 对象，其中键值对代表配置的名称和值。
     * @throws RuntimeException 如果无法读取配置文件或发生 IO 异常。
     */
    public static Map<String, Object> getConfigFromYml(String configFile) {
        Yaml yaml = new Yaml();

        try (InputStream resourceAsStream = Resources.getResource(configFile)) {
            Map<String, Object> m = yaml.load(resourceAsStream);

            return MapUtils.flatMap(m);  // 将解析后的嵌套Map转换为平铺的 Map，方便使用
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}