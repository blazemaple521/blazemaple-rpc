package com.blazemaple;

import com.blazemaple.discovery.Registry;
import com.blazemaple.discovery.RegistryConfig;
import com.blazemaple.discovery.impl.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 1:32
 */
@Slf4j
public class BrpcBootstrap {

    //BrpcBootstrap是一个单例，每个应用程序只有一个实例

    private static BrpcBootstrap brpcBootstrap = new BrpcBootstrap();

    //相关基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;

    private static final Map<String,ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    //todo 待处理
    private Registry registry;

    private int port = 8088;


    private ZooKeeper zooKeeper;


    private BrpcBootstrap() {
        //todo 构造启动引导程序，需要初始化一些参数
    }

    public static BrpcBootstrap getInstance() {
        return brpcBootstrap;
    }

    /**
     * 设置应用程序名称
     *
     * @param applicationName 应用名称
     * @return this
     */

    public BrpcBootstrap application(String applicationName) {
        //todo 设置应用程序名称
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 设置注册中心
     *
     * @param registryConfig 注册中心配置
     * @return this
     */

    public BrpcBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 设置协议
     *
     * @param protocolConfig 协议配置
     * @return this
     */
    public BrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        if (log.isDebugEnabled()) {
            log.debug("set protocolConfig:{}", protocolConfig.toString());
        }
        //todo 设置序列化协议
        this.protocolConfig = protocolConfig;
        return this;
    }

    /**
     * 发布服务
     *
     * @param service 服务配置
     * @return this
     */

    public BrpcBootstrap publish(ServiceConfig<?> service) {
        //todo 发布服务
        registry.register(service);
        SERVICE_LIST.put(service.getInterface().getName(),service);
        return this;
    }

    /**
     * 批量发布服务
     *
     * @param services 服务配置列表
     * @return this
     */
    public BrpcBootstrap publish(List<ServiceConfig<?>> services) {
        //todo 发布服务
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动引导程序
     */
    public void start() {
        //todo 启动引导程序
        try {
            Thread.sleep(100000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public BrpcBootstrap reference(ReferenceConfig<?> reference) {
        //todo 引用服务
        reference.setRegistry(registry);
        return this;
    }

}
