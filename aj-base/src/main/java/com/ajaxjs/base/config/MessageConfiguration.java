package com.ajaxjs.base.config;

import com.ajaxjs.base.service.message.email.Mail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 消息配置
 */
@Configuration
public class MessageConfiguration {
/*
    @Value("${sms.accessKeyId}")
    private String accessKeyId;

    @Value("${sms.accessSecret}")
    private String accessSecret;

    @Value("${sms.signName}")
    private String signName;

    @Value("${sms.templateCode}")
    private String templateCode;

    @Bean
    AliyunSmsEntity AliyunSmsEntity() {
        AliyunSmsEntity sms = new AliyunSmsEntity();
        sms.setAccessKeyId(accessKeyId);
        sms.setAccessSecret(accessSecret);
        sms.setSignName(signName);
        sms.setTemplateCode(templateCode);

        return sms;
    }
*/

    @Value("${Message.email.smtpServer:aa}")
    private String smtpServer;

    @Value("${Message.email.port:25}")
    private int port;

    @Value("${Message.email.account:a}")
    private String account;

    @Value("${Message.email.password:''}")
    private String password;

    @Bean
    @Scope("prototype")
    @ConditionalOnProperty(name = "Message.email", havingValue = "true")
    Mail getMailConfig() {
        Mail mailCfg = new Mail();
        mailCfg.setMailServer(smtpServer);
        mailCfg.setPort(port);
        mailCfg.setAccount(account);
        mailCfg.setPassword(password);

        return mailCfg;
    }

//    /**
//     * 配置 RedisTemplate
//     *
//     * @param factory 链接配置
//     * @return RedisTemplate
//     */
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
}
