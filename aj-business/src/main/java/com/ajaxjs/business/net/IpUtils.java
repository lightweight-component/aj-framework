package com.ajaxjs.net;

import java.lang.reflect.Field;
import java.util.Map;

public class IpUtils {
    /**
     * 让 host 的修改立即生效 本地添加一条host：127.0.0.1 www.baidu.com 发现并没立即生效，大概30s之后生效的。
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */

    public static void clearCache() throws NoSuchFieldException, IllegalAccessException {
        Field field = java.net.InetAddress.class.getDeclaredField("addressCache");
        field.setAccessible(true);
        Object addressCache = field.get(null);

        Field cacheMapField = addressCache.getClass().getDeclaredField("cache");
        cacheMapField.setAccessible(true);
        Map cacheMap = (Map) cacheMapField.get(addressCache);
        cacheMap.clear();
    }

}
