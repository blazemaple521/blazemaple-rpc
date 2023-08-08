package com.blazemaple.discovery;

import com.blazemaple.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author BlazeMaple
 * @description 注册中心
 * @date 2023/7/21 2:02
 */
public interface Registry {

    /**
     * 注册服务
     *
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取服务列表
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    List<InetSocketAddress> lookup(String serviceName, String group);
}
