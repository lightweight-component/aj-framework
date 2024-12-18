//package com.ajaxjs.util.cache.mixredis;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.listener.PatternTopic;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
//
//import java.io.UnsupportedEncodingException;
//import java.nio.charset.StandardCharsets;
//
//public class CacheConfig {
//    @Value("${springext.cache.redis.topic:cache}")
//    String topicName;
//
//
//    @Bean
//    public MyRedisCacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate) {
//        MyRedisCacheManager cacheManager = new MyRedisCacheManager(redisTemplate);
//        cacheManager.setUsePrefix(true);
//
//        return cacheManager;
//    }
//
//    @Bean
//    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
//
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.addMessageListener(listenerAdapter, new PatternTopic(topicName));
//
//        return container;
//    }
//
//    @Bean
//    MessageListenerAdapter listenerAdapter(MyRedisCacheManager cacheManager) {
//        return new MessageListenerAdapter(new MessageListener() {
//            @Override
//            public void onMessage(Message message, byte[] pattern) {
//                byte[] bs = message.getChannel();
//                String type = new String(bs, StandardCharsets.UTF_8);
//                cacheManager.receiver(type);
//            }
//        });
//    }
//}
