package com.ajaxjs.framework.cache;

/**
 * 缓存接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface Cache<K, V> {
    /**
     * 将对象加入到缓存
     *
     * @param key     键
     * @param value   对象
     * @param timeout 过期时间，单位：毫秒， 0表示无限长
     */
    void put(K key, V value, long timeout);

    default void put(K key, V value, int timeout) {
        put(key, value, timeout * 1000L);
    }

    default void put(K key, V value) {
        put(key, value, 0);
    }

    /**
     * 从缓存中获得对象
     *
     * @param key 键
     * @return 键对应的对象
     */
    V get(K key);

    /**
     * 根据指定的键获取相应的值，并将该值转换为指定的类型返回。
     *
     * @param key 键
     * @param clz 指定的类
     * @param <T> 期望的类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    default <T> T get(K key, Class<T> clz) {
        V v = get(key);

        return (T) v;
    }

    /**
     * 从缓存中删除对象
     *
     * @param key 键
     */
    void remove(K key);
}
