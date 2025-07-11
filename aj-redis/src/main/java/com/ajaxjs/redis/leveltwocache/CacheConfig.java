package com.ajaxjs.redis.leveltwocache;

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

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class CacheConfig {
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
            String type = new String(bs, StandardCharsets.UTF_8);
            cacheManager.receiver(type);
        });
    }
}
