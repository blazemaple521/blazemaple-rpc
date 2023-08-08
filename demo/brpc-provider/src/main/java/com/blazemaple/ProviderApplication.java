package com.blazemaple;

import com.blazemaple.discovery.RegistryConfig;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 1:38
 */

public class ProviderApplication {

    public static void main(String[] args) {

        //启动引导程序
        //配置--应用名称--注册中心--序列化协议--压缩方式
        //发布服务
        BrpcBootstrap.getInstance()
            .application("brpc-provider")
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .scan("com.blazemaple")
            .start();
    }

}
