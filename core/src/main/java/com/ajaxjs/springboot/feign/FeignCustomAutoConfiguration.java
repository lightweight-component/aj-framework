package com.ajaxjs.springboot.feign;


import feign.Feign;
import feign.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义配置类feign配置类
 */
@Configuration
@ConditionalOnClass({Feign.class})
public class FeignCustomAutoConfiguration {
//	@Value("${gac.furnace.web.micro-service-secret:}")
//	private String microServiceSecret;
    // TODO cachingFactory注入不进去，后置处理
    // @Bean
	/*public Client feignIpClient(CachingSpringLoadBalancerFactory cachingFactory, SpringClientFactory clientFactory) {
		return new LoadBalancerFeignClient(new FeignIpClient(), cachingFactory, clientFactory);
	}*/

    @Bean
    Logger.Level feignLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    FeignLoggerFactory gacFeignLoggerFactory() {
        return FeignLogger::new;
    }
}
