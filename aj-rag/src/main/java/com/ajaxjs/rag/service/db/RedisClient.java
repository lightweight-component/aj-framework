package com.ajaxjs.rag.service.db;

import com.ajaxjs.rag.constant.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Collections;
import java.util.List;

public class RedisClient {
    private static Jedis jedis;
    private static RedisClient instance;

    static {
        // 静态代码块中初始化 Jedis 客户端
        jedis = new Jedis(Config.REDIS_HOST, Config.REDIS_PORT);
        if (Config.REDIS_PASSWORD != null && !Config.REDIS_PASSWORD.isEmpty()) {
            jedis.auth(Config.REDIS_PASSWORD);
        }
    }

    // 提供一个公共的静态方法来获取实例
    public static synchronized RedisClient getInstance() {
        if (instance == null) {
            instance = new RedisClient();
        }
        return instance;
    }

    // 将元素添加到列表的头部，并设置过期时间（如果需要）
    public static long lpush(String key, String element, Integer expireSeconds) {
        try {
            jedis.lpush(key, element);
            if (expireSeconds != null && !jedis.exists(key)) {
                jedis.expire(key, expireSeconds);
            }
            return jedis.llen(key);
        } catch (JedisException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // 获取列表中指定范围内的元素
    public static List<String> lrange(String key, long start, long end) {
        try {
            return jedis.lrange(key, start, end);
        } catch (JedisException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // 删除指定的键
    public static boolean delete(String key) {
        try {
            if (jedis.exists(key)) {
                jedis.del(key);
                return true;
            }
            return false;
        } catch (JedisException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 关闭 Jedis 连接
    public static void close() {
        if (jedis != null) {
            jedis.close();
        }
    }

    // 主方法，用于演示如何使用 RedisClient 类
    public static void main(String[] args) {
        RedisClient redisClient = new RedisClient();
        // 向列表中添加元素并设置过期时间
        long result = lpush("myListKey", "myElement", 60); // 设置过期时间为 60 秒
        System.out.println("List length after lpush: " + result);

        // 获取列表中的所有元素
        List<String> elements = lrange("myListKey", 0, -1);
        System.out.println("List elements: " + elements);

        // 删除列表
        boolean isDeleted = delete("myListKey");
        System.out.println("Is list deleted? " + isDeleted);

        // 关闭 Redis 连接
        close();
    }
}