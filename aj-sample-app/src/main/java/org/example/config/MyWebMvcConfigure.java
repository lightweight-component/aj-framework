package org.example.config;

import com.ajaxjs.api.encryptedbody.EncryptedBodyConverter;
import com.ajaxjs.api.security.referer.HttpReferer;
import com.ajaxjs.api.time_signature.TimeSignature;
import com.ajaxjs.springboot.BaseWebMvcConfigure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

@Configuration
public class MyWebMvcConfigure extends BaseWebMvcConfigure {
    @Value("${api.EncryptedBody.publicKey}")
    private String apiPublicKey;

    @Value("${api.EncryptedBody.privateKey}")
    private String apiPrivateKey;

    @Value("${api.EncryptedBody.enable}")
    private boolean apiEncryptedBodyEnable;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        EncryptedBodyConverter converter = new EncryptedBodyConverter(apiPublicKey, apiPrivateKey);
        converter.setEnabled(apiEncryptedBodyEnable);

        converters.add(0, converter);
    }


//    @Bean
//    public MappingJackson2HttpMessageConverter converter() {
//        MappingJackson2HttpMessageConverter messageConverter = new MyBeanC();
//
//        return messageConverter;
//    }
    /**
     * 配置 RedisTemplate
     *
     * @param factory 链接配置
     * @return RedisTemplate
     */
//    @Bean
////    @Lazy
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());  // 设置键的序列化方式
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 设置值的序列化方式
//        template.setHashKeySerializer(new StringRedisSerializer()); // 设置哈希键的序列化方式
//        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());        // 设置哈希值的序列化方式
//
//        return template;
//    }


    @Bean
    public TimeSignature TimeSignature() {
        TimeSignature timeSignature = new TimeSignature();
        timeSignature.setGlobalCheck(true);

        return timeSignature;
    }

    @Bean
    HttpReferer HttpReferer() {
        return new HttpReferer();
    }


    @Value("${springext.cache.redis.topic:level_cache}")
    String topicName;

//    @Bean
//    public LevelTwoCacheManager cacheManager(RedisConnectionFactory connectionFactory, RedisTemplate<String, Object> redisTemplate) {
//        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofHours(1)); // 设置缓存有效期一小时
//
//        LevelTwoCacheManager cacheManager = new LevelTwoCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory), redisCacheConfiguration);
//        cacheManager.setRedisTemplate(redisTemplate);
//        cacheManager.setTopicName(topicName);
//
//        return cacheManager;
//    }
//
//    @Bean
//    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.addMessageListener(listenerAdapter, new PatternTopic(topicName));
//
//        return container;
//    }
//
//    @Bean
//    MessageListenerAdapter listenerAdapter(LevelTwoCacheManager cacheManager) {
//        return new MessageListenerAdapter((MessageListener) (message, pattern) -> {
//            byte[] bs = message.getChannel();
//            String type = new String(bs, StandardCharsets.UTF_8);
//            cacheManager.receiver(type);
//        });
//    }
}
