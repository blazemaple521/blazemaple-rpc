package com.blazemaple.discovery;

import com.blazemaple.constant.BrpcConstant;
import com.blazemaple.discovery.impl.ZookeeperRegistry;
import com.blazemaple.exceptions.DiscoveryException;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 1:47
 */
public class RegistryConfig {

    private final String connectStr;

    public RegistryConfig(String connectStr) {
        this.connectStr = connectStr;
    }

    public Registry getRegistry() {
        // 1、获取注册中心的类型
        String registryType = getRegistryType(connectStr,true).toLowerCase().trim();
        // 2、通过类型获取具体注册中心
        if( registryType.equals("zookeeper") ){
            String host = getRegistryType(connectStr, false);
            return new ZookeeperRegistry(host, BrpcConstant.TIME_OUT);
        }
        throw new DiscoveryException("未发现合适的注册中心。");
    }

    public String getRegistryType(String connectStr,boolean ifType){
        String[] typeAndHost = connectStr.split("://");
        if(typeAndHost.length != 2){
            throw new RuntimeException("给定的注册中心连接url不合法");
        }
        if(ifType){
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }
}
