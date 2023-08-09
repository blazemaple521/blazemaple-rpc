package com.blazemaple;

import com.blazemaple.discovery.RegistryConfig;
import com.blazemaple.utils.IdGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/9 15:15
 */
@Component
public class BrpcClient {

    private BrpcConfig brpcConfig;

    public BrpcClient(BrpcConfig brpcConfig) {
        this.brpcConfig = brpcConfig;
    }


    public void start() {
        BrpcBootstrap.getInstance()
            .port(brpcConfig.getPort())
            .application(brpcConfig.getApplicationName())
            .group(brpcConfig.getGroup())
            .registry(new RegistryConfig(brpcConfig.getRegistryUrl()))
            .serializer(brpcConfig.getSerializeType())
            .compressor(brpcConfig.getCompressType())
            .idGenerator(new IdGenerator(brpcConfig.getDataCenterNum(), brpcConfig.getMachineNum()))
            .loadBalancer(brpcConfig.getLoadBalancer())
            .scan(brpcConfig.getScanPackage())
            .start();
    }

}
