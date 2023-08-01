package com.blazemaple;

import com.blazemaple.api.service.HelloBRPC;
import com.blazemaple.core.HeartbeatDetector;
import com.blazemaple.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 2:16
 */
@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) {
        ReferenceConfig<HelloBRPC> reference=new ReferenceConfig<>();
        reference.setInterface(HelloBRPC.class);

        BrpcBootstrap.getInstance()
            .application("brpc-consumer")
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .serializer("jdk")
            .compressor("gzip")
            .reference(reference);

        HelloBRPC helloBRPC = reference.get();
        for (int i=0;i<10;i++){
            String blazemaple = helloBRPC.sayHello("blazemaple");
            log.info("sayHello result:{}",blazemaple);
        }
    }
}
