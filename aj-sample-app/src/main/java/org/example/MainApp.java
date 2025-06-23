package org.example;

import com.ajaxjs.IdCard;
import com.ajaxjs.service.tools.IIdCard;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ServiceBuilder;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@SpringBootApplication
@SpringBootApplication(exclude = {RedissonAutoConfiguration.class})
@EnableFeignClients
//@EnableDubbo
public class MainApp {
    public static void main(String[] args) {
//        DubboBootstrap.getInstance()
//                .protocol(new ProtocolConfig(CommonConstants.TRIPLE, 50051))
//                .service(ServiceBuilder.newBuilder().interfaceClass(IIdCard.class).ref(new IdCard()).build())
//                .start()
//                .await();

        SpringApplication.run(MainApp.class, args);
    }
}