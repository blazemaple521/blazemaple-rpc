package com.blazemaple.constant;

/**
 * @description 常量类
 *
 * @author BlazeMaple
 * @date 2023/7/21 1:16
 */

public class BrpcConstant {
    
    // zookeeper的默认连接地址
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";
    
    // zookeeper默认的超时时间
    public static final int TIME_OUT = 10000;
    
    // 服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PROVIDERS_PATH = "/brpc-metadata/providers";
    public static final String BASE_CONSUMERS_PATH = "/brpc-metadata/consumers";
    
}