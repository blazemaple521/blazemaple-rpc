package com.blazemaple;

import com.blazemaple.spring.BrpcProxyBeanPostProcessor;
import com.blazemaple.spring.BrpcProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/9 14:51
 */
@Configuration
@EnableConfigurationProperties(BrpcConfig.class)
@ConditionalOnProperty(prefix = "brpc", name = "enable", havingValue = "true")
public class BrpcConfiguration {

    @Autowired
    private BrpcConfig brpcConfig;

    @Bean
    public BrpcClient brpcClient() {
        return new BrpcClient(brpcConfig);
    }


    @Bean
    public BrpcProxyFactory brpcProxyFactory() {
        return new BrpcProxyFactory(brpcConfig);
    }

}
