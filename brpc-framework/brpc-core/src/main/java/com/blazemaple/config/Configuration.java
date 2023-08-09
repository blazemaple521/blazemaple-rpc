package com.blazemaple.config;

import com.blazemaple.discovery.RegistryConfig;
import com.blazemaple.loadbalancer.LoadBalancer;
import com.blazemaple.loadbalancer.impl.RoundRobinLoadBalancer;
import com.blazemaple.protection.CircuitBreaker;
import com.blazemaple.protection.RateLimiter;
import com.blazemaple.utils.IdGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/3 19:18
 */
@Data
@Slf4j
public class Configuration {

    private int port = 8091;
    private String applicationName = "brpc-default-applicationName";
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    private IdGenerator idGenerator = new IdGenerator(2, 4);

    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    private String serializeType = "jdk";
    private String compressType = "gzip";
    private String group = "default";

    // 为每一个ip配置一个限流器
    private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
    // 为每一个ip配置一个断路器
    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);

    public Configuration() {

        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);
    }



}
