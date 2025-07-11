package com.ajaxjs.redis;

import com.ajaxjs.redis.leveltwocache.LevelTwoCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Config {
    /**
     * 配置 RedisTemplate
     *
     * @param factory 链接配置
     * @return RedisTemplate
     */
    @Bean
//    @Lazy
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());  // 设置键的序列化方式
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 设置值的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer()); // 设置哈希键的序列化方式
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());        // 设置哈希值的序列化方式

        return template;
    }

    @Value("${springext.cache.redis.topic:level_cache}")
    String topicName;

    @Bean
    public LevelTwoCacheManager cacheManager(RedisConnectionFactory connectionFactory, RedisTemplate<String, Object> redisTemplate) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)); // 设置缓存有效期一小时

        LevelTwoCacheManager cacheManager = new LevelTwoCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory), redisCacheConfiguration);
        cacheManager.setRedisTemplate(redisTemplate);
        cacheManager.setTopicName(topicName);

        return cacheManager;
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(topicName));

        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(LevelTwoCacheManager cacheManager) {
        return new MessageListenerAdapter((MessageListener) (message, pattern) -> {
            byte[] bs = message.getChannel();
            cacheManager.receiver(new String(bs, StandardCharsets.UTF_8));
        });
    }
}
