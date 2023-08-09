package com.blazemaple;

import com.blazemaple.loadbalancer.LoadBalancer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/9 15:16
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "brpc")
public class BrpcConfig {
    private int port = 8091;
    private String applicationName = "brpc-default-applicationName";
    private String registryUrl = "zookeeper://127.0.0.1:2181";
    private int dataCenterNum = 2;
    private int machineNum= 4;
    private String loadBalancer ="com.blazemaple.loadbalancer.impl.RoundRobinLoadBalancer";
    private String serializeType = "jdk";
    private String compressType = "gzip";
    private String scanPackage;
    private String group= "default";


    public LoadBalancer getLoadBalancer() {
        try {
            Class<?> clazz = Class.forName(loadBalancer);
            return (LoadBalancer) clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
