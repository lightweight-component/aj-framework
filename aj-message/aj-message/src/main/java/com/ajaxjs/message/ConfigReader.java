package com.ajaxjs.message;

import com.ajaxjs.util.io.Resources;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {
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

            return flattenMap(m);  // 将解析后的嵌套Map转换为平铺的 Map，方便使用
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
